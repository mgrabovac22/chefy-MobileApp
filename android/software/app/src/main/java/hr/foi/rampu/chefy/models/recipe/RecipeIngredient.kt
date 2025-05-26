package hr.foi.rampu.chefy.models.recipe

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "recipe_ingredients",
    foreignKeys = [
        ForeignKey(
            entity = Ingredients::class,
            parentColumns = ["id"],
            childColumns = ["idIngredient"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Recipes::class,
            parentColumns = ["id"],
            childColumns = ["idRecipe"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Units::class,
            parentColumns = ["id"],
            childColumns = ["idUnit"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class RecipeIngredient(
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
    val idIngredient: Int,
    val idRecipe: Int,
    val idUnit: Int,
    val quantity: Double
)
