package hr.foi.rampu.chefy.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import hr.foi.rampu.chefy.models.recipe.Ingredients

@Dao
interface IngredientsDAO {
    @Insert
    fun insertIngredient(ingredient: Ingredients)

    @Insert
    fun insertIngredients(ingredients: List<Ingredients>)

    @Query("SELECT * FROM ingredients")
    fun getAllIngredients(): List<Ingredients>

    @Query("SELECT * FROM ingredients WHERE id = :ingredientId")
    fun getIngredientById(ingredientId: Int): Ingredients?

    @Query("SELECT * FROM ingredients WHERE name = :name")
    fun getIngredientByName(name: String): Ingredients?

    @Update
    fun updateIngredient(ingredient: Ingredients)

    @Query("DELETE FROM ingredients WHERE id = :ingredientId")
    fun deleteIngredientById(ingredientId: Int)

    @Query("DELETE FROM ingredients")
    fun deleteAllIngredients()

    @Query("SELECT COUNT(*) FROM ingredients")
    fun getIngCount(): Int

    @Query("SELECT id FROM ingredients WHERE name = :ingredientName")
    fun getIngredientIdByName(ingredientName: String): Int?
}