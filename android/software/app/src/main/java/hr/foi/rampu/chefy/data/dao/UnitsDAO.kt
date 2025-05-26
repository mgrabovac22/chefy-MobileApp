package hr.foi.rampu.chefy.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import hr.foi.rampu.chefy.models.recipe.Units

@Dao
interface UnitsDAO {

    @Insert
    fun insertUnit(unit: Units)

    @Insert
    fun insertUnits(units: List<Units>)

    @Query("SELECT * FROM units")
    fun getAllUnits(): List<Units>

    @Query("SELECT * FROM units WHERE id = :unitId")
    fun getUnitById(unitId: Int): Units?

    @Query("SELECT * FROM units WHERE name = :name")
    fun getUnitByName(name: String): Units?

    @Update
    fun updateUnit(unit: Units)

    @Query("DELETE FROM units WHERE id = :unitId")
    fun deleteUnitById(unitId: Int)

    @Query("DELETE FROM units")
    fun deleteAllUnits()

    @Query("SELECT COUNT(*) FROM units")
    fun getUnitsCount(): Int

    @Query("SELECT id FROM units WHERE name = :unitName")
    fun getUnitIdByName(unitName: String): Int?
}