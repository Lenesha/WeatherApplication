package com.weatherforecast.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class CustomTypeConverter {

    @TypeConverter
    fun stringToSomeObjectList(data: String?): List<ForecastTable.Weather> {
        if (data == null) {
            return emptyList()
        }
        val listType = object : TypeToken<List<ForecastTable.Weather?>?>() {}.type
        return Gson().fromJson(data, listType)
    }

    @TypeConverter
    fun someObjectListToString(someObjects: List<ForecastTable.Weather?>?): String {
        return Gson().toJson(someObjects)
    }
}