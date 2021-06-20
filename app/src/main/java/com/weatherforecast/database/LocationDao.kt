package com.weatherforecast.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface LocationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(location: LocationTable): Long

    @Query("SELECT * FROM location ORDER BY timestamp DESC LIMIT 1")
    fun query(): LocationTable


    @Query("DELETE FROM location")
    fun deleteAll(): Int
}