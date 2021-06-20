package com.weatherforecast.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.work.*
import com.google.android.gms.location.*
import com.google.common.util.concurrent.ListenableFuture
import com.weatherforecast.R
import com.weatherforecast.base.BaseActivity
import com.weatherforecast.database.AppDataBase
import com.weatherforecast.database.ForecastTable
import com.weatherforecast.database.LocationTable
import com.weatherforecast.databinding.ActivityMainBinding
import com.weatherforecast.util.AppConstants
import com.weatherforecast.util.Resource
import com.weatherforecast.util.hide
import com.weatherforecast.util.show
import com.weatherforecast.viewmodel.MainViewModel
import com.weatherforecast.work.RefreshWeatherForecast
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.getViewModel
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit


@InternalCoroutinesApi
class MainActivity : BaseActivity<MainViewModel>() {

    override val binding: ActivityMainBinding by binding(R.layout.activity_main)

    override val viewModel: MainViewModel
        get() = getViewModel()


    val database: AppDataBase by inject()

    var mFusedLocationClient: FusedLocationProviderClient? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        isWorkScheduled("PeriodicWork")
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        getLastLocation()

        binding.apply {
            vm = viewModel
        }


        lifecycleScope.launch {
            viewModel.stateFlow.collect { resource ->
                when (resource.status) {
                    Resource.Status.SUCCESS -> {
                        submitList(resource.data)
                    }
                    Resource.Status.LOADING -> {
                        binding.loadingSpinner.show()
                        binding.errorLayout.hide()
                    }
                    Resource.Status.ERROR -> {
                        when {
                            resource.data==null -> {
                                binding.loadingSpinner.hide()
                                binding.errorLayout.show()
                                binding.errorMsg.text = resource.message
                            }

                            else -> {
                                submitList(resource.data)
                            }
                        }
                    }
                }
            }
        }

    }

    private fun submitList(data: ForecastTable?) {
        binding.loadingSpinner.hide()
        binding.errorLayout.hide()

        binding.nameDesc.text = data?.name
        binding.minTempDesc.text = data?.main?.tempMin.toString()
        binding.maxTempDesc.text = data?.main?.tempMax.toString()
        binding.weatherDesc.text = data?.weather?.get(0)?.main
        binding.speedDesc.text = data?.wind?.speed.toString()

    }

    private fun getLastLocation() {
            if (isLocationEnabled()) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermissions()
                    return
                }
                mFusedLocationClient?.lastLocation?.addOnCompleteListener { task ->
                    val location = task.result
                    if (location == null) {
                        requestNewLocationData()
                    } else {
                        saveAndRefreshData(location)
                    }
                }
            } else {
                AlertDialog.Builder(this)
                    .setMessage(getString(R.string.location_permission))
                    .setPositiveButton(
                        R.string.ok
                    ) { _, _ ->
                        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        startActivity(intent)
                    }
                    .show()
            }

    }


    private fun requestNewLocationData() {
        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions()
            return
        }
        mFusedLocationClient?.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager =
           getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        return locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            AppConstants.PERMISSION_ID
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == AppConstants.PERMISSION_ID
        ) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation()
            }
        }
    }

    private val mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {

            saveAndRefreshData(locationResult.lastLocation)

        }
    }

    private fun saveAndRefreshData(lastLocation: Location) {

        saveLocation(lastLocation)
        viewModel.refresh()

        cancelPrevioursWork()
        setupRecurringWork()
    }

    private fun cancelPrevioursWork() {
        WorkManager.getInstance(this).cancelAllWorkByTag(RefreshWeatherForecast.WORK_NAME)


    }

    private fun setupRecurringWork() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .build()

        val repeatingRequest = PeriodicWorkRequestBuilder<RefreshWeatherForecast>(15, TimeUnit.MINUTES)
            .setConstraints(constraints).setInitialDelay(15,TimeUnit.MINUTES).addTag(RefreshWeatherForecast.WORK_NAME).build()


        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            RefreshWeatherForecast.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            repeatingRequest
        )
    }

    private fun saveLocation(location: Location) = lifecycleScope.launch {
        database.locationDao.insert(
        LocationTable(location.latitude,location.longitude,System.currentTimeMillis())
    ) }

    private fun isWorkScheduled(tag: String): Boolean {
        val instance = WorkManager.getInstance()
        val statuses: ListenableFuture<List<WorkInfo>> = instance.getWorkInfosByTag(tag)
        return try {
            var running = false
            val workInfoList: List<WorkInfo> = statuses.get()
            for (workInfo in workInfoList) {
                val state = workInfo.state
                running = state == WorkInfo.State.RUNNING || state == WorkInfo.State.ENQUEUED
            }
            running
        } catch (e: ExecutionException) {
            e.printStackTrace()
            false
        } catch (e: InterruptedException) {
            e.printStackTrace()
            false
        }
    }


}
