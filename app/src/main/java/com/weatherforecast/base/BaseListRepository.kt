package com.weatherforecast.base

import android.content.Context
import com.weatherforecast.util.contextProvider.CoroutineContextProvider

abstract class BaseListRepository<T>(
        context: Context,
        contextProvider: CoroutineContextProvider
) : BaseRepository<T>(context, contextProvider) {

}