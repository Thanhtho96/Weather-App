package com.tt.weatherapp.dao

import com.tt.weatherapp.model.WeatherData
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherDao {

    @GET("onecall")
    fun getWeatherByYourLocation(
        @Query("lat") lat: Double?,
        @Query("lon") lon: Double?,
        @Query("appid") appid: String,
        @Query("units") units: String?,
        @Query("lang") lang: String?
    ): Call<WeatherData>
}