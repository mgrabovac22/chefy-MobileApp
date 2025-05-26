package hr.foi.rampu.chefy.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import hr.foi.rampu.chefy.models.recipe.RecipeWithSteps
import hr.foi.rampu.chefy.models.recipe.Recipes
import hr.foi.rampu.chefy.models.recipe.Steps

@Dao
interface RecipesDAO {
    @Insert(entity = Recipes::class)
    fun insertRecipes(recipe: Recipes): Long

    @Query("SELECT * FROM recipes")
    fun getRecipes(): List<Recipes>

    @Query("SELECT * FROM recipes WHERE id= :id")
    fun getOneRecipe(id: Int): Recipes

    @Query("SELECT COUNT(*) FROM recipes")
    fun getRecipesCount(): Int

    @Update
    fun updateRecipe(recipe: Recipes)

    @Delete
    fun deleteRecipe(recipe: Recipes)

    @Query("SELECT * FROM recipes WHERE id = :recipeId")
    fun getRecipeWithSteps(recipeId: Int): RecipeWithSteps

    @Query("SELECT * FROM steps WHERE idRecipe = :recipeId ORDER BY stage ASC")
    fun getStepsForRecipeSorted(recipeId: Int): List<Steps>

    @Query("SELECT * FROM recipes")
    fun getAllRecipesWithSteps(): List<RecipeWithSteps>


}