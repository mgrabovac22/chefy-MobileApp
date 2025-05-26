package hr.foi.rampu.chefy.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import hr.foi.rampu.chefy.models.recipe.Recipes
import hr.foi.rampu.chefy.models.recipe.ShoppingList

@Dao
interface ShoppingListDAO {

    @Insert(entity = ShoppingList::class)
    fun insert(shoppingList: ShoppingList)

    @Delete
    fun delete(shoppingList: ShoppingList)

    @Query("SELECT COUNT(*) FROM shopping_list")
    fun getShoppingListCount(): Int

    @Update
    fun update(shoppingList: ShoppingList)

    @Query("""
    SELECT i.name AS ingredientName, sl.quantity, u.name AS unitName
    FROM shopping_list sl
    JOIN ingredients i ON sl.idIngredient = i.id
    JOIN units u ON sl.idUnit = u.id
    """)
    fun getFullShoppingList(): List<MyIngredientUnitQuantity>

    @Query("SELECT id FROM shopping_list WHERE idIngredient = :ingredientId")
    fun getShoppingListIdByIngredientId(ingredientId: Int): Int?

    @Query("DELETE FROM shopping_list WHERE id = :shoppingListId")
    fun deleteById(shoppingListId: Int)

    @Query("SELECT * FROM shopping_list WHERE idIngredient = :ingredientId LIMIT 1")
    suspend fun getShoppingListEntryByIngredientId(ingredientId: Int): ShoppingList?
    
    data class MyIngredientUnitQuantity(
        val ingredientName: String,
        val quantity: Double,
        val unitName: String
    )
}
