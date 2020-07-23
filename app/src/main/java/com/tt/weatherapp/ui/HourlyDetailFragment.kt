package com.tt.weatherapp.ui

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.tt.weatherapp.R
import com.tt.weatherapp.Unit
import com.tt.weatherapp.adapter.HourlyDetailAdapter
import com.tt.weatherapp.model.HourlyCustom
import com.tt.weatherapp.viewmodel.WeatherViewModel
import kotlinx.android.synthetic.main.fragment_hourly_detail.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.text.SimpleDateFormat
import java.util.*

class HourlyDetailFragment : Fragment() {

    private val sharedPref: SharedPreferences by inject()
    private var listHourlyCustom: MutableList<HourlyCustom> = ArrayList()
    private val weatherViewModel: WeatherViewModel by sharedViewModel(from = { requireActivity() })
    private lateinit var hourlyDetailAdapter: HourlyDetailAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_hourly_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        generateRecyclerView()

        var dayHeader = 0

        weatherViewModel.weatherDataLiveData.observe(viewLifecycleOwner, Observer {
            listHourlyCustom.clear()
            lifecycleScope.launch(Dispatchers.IO) {
                for (hourly in it.hourly) {
                    var hourlyCustom: HourlyCustom
                    val currentDay =
                        SimpleDateFormat("dd", Locale.getDefault()).format(hourly.dt * 1000).toInt()

                    when {
                        dayHeader == 0 -> {
                            dayHeader = currentDay
                            hourlyCustom = HourlyCustom(hourly, true)
                        }
                        currentDay != dayHeader -> {
                            dayHeader = currentDay
                            hourlyCustom = HourlyCustom(hourly, true)
                        }
                        else -> {
                            hourlyCustom = HourlyCustom(hourly, false)
                        }
                    }
                    listHourlyCustom.add(hourlyCustom)
                }

                hourlyDetailAdapter =
                    HourlyDetailAdapter(
                        requireContext(),
                        listHourlyCustom,
                        Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
                        sharedPref.getString("unit", Unit.Imperial.toString())!!
                    )
                withContext(Dispatchers.Main) {
                    recycler_view_hours_detail.adapter = hourlyDetailAdapter
                    hourlyDetailAdapter.notifyDataSetChanged()
                }
            }
        })
    }

    private fun generateRecyclerView() {
        val layoutManager = LinearLayoutManager(context)
        recycler_view_hours_detail.layoutManager = layoutManager
        recycler_view_hours_detail.itemAnimator = DefaultItemAnimator()
        recycler_view_hours_detail.addItemDecoration(
            DividerItemDecoration(
                context,
                layoutManager.orientation
            ).apply {
                setDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.item_divider_transparent_5dp_horizontal
                    )!!
                )
            }
        )
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            HourlyDetailFragment()
    }
}