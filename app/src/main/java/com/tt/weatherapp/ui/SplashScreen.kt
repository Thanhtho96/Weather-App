package com.tt.weatherapp.ui

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tt.weatherapp.R
import com.tt.weatherapp.util.RuntimeLocaleChanger
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject

class SplashScreen : AppCompatActivity() {

    private val sharedPref: SharedPreferences by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        GlobalScope.launch {
            val token = sharedPref.getString("token", null)
            if (token == null) {
                sharedPref.edit().putString("token", "19150b28972e472b00d965a87f00b49a").apply()
            }
            delay(777)
            withContext(Dispatchers.Main) {
                this@SplashScreen.startActivity(Intent(this@SplashScreen, MainActivity::class.java))
                    .apply { finish() }
            }
        }
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(RuntimeLocaleChanger.wrapContext(base, sharedPref))
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        RuntimeLocaleChanger.overrideLocale(this, sharedPref)
    }
}
