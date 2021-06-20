package com.weatherforecast.viewmodel

import com.weatherforecast.base.BaseListRepository
import com.weatherforecast.base.BaseViewModel
import com.weatherforecast.database.ForecastTable

class MainViewModel(repository: BaseListRepository<ForecastTable>) : BaseViewModel<ForecastTable>(repository)