package com.tt.weatherapp.util

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import java.util.*


object RuntimeLocaleChanger {
    fun wrapContext(context: Context, sharedPreferences: SharedPreferences?): Context {

        val savedLocale =
            createLocaleFromSavedLanguage(sharedPreferences) // load the user picked language from persistence (e.g SharedPreferences)
                ?: return context // else return the original untouched context

        // as part of creating a new context that contains the new locale we also need to override the default locale.
        Locale.setDefault(savedLocale)

        // create new configuration with the saved locale
        val newConfig = Configuration()
        newConfig.setLocale(savedLocale)

        return context.createConfigurationContext(newConfig)
    }

    fun overrideLocale(context: Context, sharedPreferences: SharedPreferences?) {

        val savedLocale =
            createLocaleFromSavedLanguage(sharedPreferences) // load the user picked language from persistence (e.g SharedPreferences)
                ?: return // nothing to do in this case

        // as part of creating a new context that contains the new locale we also need to override the default locale.
        Locale.setDefault(savedLocale)

        // create new configuration with the saved locale
        val newConfig = Configuration()
        newConfig.setLocale(savedLocale)

        // override the locale on the given context (Activity, Fragment, etc...)
        context.resources.updateConfiguration(newConfig, context.resources.displayMetrics)

        // override the locale on the application context
        if (context != context.applicationContext) {
            context.applicationContext.resources.run {
                updateConfiguration(
                    newConfig,
                    displayMetrics
                )
            }
        }
    }

    private fun createLocaleFromSavedLanguage(sharedPreferences: SharedPreferences?): Locale? =
        Locale(sharedPreferences?.getString("language", "en") ?: "en")
}