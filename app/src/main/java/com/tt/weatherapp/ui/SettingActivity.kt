package com.tt.weatherapp.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import com.tt.weatherapp.R
import com.tt.weatherapp.Unit
import com.tt.weatherapp.util.RuntimeLocaleChanger
import kotlinx.android.synthetic.main.activity_setting.*
import kotlinx.android.synthetic.main.fragment_setting.text_view_my_location
import org.koin.android.ext.android.inject

class SettingActivity : AppCompatActivity() {

    private val sharedPref: SharedPreferences by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        text_view_my_location.setOnClickListener {
            setResult(Activity.RESULT_OK, intent.putExtra("Setting", "location"))
            finish()
        }

        radioGroupUnit.setOnCheckedChangeListener { _, checkedId ->
            if (!findViewById<RadioButton>(checkedId).isPressed) return@setOnCheckedChangeListener
            when (checkedId) {
                radioButtonMetric.id -> {
                    sharedPref.edit().putString("unit", Unit.Metric.toString()).apply()
                    setResult(
                        Activity.RESULT_OK,
                        intent.putExtra("Setting", Unit.Metric.toString())
                    )
                    finish()
                }
                radioButtonImperial.id -> {
                    sharedPref.edit().putString("unit", Unit.Imperial.toString()).apply()
                    setResult(
                        Activity.RESULT_OK,
                        intent.putExtra("Setting", Unit.Imperial.toString())
                    )
                    finish()
                }
            }
        }

        when (Unit.valueOf(sharedPref.getString("unit", Unit.Imperial.toString())!!)) {
            Unit.Metric -> {
                radioButtonMetric.isChecked = true
            }
            Unit.Imperial -> {
                radioButtonImperial.isChecked = true
            }
        }

        button_choose_language.setOnClickListener {
            ChooseLanguageDialogFragment.newInstance().also {
                it.show(
                    supportFragmentManager,
                    "lang_fragment"
                )
            }
        }

        backButton.setOnClickListener {
            onBackPressed()
        }
    }

    internal fun reLaunchActivity() {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        finish()
        startActivity(intent)
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(RuntimeLocaleChanger.wrapContext(base, sharedPref))
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        RuntimeLocaleChanger.overrideLocale(this, sharedPref)
    }
}