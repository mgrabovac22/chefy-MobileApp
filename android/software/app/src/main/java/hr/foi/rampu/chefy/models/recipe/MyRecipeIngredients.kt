package hr.foi.rampu.chefy.models.recipe

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "my_recipe_ingredients",
    foreignKeys = [
        ForeignKey(
            entity = Ingredients::class,
            parentColumns = ["id"],
            childColumns = ["idIngredient"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = MyRecipes::class,
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
data class MyRecipeIngredients(
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
    val idIngredient: Int,
    val idRecipe: Int,
    val idUnit: Int,
    val quantity: Double
)