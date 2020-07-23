package com.tt.weatherapp.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.tt.weatherapp.R
import com.tt.weatherapp.Unit
import com.tt.weatherapp.adapter.DailyAdapter
import com.tt.weatherapp.adapter.HourlyAdapter
import com.tt.weatherapp.model.Daily
import com.tt.weatherapp.model.Hourly
import com.tt.weatherapp.model.WeatherData
import com.tt.weatherapp.viewmodel.WeatherViewModel
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

private const val unitParam = "unit"
private const val SETTING_CODE = 77

class HomeFragment : Fragment(), View.OnClickListener {
    private lateinit var unit: String
    private val weatherViewModel: WeatherViewModel by sharedViewModel(from = { requireActivity() })
    private val sharedPref: SharedPreferences by inject()
    private var listHourly: MutableList<Hourly> = ArrayList()
    private lateinit var hourlyAdapter: HourlyAdapter
    private val listDaily: MutableList<Daily> = ArrayList()
    private lateinit var dailyAdapter: DailyAdapter
    private val listMinTemp: MutableList<Double> = ArrayList()
    private val listMaxTemp: MutableList<Double> = ArrayList()
    private var owner: LifecycleOwner? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            unit = it.getString(unitParam).toString()
        }

        if (owner != null) {
            weatherViewModel.latLiveData.observe(owner!!, Observer { lat ->
                weatherViewModel.getWeatherDataByLatLon(
                    lat,
                    weatherViewModel.lonLiveData.value,
                    unit
                )
            })
        } else {
            weatherViewModel.latLiveData.observe(
                weatherViewModel.lifecycleOwnerLiveData.value!!,
                Observer { lat ->
                    weatherViewModel.getWeatherDataByLatLon(
                        lat,
                        weatherViewModel.lonLiveData.value,
                        unit
                    )
                })
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        owner?.let { weatherViewModel.saveLifeCycle(it) }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    @ExperimentalStdlibApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val aHalfAppBarHeight = (app_bar.layoutParams as CoordinatorLayout.LayoutParams).height / 2
        generateHourlyRecyclerView(requireContext())

        weatherViewModel.weatherDataLiveData.observe(viewLifecycleOwner, Observer {
            lifecycleScope.launch(Dispatchers.Main) {
                delay(77)
                addressHeaderText = sharedPref.getString("address", "")
                setDataToView(it)
            }
        })

        weatherViewModel.unitLiveData.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                unit = it
                weatherViewModel.getWeatherDataByLatLon(
                    weatherViewModel.latLiveData.value,
                    weatherViewModel.lonLiveData.value,
                    unit
                )
            }
        })

        button_setting.setOnClickListener {
            startActivityForResult(Intent(context, SettingActivity::class.java), SETTING_CODE)
        }

        edit_text_search_place.setOnClickListener(this)
        button_search.setOnClickListener(this)
        button_see_more_hourly.setOnClickListener(this)
        button_see_more_daily.setOnClickListener(this)
        textViewDaily.setOnClickListener(this)
        recycler_view_daily.setOnClickListener(this)
        textViewHourly.setOnClickListener(this)
        recycler_view_hourly.setOnClickListener(this)

        setHourHeaderAndPlaceHeader(aHalfAppBarHeight)
    }

    private fun setHourHeaderAndPlaceHeader(aHalfAppBarHeight: Int) {
        val fetchTimeStampTopMargin =
            (fetch_timestamp.layoutParams as ConstraintLayout.LayoutParams).topMargin

        fetch_timestamp.measure(0, 0)
        val fetchTimeStampHeight = fetch_timestamp.measuredHeight

        val currentTemperatureTopMargin =
            (current_temp.layoutParams as ConstraintLayout.LayoutParams).topMargin

        current_temp.measure(0, 0)
        val currentTemperatureHeight = current_temp.measuredHeight

        val locationNameTopMargin =
            (location_name.layoutParams as ConstraintLayout.LayoutParams).topMargin

        location_name.measure(0, 0)
        val locationNameHeight = location_name.measuredHeight

        val distanceFromLocationNameToTop =
            fetchTimeStampTopMargin +
                    fetchTimeStampHeight +
                    currentTemperatureTopMargin +
                    currentTemperatureHeight +
                    locationNameTopMargin +
                    aHalfAppBarHeight

        scroll_view.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, _ ->
            when {
                scrollY <= aHalfAppBarHeight -> {
                    text_view_header.visibility = View.GONE
                    hourHeaderVisible = false
                    return@OnScrollChangeListener
                }
                scrollY >= aHalfAppBarHeight && scrollY >= aHalfAppBarHeight != hourHeaderVisible -> {
                    text_view_header.text = hourHeaderText
                    text_view_header.visibility = View.VISIBLE
                    hourHeaderVisible = true
                    return@OnScrollChangeListener
                }
                (scrollY + fetchTimeStampTopMargin) < aHalfAppBarHeight && scrollY >= aHalfAppBarHeight != hourHeaderVisible -> {
                    text_view_header.visibility = View.GONE
                    hourHeaderVisible = false
                    return@OnScrollChangeListener
                }
                scrollY >= distanceFromLocationNameToTop && scrollY >= distanceFromLocationNameToTop != addressHeaderVisible -> {
                    text_view_header.text = addressHeaderText
                    addressHeaderVisible = true
                    return@OnScrollChangeListener
                }
                (scrollY + locationNameHeight) < distanceFromLocationNameToTop && scrollY >= distanceFromLocationNameToTop != addressHeaderVisible -> {
                    text_view_header.text = hourHeaderText
                    hourHeaderVisible = true
                    addressHeaderVisible = false
                    return@OnScrollChangeListener
                }
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SETTING_CODE && resultCode == Activity.RESULT_OK && data != null) {
            when (data.getStringExtra("Setting")) {
                "location" -> {
                    (activity as MainActivity).checkIfLocationServiceEnable()
                }
                Unit.Metric.toString() -> {
                    (activity as MainActivity).refreshFragmentHome(Unit.Metric.toString())
                }
                Unit.Imperial.toString() -> {
                    (activity as MainActivity).refreshFragmentHome(Unit.Imperial.toString())
                }
            }
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            button_see_more_daily, textViewDaily, recycler_view_daily -> {
                (parentFragment as MainFragment).showDailyDetailFragment()
            }
            button_see_more_hourly, textViewHourly, recycler_view_hourly -> {
                (parentFragment as MainFragment).showHourlyDetailFragment()
            }
            button_search, edit_text_search_place -> {
                if (v == edit_text_search_place) {
                    edtClickCount++
                    if (edtClickCount % 2 == 0) return
                }
                (activity as MainActivity).supportFragmentManager.beginTransaction()
                    .setCustomAnimations(
                        R.anim.enter_from_top,
                        R.anim.exit_to_top,
                        R.anim.enter_from_top,
                        R.anim.exit_to_top
                    ).add(
                        R.id.nav_host_activity,
                        SearchPlaceFragment.newInstance(),
                        "search_fragment"
                    )
                    .addToBackStack(null).commit()
            }
        }
    }

    @ExperimentalStdlibApi
    private fun setDataToView(weatherData: WeatherData) {
        val current = weatherData.current
        val daily = weatherData.daily

        hourHeaderText =
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(current.dt * 1000))
        fetch_timestamp.text = hourHeaderText
        if (hourHeaderVisible) text_view_header.text = hourHeaderText

        when (Unit.valueOf(unit)) {
            Unit.Metric -> {
                weather_description.text = getString(
                    R.string.txt_description_meter,
                    current.weather[0].description.capitalize(Locale.ROOT),
                    daily[0].temp.max.roundToInt().toString(),
                    resources.getStringArray(R.array.compass_directions)[((current.wind_deg % 360) / 22.5).roundToInt()],
                    current.wind_speed.roundToInt().toString()
                )

                current_temp.text =
                    getString(R.string.txt_celsius_temp, current.temp.roundToInt().toString())
                high_low_temp.text = getString(
                    R.string.txt_celsius_low_high_temp,
                    daily[0].temp.min.roundToInt().toString(),
                    daily[0].temp.max.roundToInt().toString()
                )

                feel_like_value.text =
                    getString(
                        R.string.txt_celsius_feel_like,
                        current.feels_like.roundToInt().toString()
                    )

                dew_point_value.text =
                    getString(
                        R.string.txt_celsius_dew_point,
                        current.dew_point.roundToInt().toString()
                    )
            }
            Unit.Imperial -> {
                weather_description.text = getString(
                    R.string.txt_description_imperial,
                    current.weather[0].description.capitalize(Locale.ROOT),
                    daily[0].temp.max.roundToInt().toString(),
                    resources.getStringArray(R.array.compass_directions)[((current.wind_deg % 360) / 22.5).roundToInt()],
                    current.wind_speed.roundToInt().toString()
                )

                current_temp.text =
                    getString(R.string.txt_fahrenheit_temp, current.temp.roundToInt().toString())
                high_low_temp.text = getString(
                    R.string.txt_fahrenheit_low_high_temp,
                    daily[0].temp.min.roundToInt().toString(),
                    daily[0].temp.max.roundToInt().toString()
                )

                feel_like_value.text =
                    getString(
                        R.string.txt_fahrenheit_feel_like,
                        current.feels_like.roundToInt().toString()
                    )

                dew_point_value.text =
                    getString(
                        R.string.txt_fahrenheit_dew_point,
                        current.dew_point.roundToInt().toString()
                    )
            }
        }

        location_name.text = addressHeaderText
        if (addressHeaderVisible) text_view_header.text = addressHeaderText

        Glide
            .with(this)
            .load("https://openweathermap.org/img/wn/${current.weather[0].icon}@2x.png")
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .into(weather_icon)

        weather_main.text = current.weather[0].main

        pressure_value.text = getString(R.string.txt_hpa_pressure, current.pressure.toString())
        humidity_value.text =
            getString(R.string.txt_percentage_humidity, current.humidity.toString())

        uvi_value.text = current.uvi.roundToInt().toString()
        if (current.visibility / 1000 > 0) {
            visibility_value.text =
                getString(
                    R.string.txt_kilometer_visibility,
                    String.format("%d", current.visibility / 1000)
                )
        } else {
            visibility_value.text =
                getString(R.string.txt_meter_visibility, current.visibility.toString())
        }

        listHourly.clear()
        listHourly.addAll(weatherData.hourly)
        listHourly.removeAt(0)
        hourlyAdapter =
            HourlyAdapter(requireContext(), listHourly, unit)
        recycler_view_hourly.adapter = hourlyAdapter
        hourlyAdapter.notifyDataSetChanged()

        listDaily.clear()
        listDaily.addAll(daily)
        listDaily.removeAt(0)
        generateDailyRecyclerView(requireContext())
        dailyAdapter.notifyDataSetChanged()
    }

    private fun generateHourlyRecyclerView(context: Context) {
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recycler_view_hourly.layoutManager = layoutManager
        recycler_view_hourly.itemAnimator = DefaultItemAnimator()
        recycler_view_hourly.addItemDecoration(
            DividerItemDecoration(
                context,
                layoutManager.orientation
            ).apply {
                setDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.item_divider_transparent_vertical
                    )!!
                )
            }
        )
    }

    private fun generateDailyRecyclerView(context: Context) {
        listMinTemp.clear()
        listMaxTemp.clear()

        for (day in listDaily) {
            listMaxTemp.add(day.temp.max)
            listMinTemp.add(day.temp.min)
        }

        val maxTemp = Collections.max(listMaxTemp)
        val minTemp = Collections.min(listMinTemp)

        val range = maxTemp - minTemp

        dailyAdapter = DailyAdapter(
            context,
            listDaily,
            maxTemp,
            minTemp,
            range,
            unit
        )
        val layoutManager = GridLayoutManager(context, listDaily.size)
        recycler_view_daily.layoutManager = layoutManager
        recycler_view_daily.itemAnimator = DefaultItemAnimator()
        recycler_view_daily.adapter = dailyAdapter
    }

    companion object {
        @JvmStatic
        fun newInstance(unit: String, owner: LifecycleOwner) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(unitParam, unit)
                }
                this.owner = owner
            }

        private var edtClickCount = 0

        private var hourHeaderVisible = false
        private var hourHeaderText = ""

        private var addressHeaderVisible = false
        private var addressHeaderText: String? = ""
    }
}