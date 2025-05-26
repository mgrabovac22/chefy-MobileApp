package hr.foi.rampu.chefy.models.recipe

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "favourites",
    foreignKeys = [
        ForeignKey(
            entity = Recipes::class,
            parentColumns = ["id"],
            childColumns = ["idRecipe"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Favourites(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val idRecipe: Int
)

