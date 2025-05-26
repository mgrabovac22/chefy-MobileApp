package hr.foi.rampu.chefy.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import hr.foi.rampu.chefy.models.recipe.MyRecipeIngredients

@Dao
interface MyRecipeIngredientsDAO {

    @Insert
    fun insert(recipeIngredient: MyRecipeIngredients)

    @Query("SELECT * FROM my_recipe_ingredients WHERE idRecipe = :recipeId")
    fun getIngredientsForRecipe(recipeId: Int): List<MyRecipeIngredients>

    @Delete
    fun delete(recipeIngredient: MyRecipeIngredients)

    @Query("SELECT COUNT(*) FROM my_recipe_ingredients")
    fun getRecIngCount(): Int

    @Update
    fun update(recipeIngredient: MyRecipeIngredients)

    @Query("""
    SELECT i.name AS ingredientName, ri.quantity, u.name AS unitName
    FROM my_recipe_ingredients ri
    JOIN ingredients i ON ri.idIngredient = i.id
    JOIN units u ON ri.idUnit = u.id
    WHERE ri.idRecipe = :recipeId """)
    fun getFullIngredientsForRecipe(recipeId: Int): List<MyIngredientUnitQuantity>

    data class MyIngredientUnitQuantity(
        val ingredientName: String,
        val quantity: Double,
        val unitName: String
    )
}


