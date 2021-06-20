package com.weatherforecast.base

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weatherforecast.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

open class BaseViewModel<T>(
        private val repository: BaseRepository<T>
) : ViewModel() {

    private val _stateFlow = MutableStateFlow<Resource<T>>(Resource.loading())
    val stateFlow: StateFlow<Resource<T>>
        get() = _stateFlow

    fun refresh() {
        viewModelScope.launch {
            repository.result.collect {
                _stateFlow.value = it
            }
        }
    }

}