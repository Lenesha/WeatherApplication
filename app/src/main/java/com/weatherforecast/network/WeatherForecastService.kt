package com.weatherforecast.network

import com.weatherforecast.database.ForecastTable
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherForecastService {
    @GET("weather")
    suspend fun forecastByLocation(@Query("lat") lat: String?, @Query("lon") lon: String?, @Query("appid") appId: String): ForecastTable

}