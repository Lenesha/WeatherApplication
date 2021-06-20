package com.weatherforecast.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
/**
 * Data Access Object for the show table.
 */
@Dao
interface WeatherDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun updateForecast(data: ForecastTable)

    @Query("SELECT * FROM forecast_info")
    fun getForecast(): ForecastTable

    @Query("DELETE FROM forecast_info")
    fun delete()
}