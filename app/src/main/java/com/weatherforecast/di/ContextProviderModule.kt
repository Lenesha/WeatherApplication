package com.weatherforecast.di

import com.weatherforecast.util.contextProvider.CoroutineContextProvider
import org.koin.dsl.module

val ContextProviderModule = module {

    single { CoroutineContextProvider() }
}