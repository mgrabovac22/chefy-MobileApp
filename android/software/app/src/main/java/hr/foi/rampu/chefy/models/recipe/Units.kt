package hr.foi.rampu.chefy.models.recipe

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "units")
data class Units(
    @PrimaryKey(autoGenerate = false)
    val id: Int? = null,
    val name: String
)
