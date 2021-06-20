package com.weatherforecast.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters


@Database(entities = [ForecastTable::class,LocationTable::class], version = 1, exportSchema = false)
@TypeConverters(CustomTypeConverter::class)
abstract class AppDataBase : RoomDatabase() {
    abstract val weatherDao: WeatherDao
    abstract val locationDao: LocationDao

}