package hr.foi.rampu.chefy.models.recipe

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipes")
data class Recipes(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val name: String,
    val description: String,
    val makingTime: Long,
    val picturePaths: String,
    val category: String
)
