package hr.foi.rampu.chefy

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.floatingactionbutton.FloatingActionButton
import hr.foi.rampu.chefy.activities.StepsActivity
import hr.foi.rampu.chefy.data.db.RecipesDatabase
import hr.foi.rampu.chefy.fragments.IngredientsListFragment
import hr.foi.rampu.chefy.fragments.SecondStepAddRecipeFragment
import hr.foi.rampu.chefy.models.recipe.Favourites
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetailRecipeFragment : Fragment() {

    private lateinit var recipeNameTextView: TextView
    private lateinit var recipePictureImageView: ImageView
    private lateinit var recipeDescription: TextView
    private lateinit var tv_detail_ingredients: TextView
    private lateinit var boxImageView: ImageView
    private lateinit var authorRecipe: TextView
    private lateinit var imageAuthorView: ImageView

    private var recipeId: Int? = null
    private var recipeName: String? = null
    private var recipeDesc: String? = null

    private val db: RecipesDatabase by lazy { RecipesDatabase.getInstance() }
    private var isFavorite: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            recipeId = it.getInt("recipe_id", -1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val viewRecipe = inflater.inflate(R.layout.fragment_detail_recipe, container, false)
        recipeNameTextView = viewRecipe.findViewById(R.id.tv_detail_recipe_name)
        recipePictureImageView = viewRecipe.findViewById(R.id.tv_detail_recipe_picture)
        recipeDescription = viewRecipe.findViewById(R.id.tv_description)
        tv_detail_ingredients = viewRecipe.findViewById(R.id.tv_detail_ingredients)
        boxImageView = viewRecipe.findViewById(R.id.boxImageView)
        authorRecipe = viewRecipe.findViewById(R.id.tv_author)
        imageAuthorView = viewRecipe.findViewById(R.id.imvAuthorPicture)

        val backButton: Button = viewRecipe.findViewById(R.id.back_button)
        val ingredientsListButton: Button = viewRecipe.findViewById(R.id.fab_list)

        recipeNameTextView.text = recipeName


        authorRecipe.text = "Kuhar Cheffy";

        ingredientsListButton.setOnClickListener{
            val fragment = IngredientsListFragment()

            val bundle = Bundle()
            bundle.putInt("recipe_id", recipeId ?: -1)

            fragment.arguments = bundle

            val transaction: FragmentTransaction = parentFragmentManager.beginTransaction()
            transaction.replace(R.id.frame_layout, fragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }

        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        boxImageView.setOnClickListener {
            toggleFavoriteStatus()
        }

        return viewRecipe
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val button: FloatingActionButton = view.findViewById(R.id.btn_preparation)
        button.setOnClickListener {
            openStepsActivity()
        }

        loadRecipeData()
        loadIngredients()
        checkIfRecipeIsFavorite()


        imageAuthorView.setImageResource(R.drawable.logo)
        authorRecipe.text = "Kuhar Cheffy"
    }

    private fun loadRecipeData() {
        recipeId?.let { id ->
            if (id != -1) {
                CoroutineScope(Dispatchers.IO).launch {
                    val recipe = db.daoRec.getOneRecipe(id)
                    withContext(Dispatchers.Main) {
                        recipe?.let {
                            recipeNameTextView.text = it.name
                            recipeDescription.text = it.description

                            if (!it.picturePaths.isNullOrEmpty()) {
                                val assetManager = requireContext().assets
                                val imageStream = assetManager.open("${it.picturePaths}")
                                val drawable = Drawable.createFromStream(imageStream, null)
                                recipePictureImageView.setImageDrawable(drawable)
                            } else {
                                recipePictureImageView.setImageResource(R.drawable.food_picture)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun loadIngredients() {
        recipeId?.let { id ->
            if (id != -1) {
                CoroutineScope(Dispatchers.IO).launch {
                    val ingredients = db.daoRecipeIngredient.getFullIngredientsForRecipe(id)
                    withContext(Dispatchers.Main) {
                        if (ingredients.isNotEmpty()) {
                            val ingredientText = ingredients.joinToString("\n") {
                                "- ${it.quantity} ${it.unitName} ${it.ingredientName}"
                            }
                            tv_detail_ingredients.text = ingredientText
                        } else {
                            tv_detail_ingredients.text = "Nema sastojaka"
                        }
                    }
                }
            }
        }
    }

    private fun checkIfRecipeIsFavorite() {
        recipeId?.let { id ->
            CoroutineScope(Dispatchers.IO).launch {
                val favoriteRecipes = db.daoFavourites.getAllFavourites()
                isFavorite = favoriteRecipes.any { it.id == id }

                withContext(Dispatchers.Main) {
                    updateFavoriteIcon()
                }
            }
        }
    }

    private fun toggleFavoriteStatus() {
        recipeId?.let { id ->
            CoroutineScope(Dispatchers.IO).launch {
                val existingFavorites = db.daoFavourites.getAllFavourites()

                if (existingFavorites.any { it.id == id }) {

                    db.daoFavourites.deleteFavourite(Favourites(idRecipe = id))
                    isFavorite = false
                    withContext(Dispatchers.Main) {
                      //  updateFavoriteIcon()
                    }
                } else {

                    db.daoFavourites.insertFavourites(Favourites(idRecipe = id))
                    isFavorite = true
                    withContext(Dispatchers.Main) {
                        updateFavoriteIcon()
                    }
                }
            }
        }
    }

    private fun updateFavoriteIcon() {
        if (isFavorite) {
            boxImageView.setImageResource(R.drawable.box_full)
        } else {
            boxImageView.setImageResource(R.drawable.box)
        }
    }

    private fun openStepsActivity() {
        val intent = Intent(requireActivity(), StepsActivity::class.java).apply {
            putExtra("recipe_name", recipeName)
            putExtra("recipe_description", recipeDesc)
            putExtra("recipe_id", recipeId)
        }
        startActivity(intent)
    }

    companion object {
        fun newInstance(recipeId: Int, recipeName: String, recipePicture: String, recipeAuthor: String, recipeDesc: String) =
            DetailRecipeFragment().apply {
                arguments = Bundle().apply {
                    putInt("recipe_id", recipeId)
                    putString("recipe_name", recipeName)
                    putString("recipe_picture", recipePicture)
                    putString("recipe_desc", recipeDesc)
                }
            }
    }
}
