package com.weatherforecast

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.weatherforecast.database.ForecastTable
import com.weatherforecast.database.LocationDao
import com.weatherforecast.database.WeatherDao
import com.weatherforecast.network.WeatherForecastService
import com.weatherforecast.repository.WeatherRepository
import com.weatherforecast.util.AppConstants
import com.weatherforecast.util.Resource
import com.weatherforecast.util.contextProvider.CoroutineContextProvider
import com.weatherforecast.util.contextProvider.TestContextProvider
import com.weatherforecast.util.isNetworkAvailable
import com.weatherforecast.viewmodel.MainViewModel


import io.mockk.every
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

@ExperimentalCoroutinesApi
class MainViewModelTest {

    @get:Rule
    val rule: TestRule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @Mock
    private lateinit var api: WeatherForecastService

    @Mock
    private lateinit var dao: WeatherDao

    @Mock
    private lateinit var locationDao: LocationDao

    @Mock
    private lateinit var context: Context

    private lateinit var contextProvider: CoroutineContextProvider

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        contextProvider = TestContextProvider()
    }

    @Test
    fun givenServerResponse200_whenFetch_shouldReturnSuccess() {
        mockkStatic("com.weatherforecast.util.ContextExtKt")
        every {
            context.isNetworkAvailable()
        } returns true
        testCoroutineRule.runBlockingTest {
            `when`(api.forecastByLocation("37.4219983","-122.084",AppConstants.API_ID)).thenReturn(

            ForecastTable("",ForecastTable.Clouds(0),0,
                ForecastTable.Coord(0.0,0.0),0,0,
                ForecastTable.Main(0.0,0,0,0.0,0.0,0.0),"",ForecastTable.Sys("",0,0.0,0,0,0),0,
                emptyList(),ForecastTable.Wind(0.0,0.0))
            )
            `when`(dao.getForecast()).thenReturn( ForecastTable("",ForecastTable.Clouds(0),0,
                ForecastTable.Coord(0.0,0.0),0,0,
                ForecastTable.Main(0.0,0,0,0.0,0.0,0.0),"",ForecastTable.Sys("",0,0.0,0,0,0),0,
                emptyList(),ForecastTable.Wind(0.0,0.0)))
        }

        val repository = WeatherRepository(dao, api, context, contextProvider,locationDao)

        testCoroutineRule.pauseDispatcher()

        val viewModel = MainViewModel(repository)

        assertThat(viewModel.stateFlow.value, `is`(Resource.loading()))

        testCoroutineRule.resumeDispatcher()

        assertThat(viewModel.stateFlow.value, `is`(Resource.success(  ForecastTable("as",ForecastTable.Clouds(0),0,
            ForecastTable.Coord(0.0,0.0),0,0,
            ForecastTable.Main(0.0,0,0,0.0,0.0,0.0),"as",ForecastTable.Sys("as",0,0.0,0,0,0),0,
            emptyList(),ForecastTable.Wind(0.0,0.0)))))
    }

    @Test
    fun givenServerResponseError_whenFetch_shouldReturnError() {
        val errorMsg = "error message"
        `when`(context.getString(Mockito.anyInt())).thenReturn(errorMsg)
        mockkStatic("com.weatherforecast.util.ContextExtKt")
        every {
            context.isNetworkAvailable()
        } returns true
        testCoroutineRule.runBlockingTest {
            `when`(api.forecastByLocation("37.4219983","-122.084",AppConstants.API_ID)).thenThrow(RuntimeException(""))
            `when`(dao.getForecast()).thenReturn( ForecastTable("",ForecastTable.Clouds(0),0,
                ForecastTable.Coord(0.0,0.0),0,0,
                ForecastTable.Main(0.0,0,0,0.0,0.0,0.0),"",ForecastTable.Sys("",0,0.0,0,0,0),0,
                emptyList(),ForecastTable.Wind(0.0,0.0)))
        }
        val repository = WeatherRepository(dao, api, context, contextProvider,locationDao)

        testCoroutineRule.pauseDispatcher()

        val viewModel = MainViewModel(repository)

        assertThat(viewModel.stateFlow.value, `is`(Resource.loading()))

        testCoroutineRule.resumeDispatcher()

        assertThat(viewModel.stateFlow.value, `is`(Resource.error(errorMsg, null)))
    }

    @Test
    fun givenNetworkUnAvailable_whenFetch_shouldReturnError() {
        val errorMsg = "error message"
        `when`(context.getString(Mockito.anyInt())).thenReturn(errorMsg)
        mockkStatic("com.weatherforecast.util.ContextExtKt")
        every {
            context.isNetworkAvailable()
        } returns false
        testCoroutineRule.runBlockingTest {
            `when`(dao.getForecast()).thenReturn( ForecastTable("",ForecastTable.Clouds(0),0,
                ForecastTable.Coord(0.0,0.0),0,0,
                ForecastTable.Main(0.0,0,0,0.0,0.0,0.0),"",ForecastTable.Sys("",0,0.0,0,0,0),0,
                emptyList(),ForecastTable.Wind(0.0,0.0)))
        }
        val repository = WeatherRepository(dao, api, context, contextProvider,locationDao)

        testCoroutineRule.pauseDispatcher()

        val viewModel = MainViewModel(repository)

        assertThat(viewModel.stateFlow.value, `is`(Resource.loading()))

        testCoroutineRule.resumeDispatcher()

        assertThat(viewModel.stateFlow.value, `is`(Resource.error(errorMsg,  ForecastTable("",ForecastTable.Clouds(0),0,
            ForecastTable.Coord(0.0,0.0),0,0,
            ForecastTable.Main(0.0,0,0,0.0,0.0,0.0),"",ForecastTable.Sys("",0,0.0,0,0,0),0,
            emptyList(),ForecastTable.Wind(0.0,0.0)))))
    }
}