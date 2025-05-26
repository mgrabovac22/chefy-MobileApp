package hr.foi.rampu.chefy.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import hr.foi.rampu.chefy.models.recipe.MyRecipes
import hr.foi.rampu.chefy.models.recipe.MySteps

@Dao
interface MyRecipesDAO {

    @Insert(entity = MyRecipes::class)
    fun insertRecipes(recipe: MyRecipes): Long

    @Query("SELECT * FROM my_recipes")
    fun getRecipes(): List<MyRecipes>

    @Query("SELECT * FROM my_recipes WHERE id_user= :id_user")
    fun getRecipesByUserId(id_user: Int): List<MyRecipes>

    @Query("SELECT * FROM my_recipes WHERE id= :id")
    fun getOneRecipe(id: Int): MyRecipes

    @Query("SELECT COUNT(*) FROM my_recipes")
    fun getRecipesCount(): Int

    @Update
    fun updateRecipe(recipe: MyRecipes)

    @Delete
    fun deleteRecipe(recipe: MyRecipes)

    @Query("SELECT * FROM my_steps WHERE idRecipe = :recipeId ORDER BY stage ASC")
    fun getStepsForRecipeSorted(recipeId: Int): List<MySteps>

    @Query("SELECT MAX(id) FROM my_recipes")
    fun getMaxRecipeId(): Int?

}