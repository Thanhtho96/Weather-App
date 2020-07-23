package com.tt.weatherapp

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import com.tt.weatherapp.di.appComponent
import com.tt.weatherapp.util.RuntimeLocaleChanger
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class WeatherApp : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@WeatherApp)
            androidLogger()
            modules(appComponent)
        }
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(RuntimeLocaleChanger.wrapContext(base, null))
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        RuntimeLocaleChanger.overrideLocale(this, null)
    }
}