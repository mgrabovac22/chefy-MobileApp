package hr.foi.rampu.chefy.models.recipe

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "my_recipes")
data class MyRecipes(
    @PrimaryKey(autoGenerate = false)
    val id: Int? = null,
    val name: String,
    val description: String,
    val makingTime: Long,
    val picturePaths: String,
    val category: String,
    var id_user : Int,
    var remoteId : Int?
)
