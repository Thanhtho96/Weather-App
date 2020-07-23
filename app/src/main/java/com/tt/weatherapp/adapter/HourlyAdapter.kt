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
import com.tt.weatherapp.model.Hourly
import kotlinx.android.synthetic.main.item_hourly.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class HourlyAdapter(
    private val context: Context,
    private val listHourly: List<Hourly>,
    private val unit: String
) :
    RecyclerView.Adapter<HourlyAdapter.ViewHolder>() {

    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = layoutInflater.inflate(R.layout.item_hourly, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = listHourly.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val hourly = listHourly[position]

        when (Unit.valueOf(unit)) {
            Unit.Metric -> {
                holder.itemView.temp_hourly.text =
                    context.getString(
                        R.string.txt_celsius_temp,
                        hourly.temp.roundToInt().toString()
                    )
            }
            Unit.Imperial -> {
                holder.itemView.temp_hourly.text =
                    context.getString(
                        R.string.txt_fahrenheit_temp,
                        hourly.temp.roundToInt().toString()
                    )
            }
        }

        Glide
            .with(context)
            .load("https://openweathermap.org/img/wn/${hourly.weather[0].icon}@2x.png")
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .into(holder.itemView.icon_weather_hourly)

        holder.itemView.timestamp_hourly.text =
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(hourly.dt * 1000))
    }
}