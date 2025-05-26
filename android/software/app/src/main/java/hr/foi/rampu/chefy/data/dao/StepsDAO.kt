package hr.foi.rampu.chefy.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import hr.foi.rampu.chefy.models.recipe.Steps


@Dao
interface StepsDAO {

    @Insert(entity = Steps::class)
    fun insertSteps(steps: Steps): Long

    @Query("SELECT * FROM steps")
    fun getSteps(): List<Steps>

    @Query("SELECT * FROM steps WHERE id= :id")
    fun getOneStep(id: Int): Steps

    @Query("SELECT COUNT(*) FROM steps")
    fun getCountSteps(): Int

    @Update
    fun updateSteps(steps: Steps)

    @Delete
    fun deleteSteps(steps: Steps)
}