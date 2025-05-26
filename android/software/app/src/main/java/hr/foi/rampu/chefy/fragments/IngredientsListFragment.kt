package hr.foi.rampu.chefy.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import hr.foi.rampu.chefy.R
import hr.foi.rampu.chefy.data.db.RecipesDatabase
import hr.foi.rampu.chefy.data.dao.RecipeIngredientDAO
import hr.foi.rampu.chefy.models.recipe.Ingredients
import hr.foi.rampu.chefy.models.recipe.ShoppingList
import hr.foi.rampu.chefy.models.recipe.Units
import hr.foi.rampu.chefy.viewmodels.IngredientViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [IngredientsListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class IngredientsListFragment : Fragment() {

    private lateinit var tv_ingredients_list: LinearLayout
    private var recipeId: Int? = null
    private val db: RecipesDatabase by lazy { RecipesDatabase.getInstance() }
    private val ingredientsToDelete = mutableListOf<String>()
    private val ingredientsList = mutableListOf<ShoppingList>()

    private lateinit var ingredientViewModel: IngredientViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            recipeId = it.getInt("recipe_id", -1)
        }
        Log.d("id", recipeId.toString())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val viewIngredients = inflater.inflate(R.layout.fragment_ingredients_list, container, false)
        tv_ingredients_list = viewIngredients.findViewById(R.id.ingredientsListLayout)
        ingredientViewModel = ViewModelProvider(requireActivity()).get(IngredientViewModel::class.java)

        val btnSave = viewIngredients.findViewById<ExtendedFloatingActionButton>(R.id.btnSave)
        btnSave.setOnClickListener {
            saveIngredientsToShoppingList()
            loadAllIngredients()
            updateIngredientsInDatabase()
            parentFragmentManager.popBackStack()
        }


        val btnCancel = viewIngredients.findViewById<ExtendedFloatingActionButton>(R.id.btnCancel)
        btnCancel.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        return viewIngredients
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recipeId = arguments?.getInt("recipe_id") ?: null
        loadIngredients(recipeId)
    }

    private fun loadIngredients(recipeId: Int?) {
        if (recipeId != null && recipeId != -1) {
            loadIngredientsForRecipe(recipeId)
        } else {
            loadAllIngredients()
        }
    }

    private fun loadIngredientsForRecipe(recipeId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val ingredients = db.daoRecipeIngredient.getFullIngredientsForRecipe(recipeId)

            withContext(Dispatchers.Main) {
                tv_ingredients_list.removeAllViews()

                if (ingredients.isNotEmpty()) {
                    for (ingredient in ingredients) {
                        addIngredientToUI(ingredient.ingredientName, ingredient.quantity.toString(), ingredient.unitName)
                    }
                } else {
                    addIngredientToUI("Nema sastojaka", "", "")
                }
            }
        }
    }

    private fun loadAllIngredients() {
        CoroutineScope(Dispatchers.IO).launch {
            val allIngredients = db.daoShoppingList.getFullShoppingList()

            withContext(Dispatchers.Main) {
                if (isAdded) {
                    tv_ingredients_list.removeAllViews()
                    ingredientsList.clear()

                    if (allIngredients.isNotEmpty()) {
                        allIngredients.forEach { ingredient ->
                            addIngredientToUI(ingredient.ingredientName, ingredient.quantity.toString(), ingredient.unitName)
                        }
                    } else {
                        val noIngredientsTextView = TextView(requireContext())
                        noIngredientsTextView.text = "Nema sastojaka"
                        tv_ingredients_list.addView(noIngredientsTextView)
                    }
                } else {
                    Log.d("IngredientsListFragment", "Fragment više nije povezan s aktivnošću.")
                }
            }
        }
    }

    private fun saveIngredientsToShoppingList() {
        recipeId?.let { id ->
            if (id != -1) {
                CoroutineScope(Dispatchers.IO).launch {
                    val ingredients = db.daoRecipeIngredient.getFullIngredientsForRecipe(id)

                    if (ingredients.isNotEmpty()) {
                        for (ingredient in ingredients) {
                            val ingredientId = db.daoIngredient.getIngredientIdByName(ingredient.ingredientName)
                            val unitId = db.daoUnits.getUnitIdByName(ingredient.unitName)

                            if (ingredientId != null && unitId != null) {
                                val existingShoppingListEntry = db.daoShoppingList.getShoppingListEntryByIngredientId(ingredientId)

                                if (existingShoppingListEntry != null) {
                                    if (existingShoppingListEntry.quantity != ingredient.quantity) {
                                        val updatedShoppingList = ShoppingList(
                                            id = existingShoppingListEntry.id,
                                            idIngredient = ingredientId,
                                            idUnit = unitId,
                                            quantity = ingredient.quantity
                                        )
                                        db.daoShoppingList.update(updatedShoppingList)
                                    }
                                } else {
                                    val shoppingListEntry = ShoppingList(
                                        idIngredient = ingredientId,
                                        idUnit = unitId,
                                        quantity = ingredient.quantity
                                    )
                                    db.daoShoppingList.insert(shoppingListEntry)
                                }
                            } else {
                                Log.e("ShoppingListError", "Nije moguće pronaći ID za sastojak: ${ingredient.ingredientName} ili jedinicu: ${ingredient.unitName}")
                            }
                        }

                        ingredientsList.clear()
                        deleteIngredientsFromDatabase()

                        withContext(Dispatchers.Main) {
                            Log.d("ShoppingList", "Sastojci su uspješno spremljeni.")
                        }
                    } else {
                        Log.d("IngredientsList", "Nema sastojaka za spremanje za recept ID: $id")
                    }
                }
            }
        }
    }


    private fun addIngredientToUI(name: String, quantity: String, unit: String) {
        val newIngredientView = LayoutInflater.from(requireContext())
            .inflate(R.layout.ingredient_shopping_list, null)

        val editQuantity = newIngredientView.findViewById<EditText>(R.id.txtQuantity)
        editQuantity.setText(quantity)

        val txtIngredientName = newIngredientView.findViewById<TextView>(R.id.txtIngredientName)
        txtIngredientName.text = name

        val txtUnit = newIngredientView.findViewById<TextView>(R.id.txtUnit)
        txtUnit.text = unit

        tv_ingredients_list.addView(newIngredientView)

        val newIngredient = ShoppingList(
            idIngredient = -1,
            idUnit = -1,
            quantity = quantity.toDoubleOrNull() ?: 0.0
        )
        ingredientsList.add(newIngredient)

        val btnDelete = newIngredientView.findViewById<Button>(R.id.btnDeleteIngredient)
        btnDelete.setOnClickListener {
            tv_ingredients_list.removeView(newIngredientView)
            ingredientsList.remove(newIngredient)
            ingredientsToDelete.add(name)

            CoroutineScope(Dispatchers.IO).launch {
                val ingredientId = db.daoIngredient.getIngredientIdByName(name)
                if (ingredientId != null) {
                    val shoppingListId = db.daoShoppingList.getShoppingListIdByIngredientId(ingredientId)
                    shoppingListId?.let { db.daoShoppingList.deleteById(it) }
                }
            }
        }

        editQuantity.addTextChangedListener {
            val updatedQuantity = it.toString().toDoubleOrNull() ?: 0.0
            val index = ingredientsList.indexOf(newIngredient)

            if (index != -1) {
                ingredientsList[index] = ingredientsList[index].copy(quantity = updatedQuantity)

                CoroutineScope(Dispatchers.IO).launch {
                    val ingredientId = db.daoIngredient.getIngredientIdByName(name)
                    val unitId = db.daoUnits.getUnitIdByName(unit)

                    if (ingredientId != null && unitId != null) {
                        val shoppingListEntry = ShoppingList(
                            id = ingredientsList[index].id,
                            idIngredient = ingredientId,
                            idUnit = unitId,
                            quantity = updatedQuantity
                        )

                        db.daoShoppingList.update(shoppingListEntry)

                        withContext(Dispatchers.Main) {
                            updateIngredientsInDatabase()
                            Log.d("IngredientsList", "Updated ingredient at index $index with quantity $updatedQuantity")
                        }
                    } else {
                        Log.e("IngredientsList", "Ingredient or unit not found in database")
                    }
                }
            } else {
                Log.e("IngredientsList", "Ingredient not found in list!")
            }
        }

    }

    private fun deleteIngredientsFromDatabase() {
        CoroutineScope(Dispatchers.IO).launch {
            ingredientsToDelete.forEach { ingredientName ->
                val ingredientId = db.daoIngredient.getIngredientIdByName(ingredientName)
                if (ingredientId != null) {
                    val shoppingListId = db.daoShoppingList.getShoppingListIdByIngredientId(ingredientId)
                    shoppingListId?.let { db.daoShoppingList.deleteById(it) }
                }
            }
            ingredientsToDelete.clear()
        }
    }

    private fun updateIngredientsInDatabase() {
        val count = tv_ingredients_list.childCount

        for (i in 0 until count) {
            val ingredientView = tv_ingredients_list.getChildAt(i)

            val ingredientName = ingredientView.findViewById<TextView>(R.id.txtIngredientName).text.toString()
            val unitName = ingredientView.findViewById<TextView>(R.id.txtUnit).text.toString()
            val newQuantityText = ingredientView.findViewById<EditText>(R.id.txtQuantity).text.toString()

            val newQuantity = newQuantityText.toDoubleOrNull()

            if (newQuantity != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    val ingredientId = db.daoIngredient.getIngredientIdByName(ingredientName)

                    val unitId = db.daoUnits.getUnitIdByName(unitName)

                    if (ingredientId != null && unitId != null) {
                        val shoppingListId = db.daoShoppingList.getShoppingListIdByIngredientId(ingredientId)

                        if (shoppingListId != null) {
                            val updatedShoppingList = ShoppingList(
                                id = shoppingListId,
                                idIngredient = ingredientId,
                                idUnit = unitId,
                                quantity = newQuantity
                            )

                            db.daoShoppingList.update(updatedShoppingList)

                            withContext(Dispatchers.Main) {
                                Log.d("UpdateIngredients", "Ažuriran sastojak: $ingredientName sa novom količinom: $newQuantity")
                            }
                        } else {
                            Log.e("UpdateIngredients", "Nije moguće pronaći shopping list ID za sastojak: $ingredientName")
                        }
                    } else {
                        Log.e("UpdateIngredients", "Nije moguće pronaći ID za sastojak: $ingredientName ili jedinicu: $unitName")
                    }
                }
            }
        }
    }
}
