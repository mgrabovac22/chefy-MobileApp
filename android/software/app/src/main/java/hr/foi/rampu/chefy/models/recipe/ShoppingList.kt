package hr.foi.rampu.chefy.models.recipe

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "shopping_list",
    foreignKeys = [
        ForeignKey(
            entity = Ingredients::class,
            parentColumns = ["id"],
            childColumns = ["idIngredient"],
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
data class ShoppingList(
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
    val idIngredient: Int,
    val idUnit: Int,
    val quantity: Double
)
