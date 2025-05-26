package hr.foi.rampu.chefy.models.recipe

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "my_steps",
    foreignKeys = [
        ForeignKey(
            entity = MyRecipes::class,
            parentColumns = ["id"],
            childColumns = ["idRecipe"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class MySteps(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val name: String,
    val description: String,
    val stage: Int,
    val makingTime: Int,
    val imagePath: String,
    val idRecipe: Int
)
