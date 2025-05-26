package hr.foi.rampu.chefy.models.recipe

import androidx.room.Embedded
import androidx.room.Relation

data class RecipeWithSteps(
    @Embedded val recipe: Recipes,
    @Relation(
        parentColumn = "id",
        entityColumn = "idRecipe"
    )
    val steps: List<Steps>
)
