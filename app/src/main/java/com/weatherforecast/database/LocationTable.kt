package com.weatherforecast.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "location")
data class LocationTable(
        @ColumnInfo(name = "latitude") val latitude: Double,
        @ColumnInfo(name = "longitude") val longitude: Double,
       @PrimaryKey @ColumnInfo(name = "timestamp") val timestamp: Long
)