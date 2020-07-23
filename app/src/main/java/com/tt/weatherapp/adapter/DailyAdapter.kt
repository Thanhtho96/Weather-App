package com.tt.weatherapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.tt.weatherapp.R
import com.tt.weatherapp.Unit
import com.tt.weatherapp.model.Daily
import kotlinx.android.synthetic.main.item_daily_temp_graph.view.*
import java.util.*
import kotlin.math.roundToInt

class DailyAdapter(
    private val context: Context,
    private val listDaily: List<Daily>,
    private val maxTemp: Double,
    private val minTemp: Double,
    private val rangeTemp: Double,
    private val unit: String
) :
    RecyclerView.Adapter<DailyAdapter.ViewHolder>() {

    private val layoutInflater = LayoutInflater.from(context)
    private val array = context.resources.getStringArray(R.array.days_of_week)

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = layoutInflater.inflate(R.layout.item_daily_temp_graph, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = listDaily.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val daily = listDaily[position]

        val currentMaxTemp = daily.temp.max.roundToInt()
        val currentMinTemp = daily.temp.min.roundToInt()

        val containerHeight =
            (holder.itemView.container.layoutParams as GridLayoutManager.LayoutParams).height
        val highTempMarginBottom =
            (holder.itemView.high_temp.layoutParams as ConstraintLayout.LayoutParams).bottomMargin
        val iconHeight =
            (holder.itemView.ic_weather.layoutParams as ConstraintLayout.LayoutParams).height
        holder.itemView.high_temp.measure(0, 0)
        val textViewHeight = holder.itemView.high_temp.measuredHeight

        val spareSpaceForGraph =
            containerHeight - (highTempMarginBottom * 2 + iconHeight + textViewHeight * 3)

        val layoutParamsHighTemp =
            holder.itemView.high_temp.layoutParams as ConstraintLayout.LayoutParams

        layoutParamsHighTemp.topMargin =
            ((spareSpaceForGraph / rangeTemp) * (maxTemp - currentMaxTemp)).roundToInt()
        holder.itemView.high_temp.layoutParams = layoutParamsHighTemp

        val layoutParamsLowTemp =
            holder.itemView.low_temp.layoutParams as ConstraintLayout.LayoutParams

        layoutParamsLowTemp.bottomMargin =
            ((spareSpaceForGraph / rangeTemp) * (currentMinTemp - minTemp)).roundToInt()
        holder.itemView.low_temp.layoutParams = layoutParamsLowTemp

        when (Unit.valueOf(unit)) {
            Unit.Metric -> {
                holder.itemView.high_temp.text =
                    context.getString(
                        R.string.txt_celsius_temp,
                        daily.temp.max.roundToInt().toString()
                    )
                holder.itemView.low_temp.text =
                    context.getString(
                        R.string.txt_celsius_temp,
                        daily.temp.min.roundToInt().toString()
                    )

            }
            Unit.Imperial -> {
                holder.itemView.high_temp.text =
                    context.getString(
                        R.string.txt_temp_without_unit,
                        daily.temp.max.roundToInt().toString()
                    )
                holder.itemView.low_temp.text =
                    context.getString(
                        R.string.txt_temp_without_unit,
                        daily.temp.min.roundToInt().toString()
                    )
            }
        }

        Calendar.getInstance().apply {
            time = Date(daily.dt * 1000)
            holder.itemView.day_of_week.text = array[get(Calendar.DAY_OF_WEEK) - 1]
        }

        Glide
            .with(context)
            .load("https://openweathermap.org/img/wn/${daily.weather[0].icon}@2x.png")
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .into(holder.itemView.ic_weather)
    }
}
