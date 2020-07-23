package com.tt.weatherapp.ui

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.tt.weatherapp.R
import com.tt.weatherapp.Unit
import kotlinx.android.synthetic.main.fragment_main.*
import org.koin.android.ext.android.inject

class MainFragment : Fragment() {
    private lateinit var fm: FragmentManager
    private lateinit var fragmentHome: Fragment
    private lateinit var fragmentHourly: Fragment
    private lateinit var fragmentDaily: Fragment
    private lateinit var active: Fragment
    private lateinit var navView: BottomNavigationView
    private val sharedPref: SharedPreferences by inject()

    private val mOnNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    if (active != fragmentHome) {
                        fm.beginTransaction()
                            .setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right)
                            .hide(active).show(fragmentHome).commit()
                        active = fragmentHome
                    }
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_hourly_detail -> {
                    if (active != fragmentHourly) {
                        fm.beginTransaction().apply {
                            if (active == fragmentHome) {
                                setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left)
                            } else {
                                setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right)
                            }
                            hide(active).show(fragmentHourly).commit()
                        }
                        active = fragmentHourly
                    }
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_daily_detail -> {
                    if (active != fragmentDaily) {
                        fm.beginTransaction()
                            .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left)
                            .hide(active).show(fragmentDaily).commit()
                        active = fragmentDaily
                    }
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fm = childFragmentManager
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragmentHome =
            HomeFragment.newInstance(sharedPref.getString("unit", Unit.Imperial.toString())!!, this)
        fragmentHourly =
            HourlyDetailFragment.newInstance()
        fragmentDaily =
            DailyDetailFragment.newInstance()

        active = fragmentHome

        navView = nav_view
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        manipulateFragment()
    }

    private fun manipulateFragment() {
        fm.beginTransaction().add(R.id.nav_host_fragment, fragmentDaily, "daily")
            .hide(fragmentDaily).commit()
        fm.beginTransaction().add(R.id.nav_host_fragment, fragmentHourly, "hourly")
            .hide(fragmentHourly).commit()
        fm.beginTransaction().add(R.id.nav_host_fragment, fragmentHome, "home").commit()
    }

    internal fun showHomeFragment() {
        fm.beginTransaction().show(fragmentHome).commit()
    }

    internal fun showHourlyDetailFragment() {
        fm.beginTransaction().setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left)
            .hide(active).show(fragmentHourly).commit()
        active = fragmentHourly
        navView.selectedItemId = R.id.navigation_hourly_detail
    }

    internal fun showDailyDetailFragment() {
        fm.beginTransaction().setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left)
            .hide(active).show(fragmentDaily).commit()
        active = fragmentDaily
        navView.selectedItemId = R.id.navigation_daily_detail
    }

    companion object {
        @JvmStatic
        fun newInstance() = MainFragment()
    }
}