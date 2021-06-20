package com.weatherforecast.di

import com.weatherforecast.base.BaseListRepository
import com.weatherforecast.database.ForecastTable
import com.weatherforecast.repository.WeatherRepository
import org.koin.dsl.module

val repositoryModule = module {

    single<BaseListRepository<ForecastTable>> { WeatherRepository(get(), get(), get(), get(),get()) }
}