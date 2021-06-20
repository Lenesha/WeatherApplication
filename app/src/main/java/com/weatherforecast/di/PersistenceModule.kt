package com.weatherforecast.di

import androidx.room.Room
import com.weatherforecast.database.AppDataBase
import com.weatherforecast.util.AppConstants
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val persistenceModule = module {

    single {
        Room.databaseBuilder(androidApplication(), AppDataBase::class.java,
                AppConstants.DATABASE_NAME)
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build()
    }

    single { get<AppDataBase>().weatherDao }
    single { get<AppDataBase>().locationDao }

}