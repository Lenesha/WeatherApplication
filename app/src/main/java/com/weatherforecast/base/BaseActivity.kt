package com.weatherforecast.base

import androidx.lifecycle.ViewModel

abstract class BaseActivity<VM : ViewModel> : BaseBindingActivity() {

    protected abstract val viewModel: VM
}