package hr.foi.rampu.chefy.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import hr.foi.rampu.chefy.models.recipe.MySteps

@Dao
interface MyStepsDAO {

    @Insert(entity = MySteps::class)
    fun insertSteps(steps: MySteps): Long

    @Query("SELECT * FROM my_steps")
    fun getSteps(): List<MySteps>

    @Query("SELECT * FROM my_steps WHERE idRecipe= :id")
    fun getOneStep(id: Int): List<MySteps>

    @Query("SELECT COUNT(*) FROM my_steps")
    fun getCountSteps(): Int

    @Update
    fun updateSteps(steps: MySteps)

    @Delete
    fun deleteSteps(steps: MySteps)
}