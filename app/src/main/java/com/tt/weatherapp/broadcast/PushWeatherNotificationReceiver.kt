package com.tt.weatherapp.broadcast

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.BitmapFactory
import android.os.Build
import android.text.Html
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY
import androidx.preference.PreferenceManager
import com.tt.weatherapp.R
import com.tt.weatherapp.Unit
import com.tt.weatherapp.dao.WeatherDao
import com.tt.weatherapp.model.WeatherData
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

class PushWeatherNotificationReceiver : BroadcastReceiver() {
    private lateinit var sharedPref: SharedPreferences

    @ExperimentalStdlibApi
    override fun onReceive(context: Context, intent: Intent) {
        sharedPref = PreferenceManager.getDefaultSharedPreferences(context)

        val lat = sharedPref.getFloat("lat", 0f).toDouble()
        val lon = sharedPref.getFloat("lon", 0f).toDouble()
        val unit = sharedPref.getString("unit", Unit.Imperial.toString())
        val appId = sharedPref.getString("token", "")
        val lang = sharedPref.getString("language", "en")
        val genericAddress = sharedPref.getString("genericAddress", "")

        var conf: Configuration = context.resources.configuration
        conf = Configuration(conf)
        conf.setLocale(Locale(lang ?: "en"))
        val localizedContext = context.createConfigurationContext(conf)
        val resources = localizedContext.resources

        createNotificationChannel(
            context.getString(R.string.weather_info_channel_ID),
            context.getString(R.string.txt_weather_info_notification_description),
            context.getString(R.string.weather_info_channel_name),
            context
        )

        val weatherDao = provideRetrofit().create(WeatherDao::class.java)
        weatherDao.getWeatherByYourLocation(lat, lon, appId!!, unit, lang)
            .enqueue(object : Callback<WeatherData> {
                override fun onFailure(call: Call<WeatherData>, t: Throwable) {
                    Log.d("TAG", "onFailure: ${t.message}")
                }

                override fun onResponse(
                    call: Call<WeatherData>,
                    response: Response<WeatherData>
                ) {
                    val weatherData: WeatherData? = response.body()

                    weatherData?.also {
                        val bigText: String
                        val contentText: String
                        when (Unit.valueOf(unit!!)) {
                            Unit.Metric -> {
                                contentText = resources.getString(
                                    R.string.txt_celsius_temp_and_feel_like,
                                    it.current.temp.roundToInt().toString(),
                                    it.current.feels_like.roundToInt().toString()
                                )

                                bigText = resources.getString(
                                    R.string.txt_description_meter,
                                    it.current.weather[0].description.capitalize(
                                        Locale.ROOT
                                    ),
                                    it.daily[0].temp.max.roundToInt().toString(),
                                    resources.getStringArray(R.array.compass_directions)[((it.current.wind_deg % 360) / 22.5).roundToInt()],
                                    it.current.wind_speed.roundToInt().toString()
                                )
                            }
                            Unit.Imperial -> {
                                contentText = resources.getString(
                                    R.string.txt_fahrenheit_temp_and_feel_like,
                                    it.current.temp.roundToInt().toString(),
                                    it.current.feels_like.roundToInt().toString()
                                )

                                bigText = resources.getString(
                                    R.string.txt_description_imperial,
                                    it.current.weather[0].description.capitalize(
                                        Locale.ROOT
                                    ),
                                    it.daily[0].temp.max.roundToInt().toString(),
                                    resources.getStringArray(R.array.compass_directions)[((it.current.wind_deg % 360) / 22.5).roundToInt()],
                                    it.current.wind_speed.roundToInt().toString()
                                )
                            }
                        }

                        val notification = NotificationCompat.Builder(
                            context,
                            context.getString(R.string.weather_info_channel_ID)
                        )
                            .setSmallIcon(
                                chooseSmallIcon(
                                    it.current.weather[0].icon
                                )
                            )
                            .setColor(
                                ContextCompat.getColor(
                                    context,
                                    R.color.white
                                )
                            )
                            .setContentText(contentText)
                            .setLargeIcon(
                                BitmapFactory.decodeResource(
                                    resources,
                                    chooseLargeIcon(it.current.weather[0].icon)
                                )
                            )
                            .setStyle(
                                NotificationCompat.BigTextStyle()
                                    .bigText("$contentText\n$bigText")
                            )
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .setAutoCancel(true)
                            .setContentTitle(
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    Html.fromHtml(
                                        resources.getString(
                                            R.string.bold_text,
                                            genericAddress
                                        ), FROM_HTML_MODE_LEGACY
                                    )
                                } else {
                                    resources.getString(
                                        R.string.bold_text,
                                        genericAddress
                                    )
                                }
                            ).build()

                        with(NotificationManagerCompat.from(context)) {
                            // notificationId is a unique int for each notification that you must define
                            notify(
                                System.currentTimeMillis().toInt(),
                                notification
                            )
                        }
                    }
                }
            })
    }

    private fun chooseSmallIcon(icon: String): Int {
        return when (icon) {
            "01d", "01n" -> {
                R.drawable.ic_noti_z01
            }
            "02d", "02n" -> {
                R.drawable.ic_noti_z02
            }
            "03d", "03n" -> {
                R.drawable.ic_noti_z03
            }
            "04d", "04n" -> {
                R.drawable.ic_noti_z04
            }
            "09d", "09n" -> {
                R.drawable.ic_noti_z09
            }
            "10d", "10n" -> {
                R.drawable.ic_noti_z10
            }
            "11d", "11n" -> {
                R.drawable.ic_noti_z11
            }
            "13d", "13n" -> {
                R.drawable.ic_noti_z13
            }
            "50d" -> {
                R.drawable.ic_noti_z50
            }
            else -> {
                R.drawable.ic_noti_z50
            }
        }
    }

    private fun chooseLargeIcon(icon: String): Int {
        return when (icon) {
            "01d" -> {
                R.drawable.z01d
            }
            "01n" -> {
                R.drawable.z01n
            }
            "02d" -> {
                R.drawable.z02d
            }
            "02n" -> {
                R.drawable.z02n
            }
            "03d" -> {
                R.drawable.z03d
            }
            "03n" -> {
                R.drawable.z03n
            }
            "04d" -> {
                R.drawable.z04d
            }
            "04n" -> {
                R.drawable.z04n
            }
            "09d" -> {
                R.drawable.z09d
            }
            "09n" -> {
                R.drawable.z09n
            }
            "10d" -> {
                R.drawable.z10d
            }
            "10n" -> {
                R.drawable.z10n
            }
            "11d" -> {
                R.drawable.z11d
            }
            "11n" -> {
                R.drawable.z11n
            }
            "13d", "13n" -> {
                R.drawable.ic_noti_z13
            }
            "50d" -> {
                R.drawable.ic_noti_z50
            }
            else -> {
                R.drawable.ic_noti_z50
            }
        }
    }

    private fun getHttpLoggingInterceptor(): HttpLoggingInterceptor {
        val httpLoggingInterceptor = HttpLoggingInterceptor()
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        return httpLoggingInterceptor
    }

    private fun provideOkHttpClient(httpLoggingInterceptor: HttpLoggingInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(httpLoggingInterceptor)
            .connectTimeout(1, TimeUnit.MINUTES)
            .callTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .client(provideOkHttpClient(getHttpLoggingInterceptor()))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private fun createNotificationChannel(
        channelId: String,
        description: String,
        channelName: String,
        context: Context
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                this.description = description
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}