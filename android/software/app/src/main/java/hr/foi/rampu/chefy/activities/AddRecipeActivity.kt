package hr.foi.rampu.chefy.activities

import ProcedureStepViewModel
import RecipeViewModel
import StepViewModel
import android.content.Context
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import hr.foi.rampu.chefy.R
import hr.foi.rampu.chefy.models.recipe.MyRecipeIngredients
import hr.foi.rampu.chefy.models.recipe.MyRecipes
import hr.foi.rampu.chefy.models.recipe.MySteps
import hr.foi.rampu.chefy.data.db.RecipesDatabase
import hr.foi.rampu.chefy.fragments.FirstStepAddRecipeFragment
import hr.foi.rampu.chefy.fragments.ForthStepAddRecipeFragment
import hr.foi.rampu.chefy.viewmodels.IngredientViewModel
import hr.foi.rampu.chefy.viewmodels.StepFinishViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddRecipeActivity : AppCompatActivity(), ForthStepAddRecipeFragment.SaveRecipeListener {

    private val recipeViewModel: RecipeViewModel by viewModels()
    private val stepViewModel: StepViewModel by viewModels()
    private val stepViewModelProcedure: ProcedureStepViewModel by viewModels()
    private val stepViewModelFinish: StepFinishViewModel by viewModels()
    private val ingredientViewModel: IngredientViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_recipe)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnCancel: Button = findViewById(R.id.btnCancel)
        btnCancel.setOnClickListener {
            finish()
        }

        RecipesDatabase.buildInstance(applicationContext)

        val recipeId = intent.getIntExtra("RECIPE_ID", -1)

        if (recipeId != -1) {
            CoroutineScope(Dispatchers.IO).launch {
                loadRecipeData(recipeId)

                withContext(Dispatchers.Main) {
                    initializeFragment(recipeId)
                }
            }
        } else {
            CoroutineScope(Dispatchers.IO).launch {
                val db = RecipesDatabase.getInstance()
                val maxId = db.daoMyRecipes.getMaxRecipeId() ?: 0
                val nextId = maxId + 1

                withContext(Dispatchers.Main) {
                    recipeViewModel.setRecipeId(nextId)
                    initializeFragment(nextId)
                }
            }
        }
    }

    override fun onSaveRecipe() {
        saveRecipeToDatabase()
    }

    private fun initializeFragment(recipeId: Int) {
        val fragment = FirstStepAddRecipeFragment().apply {
            arguments = Bundle().apply {
                putInt("idRecipe", recipeId)
            }
        }

        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }


    private suspend fun loadRecipeData(recipeId: Int) {
        val db = RecipesDatabase.getInstance()
        val recipe = db.daoMyRecipes.getOneRecipe(recipeId)
        val recipeSteps = db.daoMySteps.getOneStep(recipeId)
        val recipeIngredients = db.daoMyRecipeIngredients.getIngredientsForRecipe(recipeId)

        withContext(Dispatchers.Main) {
            if (recipe != null) {
                recipeViewModel.setRecipeId(recipeId)

                recipeViewModel.setRecipeName(recipe.name)

                recipeViewModel.setDescription(recipe.description)

                recipeViewModel.setMakingTime(recipe.makingTime.toString())

                recipeViewModel.setPicturePath(recipe.picturePaths)

                recipeViewModel.setCategory(recipe.category)

                recipeSteps.forEachIndexed { index, recipeStep ->
                    when (index) {
                        0 -> {
                            stepViewModel.setMakingTimePreparation(recipeStep.makingTime)

                            stepViewModel.setPictureDataString(recipeStep.imagePath)

                            stepViewModel.setDescriptionPreparation(recipeStep.description)
                        }
                        1 -> {
                            stepViewModelProcedure.setMakingTimeProcedure(recipeStep.makingTime)

                            stepViewModelProcedure.setPictureDataString(recipeStep.imagePath)

                            stepViewModelProcedure.setDescriptionProcedure(recipeStep.description)
                        }
                        2 -> {
                            stepViewModelFinish.setMakingTimeFinish(recipeStep.makingTime)

                            stepViewModelFinish.setPictureDataString(recipeStep.imagePath)

                            stepViewModelFinish.setDescriptionFinish(recipeStep.description)
                        }
                    }
                }

                recipeIngredients.forEach { recipeIngredient ->
                    ingredientViewModel.setIngredient(
                        recipeIngredient.idIngredient,
                        recipeIngredient.quantity.toString(),
                        recipeIngredient.idUnit
                    )
                }
            }
        }
    }


    fun saveRecipeToDatabase() {
        val id = recipeViewModel.getRecipeId()
        val recipeName = recipeViewModel.getRecipeName()
        val recipeDescription = recipeViewModel.getDescription()
        val makingTime = recipeViewModel.getMakingTime()
        val picturePath = recipeViewModel.getPicturePath() ?: ""
        val category = recipeViewModel.getCategory()

        CoroutineScope(Dispatchers.IO).launch {
            val db = RecipesDatabase.getInstance()
            val myRecipesDAO = db.daoMyRecipes
            val myRecipeIngredientsDAO = db.daoMyRecipeIngredients
            val myStepsDAO = db.daoMySteps

            val existingRecipe = myRecipesDAO.getOneRecipe(id)


            val sharedPreferences = getSharedPreferences("user_session", Context.MODE_PRIVATE)
            var user_id_string = sharedPreferences.getString("user_id", null)

            val recipe = MyRecipes(
                id = id,
                name = recipeName,
                description = recipeDescription,
                makingTime = makingTime.toLongOrNull() ?: 0L,
                picturePaths = picturePath,
                category = category,
                id_user = user_id_string?.toInt()!!,
                remoteId = null
            )

            if (existingRecipe != null) {
                myRecipesDAO.updateRecipe(recipe)
            } else {
                myRecipesDAO.insertRecipes(recipe)
            }

            val existingIngredients = myRecipeIngredientsDAO.getIngredientsForRecipe(id)
            existingIngredients.forEach { existingIngredient ->
                myRecipeIngredientsDAO.delete(existingIngredient)
            }

            val ingredients = ingredientViewModel.getIngredients()
            ingredients.forEach { ingredient ->
                val ingredientId = ingredient[0] as Int
                val quantity = ingredient[1] as String
                val unitId = ingredient[2] as Int

                val recipeIngredient = MyRecipeIngredients(
                    idRecipe = id,
                    idIngredient = ingredientId,
                    idUnit = unitId,
                    quantity = quantity.toDoubleOrNull() ?: 0.0
                )

                myRecipeIngredientsDAO.insert(recipeIngredient)
            }

            val stepDescriptionPreparation = stepViewModel.getDescriptionPreparation()
            val makingTimePreparation = stepViewModel.getMakingTimePreparation()
            val imagePathPreparation = stepViewModel.getImagePathPreparation()

            val stepPreparation = MySteps(
                name = "Priprema",
                description = stepDescriptionPreparation,
                stage = 1,
                makingTime = makingTimePreparation,
                imagePath = imagePathPreparation ?: "",
                idRecipe = id
            )

            val existingStepPreparation = myStepsDAO.getOneStep(id).find { it.stage == 1 }
            if (existingStepPreparation != null) {
                val updatedStep = existingStepPreparation.copy(
                    description = stepDescriptionPreparation,
                    makingTime = makingTimePreparation,
                    imagePath = imagePathPreparation ?: ""
                )
                myStepsDAO.updateSteps(updatedStep)
            } else {
                myStepsDAO.insertSteps(stepPreparation)
            }

            val stepDescriptionProcedure = stepViewModelProcedure.getDescriptionProcedure()
            val makingTimeProcedure = stepViewModelProcedure.getMakingTimeProcedure()
            val imagePathProcedure = stepViewModelProcedure.getImagePathProcedure()

            val stepProcedure = MySteps(
                name = "Postupak",
                description = stepDescriptionProcedure,
                stage = 2,
                makingTime = makingTimeProcedure,
                imagePath = imagePathProcedure ?: "",
                idRecipe = id
            )

            val existingStepProcedure = myStepsDAO.getOneStep(id).find { it.stage == 2 }
            if (existingStepProcedure != null) {
                val updatedStep = existingStepProcedure.copy(
                    description = stepDescriptionPreparation,
                    makingTime = makingTimePreparation,
                    imagePath = imagePathPreparation ?: ""
                )
                myStepsDAO.updateSteps(updatedStep)
            } else {
                myStepsDAO.insertSteps(stepProcedure)
            }

            val stepDescriptionFinish = stepViewModelFinish.getDescriptionFinish()
            val makingTimeFinish = stepViewModelFinish.getMakingTimeFinish()
            val imagePathFinish = stepViewModelFinish.getImagePathFinish()

            val stepFinish = MySteps(
                name = "Završetak",
                description = stepDescriptionFinish,
                stage = 3,
                makingTime = makingTimeFinish,
                imagePath = imagePathFinish ?: "",
                idRecipe = id
            )

            val existingStepFinish = myStepsDAO.getOneStep(id).find { it.stage == 3 }
            if (existingStepFinish != null) {
                val updatedStep = existingStepFinish.copy(
                    description = stepDescriptionFinish,
                    makingTime = makingTimeFinish,
                    imagePath = imagePathFinish ?: ""
                )
                myStepsDAO.updateSteps(updatedStep)
            } else {
                myStepsDAO.insertSteps(stepFinish)
            }

                withContext(Dispatchers.Main) {
                finish()
            }
        }
    }

}
