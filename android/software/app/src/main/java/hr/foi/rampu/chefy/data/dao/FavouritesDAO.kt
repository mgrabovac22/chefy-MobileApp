package hr.foi.rampu.chefy.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import hr.foi.rampu.chefy.models.recipe.Favourites
import hr.foi.rampu.chefy.models.recipe.FavouritesWithRecipes
import hr.foi.rampu.chefy.models.recipe.Recipes

@Dao
interface FavouritesDAO {

    @Transaction
    @Query("SELECT * FROM favourites")
    fun getAllFavouritesWithRecipes(): List<FavouritesWithRecipes>

    @Query("SELECT recipes.* FROM recipes, favourites where favourites.idRecipe == recipes.id")
    fun getAllFavourites(): List<Recipes>

    @Insert
    fun insertFavourites(favourite: Favourites)
    @Delete
    fun deleteFavourite(favourite: Favourites)

}