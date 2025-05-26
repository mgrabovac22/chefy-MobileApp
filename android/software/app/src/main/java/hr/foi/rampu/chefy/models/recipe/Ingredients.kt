package hr.foi.rampu.chefy.models.recipe

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ingredients")
data class Ingredients(
    @PrimaryKey(autoGenerate = false)
    val id: Int? = null,
    val name: String
)
