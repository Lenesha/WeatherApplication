package com.weatherforecast.work

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.weatherforecast.repository.WeatherRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

 class RefreshWeatherForecast(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params), KoinComponent {

    companion object {
        const val WORK_NAME = "RefreshWeatherWork"
    }
    /**
     * A coroutine-friendly method to do your work.
     */
    override suspend fun doWork(): Result {
        val repository: WeatherRepository by inject()
        return try {
            repository.loadFromNetwork()
            Result.success()
        } catch (err: Exception) {
            Result.failure()
        }
    }
}