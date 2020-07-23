package com.tt.weatherapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.tt.weatherapp.R
import com.tt.weatherapp.Unit
import com.tt.weatherapp.model.HourlyCustom
import com.tt.weatherapp.util.DecimalFormat
import kotlinx.android.synthetic.main.item_hourly_detail.view.*
import kotlinx.android.synthetic.main.item_hourly_detail_header.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

private const val HEADER = 0
private const val NORMAL = 1

class HourlyDetailAdapter(
    private val context: Context,
    private val listHourly: List<HourlyCustom>,
    private val today: Int,
    private val unit: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val layoutInflater = LayoutInflater.from(context)

    class HourlyHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    class HourlyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View
        return when (viewType) {
            HEADER -> {
                view = layoutInflater.inflate(R.layout.item_hourly_detail_header, parent, false)
                HourlyHeaderViewHolder(view)
            }
            else -> {
                view = layoutInflater.inflate(R.layout.item_hourly_detail, parent, false)
                HourlyViewHolder(view)
            }
        }
    }

    override fun getItemCount() = listHourly.size

    @ExperimentalStdlibApi
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val hourly = listHourly[position].hourly
        when (holder.itemViewType) {
            HEADER -> {
                holder as HourlyHeaderViewHolder

                Calendar.getInstance().apply {
                    time = Date(hourly.dt * 1000)
                    holder.itemView.text_day_header.text =
                        if (today == get(Calendar.DAY_OF_MONTH))
                            context.getString(R.string.txt_today)
                        else {
                            SimpleDateFormat("EEEE, MMMM dd", Locale.getDefault()).format(
                                Date(
                                    hourly.dt * 1000
                                )
                            )
                        }
                }

                holder.itemView.text_hour_header.text =
                    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(hourly.dt * 1000))

                Glide
                    .with(context)
                    .load("https://openweathermap.org/img/wn/${hourly.weather[0].icon}@2x.png")
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .into(holder.itemView.weather_icon_header)

                when (Unit.valueOf(unit)) {
                    Unit.Metric -> {
                        holder.itemView.text_temperature_header.text = context.getString(
                            R.string.txt_celsius_temp_and_feel_like,
                            hourly.temp.roundToInt().toString(),
                            hourly.feels_like.roundToInt().toString()
                        )

                        holder.itemView.text_wind_description_header.text = context.getString(
                            R.string.txt_wind_meter_per_sec_compass_direction,
                            hourly.wind_speed.roundToInt().toString(),
                            context.resources.getStringArray(R.array.compass_directions)[((hourly.wind_deg % 360) / 22.5).roundToInt()]
                        )
                    }
                    Unit.Imperial -> {
                        holder.itemView.text_temperature_header.text = context.getString(
                            R.string.txt_fahrenheit_temp_and_feel_like,
                            hourly.temp.roundToInt().toString(),
                            hourly.feels_like.roundToInt().toString()
                        )

                        holder.itemView.text_wind_description_header.text = context.getString(
                            R.string.txt_wind_imperial_per_hour_compass_direction,
                            hourly.wind_speed.roundToInt().toString(),
                            context.resources.getStringArray(R.array.compass_directions)[((hourly.wind_deg % 360) / 22.5).roundToInt()]
                        )
                    }
                }

                holder.itemView.text_description_header.text =
                    hourly.weather[0].description.capitalize(
                        Locale.ROOT
                    )

                hourly.rain?.let {
                    holder.itemView.text_rain_header.visibility = View.VISIBLE
                    holder.itemView.text_rain_header.text = context.getString(
                        R.string.txt_rain_volume,
                        DecimalFormat.format(it.oneHour)
                    )
                }

                hourly.snow?.let {
                    holder.itemView.text_snow_header.visibility = View.VISIBLE
                    holder.itemView.text_snow_header.text = context.getString(
                        R.string.txt_snow_volume,
                        DecimalFormat.format(it.oneHour)
                    )
                }
            }
            else -> {
                holder as HourlyViewHolder

                holder.itemView.text_hour.text =
                    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(hourly.dt * 1000))

                Glide
                    .with(context)
                    .load("https://openweathermap.org/img/wn/${hourly.weather[0].icon}@2x.png")
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .into(holder.itemView.weather_icon)

                when (Unit.valueOf(unit)) {
                    Unit.Metric -> {
                        holder.itemView.text_temperature.text = context.getString(
                            R.string.txt_celsius_temp_and_feel_like,
                            hourly.temp.roundToInt().toString(),
                            hourly.feels_like.roundToInt().toString()
                        )

                        holder.itemView.text_wind_description.text = context.getString(
                            R.string.txt_wind_meter_per_sec_compass_direction,
                            DecimalFormat.format(hourly.wind_speed),
                            context.resources.getStringArray(R.array.compass_directions)[((hourly.wind_deg % 360) / 22.5).roundToInt()]
                        )
                    }
                    Unit.Imperial -> {
                        holder.itemView.text_temperature.text = context.getString(
                            R.string.txt_fahrenheit_temp_and_feel_like,
                            hourly.temp.roundToInt().toString(),
                            hourly.feels_like.roundToInt().toString()
                        )

                        holder.itemView.text_wind_description.text = context.getString(
                            R.string.txt_wind_imperial_per_hour_compass_direction,
                            hourly.wind_speed.roundToInt().toString(),
                            context.resources.getStringArray(R.array.compass_directions)[((hourly.wind_deg % 360) / 22.5).roundToInt()]
                        )
                    }
                }

                holder.itemView.text_description.text = hourly.weather[0].description.capitalize(
                    Locale.ROOT
                )

                hourly.rain?.let {
                    holder.itemView.text_rain.visibility = View.VISIBLE
                    holder.itemView.text_rain.text = context.getString(
                        R.string.txt_rain_volume,
                        DecimalFormat.format(it.oneHour)
                    )
                }

                hourly.snow?.let {
                    holder.itemView.text_snow.visibility = View.VISIBLE
                    holder.itemView.text_snow.text = context.getString(
                        R.string.txt_snow_volume,
                        DecimalFormat.format(it.oneHour)
                    )
                }
            }
        }
    }

    override fun getItemViewType(position: Int) =
        if (listHourly[position].header) HEADER
        else NORMAL
}