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
import com.tt.weatherapp.adapter.DailyDetailAdapter
import com.tt.weatherapp.viewmodel.WeatherViewModel
import kotlinx.android.synthetic.main.fragment_daily_detail.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class DailyDetailFragment : Fragment() {

    private val sharedPref: SharedPreferences by inject()
    private val weatherViewModel: WeatherViewModel by sharedViewModel(from = { requireActivity() })
    private lateinit var dailyDetailAdapter: DailyDetailAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_daily_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        generateRecyclerView()

        weatherViewModel.weatherDataLiveData.observe(viewLifecycleOwner, Observer {
            lifecycleScope.launch(Dispatchers.IO) {
                dailyDetailAdapter =
                    DailyDetailAdapter(
                        requireContext(),
                        it.daily,
                        sharedPref.getString("unit", Unit.Imperial.toString())!!
                    )
                withContext(Dispatchers.Main) {
                    recycler_view_days.adapter = dailyDetailAdapter
                    dailyDetailAdapter.notifyDataSetChanged()
                }
            }
        })
    }

    private fun generateRecyclerView() {
        val layoutManager = LinearLayoutManager(context)
        recycler_view_days.layoutManager = layoutManager
        recycler_view_days.itemAnimator = DefaultItemAnimator()
        recycler_view_days.addItemDecoration(
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
            DailyDetailFragment()
    }
}