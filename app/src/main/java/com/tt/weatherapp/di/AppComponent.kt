package com.tt.weatherapp.di

import android.app.Application
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.tt.weatherapp.dao.WeatherDao
import com.tt.weatherapp.viewmodel.PlaceViewModel
import com.tt.weatherapp.viewmodel.WeatherViewModel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

val appComponent = module {
    factory { getHttpLoggingInterceptor() }
    factory { provideOkHttpClient(get()) }
    single { provideRetrofit() }
    single { provideWeatherDao(get()) }
    single { providesSharedPreferences(get()) }
    viewModel { WeatherViewModel(get(), get(), get()) }
    viewModel { PlaceViewModel(get()) }
}

fun getHttpLoggingInterceptor(): HttpLoggingInterceptor {
    val httpLoggingInterceptor = HttpLoggingInterceptor()
    httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
    return httpLoggingInterceptor
}

fun provideOkHttpClient(httpLoggingInterceptor: HttpLoggingInterceptor): OkHttpClient {
    return OkHttpClient.Builder()
        .addInterceptor(httpLoggingInterceptor)
        .connectTimeout(1, TimeUnit.MINUTES)
        .callTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
}

fun provideRetrofit(): Retrofit {
    return Retrofit.Builder()
        .baseUrl("https://api.openweathermap.org/data/2.5/")
        .client(provideOkHttpClient(getHttpLoggingInterceptor()))
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}

fun providesSharedPreferences(application: Application): SharedPreferences {
    return PreferenceManager.getDefaultSharedPreferences(application)
}

fun provideWeatherDao(retrofit: Retrofit): WeatherDao {
    return retrofit.create(
        WeatherDao::class.java
    )
}
