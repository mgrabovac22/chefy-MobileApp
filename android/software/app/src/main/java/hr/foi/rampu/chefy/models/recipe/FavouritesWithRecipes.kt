package hr.foi.rampu.chefy.models.recipe

import androidx.room.Embedded
import androidx.room.Relation

data class FavouritesWithRecipes(
    @Embedded val favourite: Favourites,
    @Relation(
        parentColumn = "idRecipe",
        entityColumn = "id"
    )
    val recipe: Recipes
)
