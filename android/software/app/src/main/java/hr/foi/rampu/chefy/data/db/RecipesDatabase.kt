package hr.foi.rampu.chefy.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import hr.foi.rampu.chefy.data.dao.FavouritesDAO
import hr.foi.rampu.chefy.data.dao.IngredientsDAO
import hr.foi.rampu.chefy.data.dao.MyRecipeIngredientsDAO
import hr.foi.rampu.chefy.data.dao.MyRecipesDAO
import hr.foi.rampu.chefy.data.dao.MyStepsDAO
import hr.foi.rampu.chefy.data.dao.RecipeIngredientDAO
import hr.foi.rampu.chefy.data.dao.RecipesDAO
import hr.foi.rampu.chefy.data.dao.ShoppingListDAO
import hr.foi.rampu.chefy.data.dao.StepsDAO
import hr.foi.rampu.chefy.data.dao.UnitsDAO
import hr.foi.rampu.chefy.models.recipe.Favourites
import hr.foi.rampu.chefy.models.recipe.Ingredients
import hr.foi.rampu.chefy.models.recipe.MyRecipeIngredients
import hr.foi.rampu.chefy.models.recipe.MyRecipes
import hr.foi.rampu.chefy.models.recipe.MySteps
import hr.foi.rampu.chefy.models.recipe.RecipeIngredient
import hr.foi.rampu.chefy.models.recipe.Recipes
import hr.foi.rampu.chefy.models.recipe.ShoppingList
import hr.foi.rampu.chefy.models.recipe.Steps
import hr.foi.rampu.chefy.models.recipe.Units

@Database(entities = [Recipes::class, Steps::class, Ingredients::class, Units::class, RecipeIngredient::class, Favourites::class, MyRecipeIngredients::class, MySteps::class, MyRecipes::class, ShoppingList::class], version = 10, exportSchema = false)
abstract class RecipesDatabase : RoomDatabase() {
    abstract val daoRec: RecipesDAO
    abstract val daoSteps: StepsDAO
    abstract val daoUnits: UnitsDAO
    abstract val daoIngredient: IngredientsDAO
    abstract val daoRecipeIngredient: RecipeIngredientDAO
    abstract val daoFavourites: FavouritesDAO
    abstract val daoMyRecipeIngredients: MyRecipeIngredientsDAO
    abstract val daoMyRecipes: MyRecipesDAO
    abstract val daoMySteps: MyStepsDAO
    abstract val daoShoppingList: ShoppingListDAO
    companion object {
        @Volatile
        private var implementedInstance: RecipesDatabase? = null

        fun getInstance(): RecipesDatabase {
            return implementedInstance ?: throw NullPointerException("Database instance has not yet been created!")
        }

        fun buildInstance(context: Context) {
            if (implementedInstance == null) {
                synchronized(this) {
                    implementedInstance = Room.databaseBuilder(
                        context.applicationContext,
                        RecipesDatabase::class.java,
                        "recipes.db"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }
        }
    }
}