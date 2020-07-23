package com.tt.weatherapp.ui

import android.Manifest
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import com.tt.weatherapp.R
import com.tt.weatherapp.broadcast.PushWeatherNotificationReceiver
import com.tt.weatherapp.util.RuntimeLocaleChanger
import com.tt.weatherapp.viewmodel.WeatherViewModel
import kotlinx.android.synthetic.main.fragment_main.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.IOException
import java.util.*

private const val PERMISSION_REQUEST_LOCATION = 77
private const val SERVICE_REQUEST_LOCATION = 777
private const val PUSH_NOTIFICATION_MORNING = 77777

class MainActivity : AppCompatActivity() {

    private var alarmManager: AlarmManager? = null

    private val fm = supportFragmentManager

    private lateinit var geoCoder: Geocoder
    private val sharedPref: SharedPreferences by inject()
    private val weatherViewModel: WeatherViewModel by viewModel()

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private var lat: Double = 0.0
    private var lon: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        alarmManager =
            getSystemService(Context.ALARM_SERVICE) as? AlarmManager

        geoCoder = Geocoder(this, Locale.getDefault())
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        createLocationRequest()
        initLocationCallBack()
        checkIfLocationServiceEnable()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location -> // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        lat = location.latitude
                        lon = location.longitude
                    }
                }
        }

        fm.beginTransaction()
            .add(R.id.nav_host_activity, MainFragment.newInstance(), "main_fragment").commit()

        if (!sharedPref.getBoolean("alarm", false)) setWeatherNotification()
    }

    private fun setWeatherNotification() {
        val myIntent = Intent(this, PushWeatherNotificationReceiver::class.java)

        val pendingIntentMorning = PendingIntent.getBroadcast(
            this, PUSH_NOTIFICATION_MORNING, myIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )

        val now = Calendar.getInstance()

        val calendarMorning: Calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 6)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        if (now.after(calendarMorning)) calendarMorning.add(Calendar.DATE, 1)

        alarmManager?.setInexactRepeating(
            AlarmManager.RTC,
            calendarMorning.timeInMillis,
            AlarmManager.INTERVAL_HALF_DAY,
            pendingIntentMorning
        )

        sharedPref.edit().putBoolean("alarm", true).apply()
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(RuntimeLocaleChanger.wrapContext(base, sharedPref))
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        RuntimeLocaleChanger.overrideLocale(this, sharedPref)
    }

    private fun fetchWeatherDataByLatLon(lat: Double, lon: Double) {
        weatherViewModel.setLocationLatLon(lat, lon)
        sharedPref.edit().apply {
            putFloat("lat", lat.toFloat())
            putFloat("lon", lon.toFloat())
            apply()
        }
    }

    private fun initLocationCallBack() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    lat = location.latitude
                    lon = location.longitude

                    Log.i("LAT", "onLocationResult: $lat")
                    Log.i("LON", "onLocationResult: $lon")

                    if (lat != 0.0 && lon != 0.0) {

                        fetchWeatherDataByLatLon(lat, lon)

                        try {
                            val address: List<Address> =
                                geoCoder.getFromLocation(lat, lon, 1)
                            val builder = StringBuilder()
                            val genericAddress = StringBuilder()
                            val featureName = address[0].featureName
                            val thoroughfare = address[0].thoroughfare
                            val subAdminArea = address[0].subAdminArea
                            val adminArea = address[0].adminArea
                            if (featureName != null) {
                                builder.append(featureName)
                                builder.append(", ")
                            }
                            if (thoroughfare != null) {
                                builder.append(thoroughfare)
                                builder.append(", ")
                            }
                            if (subAdminArea != null) {
                                builder.append(subAdminArea)
                                genericAddress.append(subAdminArea)
                                builder.append(", ")
                                genericAddress.append(", ")
                            }
                            if (adminArea != null) {
                                builder.append(adminArea)
                                genericAddress.append(adminArea)
                                builder.append(", ")
                                genericAddress.append(", ")
                            }
                            sharedPref.edit().apply {
                                putString("address", builder.substring(0, builder.length - 2))
                                putString("countryCode", address[0].countryCode).apply()
                                putString(
                                    "genericAddress",
                                    genericAddress.substring(0, genericAddress.length - 2)
                                )
                            }.apply()

                        } catch (e: IOException) {
                            e.printStackTrace()
                            Log.e("Address error", e.message!!)
                        }

                        fusedLocationClient.removeLocationUpdates(this)
                    }
                }
            }
        }
    }

    internal fun checkIfLocationServiceEnable() {
        val lm =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var gpsEnabled = false
        var networkEnabled = false

        try {
            gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (ex: Exception) {
            Snackbar.make(
                container, getString(R.string.gps_provider_not_available),
                Snackbar.LENGTH_INDEFINITE
            ).setAction(
                getString(R.string.open_wireless_settings)
            ) {
                startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
            }.show()
        }

        try {
            networkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (ex: Exception) {
            Snackbar.make(
                container, getString(R.string.network_provider_not_available),
                Snackbar.LENGTH_INDEFINITE
            ).setAction(
                getString(R.string.txt_open_location_settings)
            ) {
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }.show()
        }

        if (!gpsEnabled && !networkEnabled) {
            // notify user
            AlertDialog.Builder(this, R.style.AlertDialogStyle)
                .setMessage(R.string.txt_gps_network_not_enabled)
                .setPositiveButton(
                    R.string.txt_open_location_settings
                ) { _, _ ->
                    startActivityForResult(
                        Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),
                        SERVICE_REQUEST_LOCATION
                    )
                }
                .setNegativeButton(R.string.txt_cancel) { _, _ ->
                    Snackbar.make(
                        container, getString(R.string.need_location_setting),
                        Snackbar.LENGTH_INDEFINITE
                    ).setAction(
                        getString(R.string.enable_it)
                    ) { // Request the permission
                        checkIfLocationServiceEnable()
                    }.show()
                }
                .setCancelable(false)
                .show()
        } else {
            startLocationService()
        }
    }

    internal fun refreshFragmentHome(unit: String) {
        weatherViewModel.changeUnit(unit)
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest.create().apply {
            interval = 1000000
            fastestInterval = 500000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        // BEGIN_INCLUDE(onRequestPermissionsResult)

        if (requestCode == PERMISSION_REQUEST_LOCATION) {
            // Request for camera permission.
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission has been granted. Start camera preview Activity.
                startLocationService()
            } else {
                // Permission request was denied.
                Snackbar.make(
                    container, getString(R.string.need_location_permission),
                    Snackbar.LENGTH_INDEFINITE
                ).setAction(
                    getString(R.string.grant_permission)
                ) { // Request the permission
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                    ) {
                        AlertDialog.Builder(this, R.style.AlertDialogStyle)
                            .setMessage(R.string.location_permission_is_deny)
                            .setPositiveButton(
                                R.string.txt_open_app_settings
                            ) { _, _ ->
                                val intent =
                                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                val uri = Uri.fromParts("package", packageName, null)
                                intent.data = uri
                                startActivityForResult(intent, PERMISSION_REQUEST_LOCATION)
                            }
                            .setNegativeButton(R.string.txt_cancel) { _, _ ->
                                Snackbar.make(
                                    container, getString(R.string.need_location_setting),
                                    Snackbar.LENGTH_INDEFINITE
                                ).setAction(
                                    getString(R.string.enable_it)
                                ) { // Request the permission
                                    val intent =
                                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                    val uri = Uri.fromParts("package", packageName, null)
                                    intent.data = uri
                                    startActivityForResult(intent, PERMISSION_REQUEST_LOCATION)
                                }.show()
                            }
                            .setCancelable(false)
                            .show()
                    } else {
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            PERMISSION_REQUEST_LOCATION
                        )
                    }
                }.show()
            }
        }
        // END_INCLUDE(onRequestPermissionsResult)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            PERMISSION_REQUEST_LOCATION, SERVICE_REQUEST_LOCATION -> {
                checkIfLocationServiceEnable()
            }

            RATING_REQUEST -> {
                AlertDialog.Builder(this, R.style.AlertDialogStyle)
                    .setMessage(R.string.txt_thank_you_message)
                    .setPositiveButton(
                        R.string.txt_ok
                    ) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }
        }
    }

    private fun startLocationService() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } else {
            // Permission is missing and must be requested.
            requestLocationPermission()
        }
    }

    private fun requestLocationPermission() {
        // Permission has not been granted and must be requested.
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // Display a SnackBar with cda button to request the missing permission.
            Snackbar.make(
                container, getString(R.string.need_location_permission),
                Snackbar.LENGTH_INDEFINITE
            ).setAction(
                getString(R.string.grant_permission)
            ) { // Request the permission
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSION_REQUEST_LOCATION
                )
            }.show()
        } else {
            // Request the permission. The result will be received in onRequestPermissionResult().
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_REQUEST_LOCATION
            )
        }
    }

    override fun onBackPressed() {
        if (fm.backStackEntryCount == 0) {
            ExitDialogFragment.newInstance(object :
                ExitDialogFragment.OnButtonChoiceClickListener {
                override fun onYesClick() {
                    finish()
                }
            }).also {
                it.show(fm, "exit_fragment")
            }
        } else
            super.onBackPressed()
    }

    override fun onDestroy() {
        Log.d("Location", "Destroy")
        fusedLocationClient.removeLocationUpdates(locationCallback)
        super.onDestroy()
    }

    companion object {
        const val RATING_REQUEST = 7777
    }
}