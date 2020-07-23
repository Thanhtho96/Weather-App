package com.tt.weatherapp.viewmodel

import android.app.Application
import android.content.SharedPreferences
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.tt.weatherapp.dao.WeatherDao
import com.tt.weatherapp.model.WeatherData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class WeatherViewModel(
    application: Application,
    private val weatherDao: WeatherDao,
    private val sharedPref: SharedPreferences
) : AndroidViewModel(application) {

    val weatherDataLiveData: MutableLiveData<WeatherData> = MutableLiveData()
    val latLiveData: MutableLiveData<Double> = MutableLiveData()
    val lonLiveData: MutableLiveData<Double> = MutableLiveData()
    val unitLiveData: MutableLiveData<String> = MutableLiveData()
    val lifecycleOwnerLiveData: MutableLiveData<LifecycleOwner> = MutableLiveData()
    private val locale: String = Locale.getDefault().language

    fun getWeatherDataByLatLon(lat: Double?, lon: Double?, unit: String?) {
        weatherDao.getWeatherByYourLocation(
            lat,
            lon,
            sharedPref.getString("token", "")!!,
            unit,
            locale
        )
            .enqueue(object : Callback<WeatherData> {
                override fun onResponse(call: Call<WeatherData>, response: Response<WeatherData>) {
                    val weatherData = response.body()
                    weatherData?.let {
                        weatherDataLiveData.value = it
                    }
                }

                override fun onFailure(call: Call<WeatherData>, t: Throwable) {
                    Toast.makeText(getApplication(), t.message, Toast.LENGTH_SHORT).show()
                }
            })
    }

    fun setLocationLatLon(lat: Double, lon: Double) {
        lonLiveData.value = lon
        latLiveData.value = lat
    }

    fun changeUnit(unit: String) {
        unitLiveData.value = unit
    }

    fun saveLifeCycle(lifecycleOwner: LifecycleOwner) {
        lifecycleOwnerLiveData.value = lifecycleOwner
    }
}