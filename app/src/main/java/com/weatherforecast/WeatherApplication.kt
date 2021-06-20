
package com.weatherforecast

import android.app.Application
import android.os.Build
import androidx.work.*
import com.weatherforecast.work.RefreshWeatherForecast
import com.weatherforecast.di.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import java.util.concurrent.TimeUnit

class WeatherApplication : Application()  {

    private val applicationScope = CoroutineScope(Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@WeatherApplication)
            modules(listOf(
                networkModule,
                    persistenceModule,
                    repositoryModule,
                    viewModelModule,
                    ContextProviderModule
            ))
        }

    }

}