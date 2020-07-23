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
import com.tt.weatherapp.model.Daily
import com.tt.weatherapp.util.DecimalFormat
import kotlinx.android.synthetic.main.item_daily_detail.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class DailyDetailAdapter(
    private val context: Context,
    private val listDaily: List<Daily>,
    private val unit: String
) :
    RecyclerView.Adapter<DailyDetailAdapter.ViewHolder>() {

    private val layoutInflater = LayoutInflater.from(context)

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(layoutInflater.inflate(R.layout.item_daily_detail, parent, false))
    }

    override fun getItemCount() = listDaily.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val day = listDaily[position]

        Glide
            .with(context)
            .load("https://openweathermap.org/img/wn/${day.weather[0].icon}@2x.png")
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .into(holder.itemView.weather_icon)

        holder.itemView.text_day.text =
            SimpleDateFormat("EEEE, MMMM dd", Locale.getDefault()).format(
                Date(
                    day.dt * 1000
                )
            )

        when (Unit.valueOf(unit)) {
            Unit.Metric -> {
                holder.itemView.text_high_low_temp.text =
                    context.getString(
                        R.string.txt_celsius_low_high_temp,
                        day.temp.min.roundToInt().toString(),
                        day.temp.max.roundToInt().toString()
                    )

                holder.itemView.text_wind_description.text = context.getString(
                    R.string.txt_wind_meter_per_sec_compass_direction,
                    DecimalFormat.format(day.wind_speed),
                    context.resources.getStringArray(R.array.compass_directions)[((day.wind_deg % 360) / 22.5).roundToInt()]
                )

                holder.itemView.text_day_description.text =
                    context.getString(
                        R.string.txt_metric_day_description,
                        SimpleDateFormat(
                            "HH:mm",
                            Locale.getDefault()
                        ).format(Date(day.sunrise * 1000)),
                        day.temp.day.roundToInt().toString(),
                        day.feels_like.day.roundToInt().toString()
                    )

                holder.itemView.text_night_description.text =
                    context.getString(
                        R.string.txt_metric_night_description,
                        SimpleDateFormat(
                            "HH:mm",
                            Locale.getDefault()
                        ).format(Date(day.sunset * 1000)),
                        day.temp.night.roundToInt().toString(),
                        day.feels_like.night.roundToInt().toString()
                    )
            }
            Unit.Imperial -> {
                holder.itemView.text_high_low_temp.text =
                    context.getString(
                        R.string.txt_fahrenheit_low_high_temp,
                        day.temp.min.roundToInt().toString(),
                        day.temp.max.roundToInt().toString()
                    )

                holder.itemView.text_wind_description.text = context.getString(
                    R.string.txt_wind_imperial_per_hour_compass_direction,
                    day.wind_speed.roundToInt().toString(),
                    context.resources.getStringArray(R.array.compass_directions)[((day.wind_deg % 360) / 22.5).roundToInt()]
                )

                holder.itemView.text_day_description.text =
                    context.getString(
                        R.string.txt_imperial_day_description,
                        SimpleDateFormat(
                            "HH:mm",
                            Locale.getDefault()
                        ).format(Date(day.sunrise * 1000)),
                        day.temp.day.roundToInt().toString(),
                        day.feels_like.day.roundToInt().toString()
                    )

                holder.itemView.text_night_description.text =
                    context.getString(
                        R.string.txt_imperial_night_description,
                        SimpleDateFormat(
                            "HH:mm",
                            Locale.getDefault()
                        ).format(Date(day.sunset * 1000)),
                        day.temp.night.roundToInt().toString(),
                        day.feels_like.night.roundToInt().toString()
                    )
            }
        }

        holder.itemView.text_uv_index.text = context.getString(
            R.string.txt_uvi_description,
            when {
                day.uvi <= 4 -> {
                    context.getString(R.string.txt_low)
                }
                day.uvi <= 8 -> {
                    context.getString(R.string.txt_medium)
                }
                else -> context.getString(R.string.txt_High)
            }
        )

        day.rain?.let {
            holder.itemView.text_rain.visibility = View.VISIBLE
            holder.itemView.text_rain.text =
                context.getString(R.string.txt_rain_volume, DecimalFormat.format(it))
        }

        day.snow?.let {
            holder.itemView.text_snow.visibility = View.VISIBLE
            holder.itemView.text_snow.text =
                context.getString(R.string.txt_snow_volume, DecimalFormat.format(it))
        }
    }
}