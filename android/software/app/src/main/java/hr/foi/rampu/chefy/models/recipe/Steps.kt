package hr.foi.rampu.chefy.models.recipe

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "steps")
data class Steps(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val name: String,
    val description: String,
    val stage: Int,
    val makingTime: Int,
    val imagePath: String,
    val idRecipe: Int
)
