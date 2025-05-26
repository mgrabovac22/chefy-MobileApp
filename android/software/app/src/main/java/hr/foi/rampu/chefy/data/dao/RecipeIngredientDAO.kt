package hr.foi.rampu.chefy.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import hr.foi.rampu.chefy.models.recipe.RecipeIngredient

@Dao
interface RecipeIngredientDAO {

    @Insert
    fun insert(recipeIngredient: RecipeIngredient)

    @Query("SELECT * FROM recipe_ingredients WHERE idRecipe = :recipeId")
    fun getIngredientsForRecipe(recipeId: Int): List<RecipeIngredient>

    @Delete
    fun delete(recipeIngredient: RecipeIngredient)

    @Query("SELECT COUNT(*) FROM recipe_ingredients")
    fun getRecIngCount(): Int

    @Query("""
    SELECT i.name AS ingredientName, ri.quantity, u.name AS unitName
    FROM recipe_ingredients ri
    JOIN ingredients i ON ri.idIngredient = i.id
    JOIN units u ON ri.idUnit = u.id
    WHERE ri.idRecipe = :recipeId """)
    fun getFullIngredientsForRecipe(recipeId: Int): List<IngredientUnitQuantity>

    data class IngredientUnitQuantity(
        val ingredientName: String,
        val quantity: Double,
        val unitName: String
    )
}


