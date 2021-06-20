package com.weatherforecast.repository

import android.content.Context
import android.util.Log
import com.weatherforecast.base.BaseListRepository
import com.weatherforecast.database.ForecastTable
import com.weatherforecast.database.LocationDao
import com.weatherforecast.database.WeatherDao

import com.weatherforecast.network.WeatherForecastService
import com.weatherforecast.util.AppConstants
import com.weatherforecast.util.contextProvider.CoroutineContextProvider

class WeatherRepository(
    private val dao: WeatherDao,
    private val api: WeatherForecastService,
    context: Context,
    contextProvider: CoroutineContextProvider,
    private val locationDao: LocationDao
) : BaseListRepository<ForecastTable>(context, contextProvider) {

    override suspend fun query():ForecastTable = dao.getForecast()

    override suspend fun fetch(): ForecastTable {
       val location =  locationDao.query()
       return api.forecastByLocation(location.latitude.toString(),location.longitude.toString(),AppConstants.API_ID)

    }

    override suspend fun saveFetchResult(items: ForecastTable) {
        Log.d("WEatherSample","called")
        dao.updateForecast(items)
    }
}