package com.tt.weatherapp.ui

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.tt.weatherapp.R
import com.tt.weatherapp.Unit
import kotlinx.android.synthetic.main.fragment_setting.*
import org.koin.android.ext.android.inject

private const val unitParam = "unit"

class BottomSettingFragment : BottomSheetDialogFragment() {
    private lateinit var unit: String
    private val sharedPref: SharedPreferences by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            unit = it.getString(unitParam).toString()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_setting, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        button_setting.setOnClickListener {
            (activity as MainActivity).checkIfLocationServiceEnable()
        }

        text_view_my_location.setOnClickListener {
            (activity as MainActivity).checkIfLocationServiceEnable()
        }

        radioGroupUnit.setOnCheckedChangeListener { _, checkedId ->
            if (!view.findViewById<RadioButton>(checkedId).isPressed) return@setOnCheckedChangeListener
            when (checkedId) {
                radioButtonMetric.id -> {
                    sharedPref.edit().putString("unit", Unit.Metric.toString()).apply()
                    (activity as MainActivity).refreshFragmentHome(Unit.Metric.toString())
                }
                radioButtonImperial.id -> {
                    sharedPref.edit().putString("unit", Unit.Imperial.toString()).apply()
                    (activity as MainActivity).refreshFragmentHome(Unit.Imperial.toString())
                }
            }
        }

        when (Unit.valueOf(unit)) {
            Unit.Metric -> {
                radioButtonMetric.isChecked = true
            }
            Unit.Imperial -> {
                radioButtonImperial.isChecked = true
            }
        }

        button_choose_language.setOnClickListener {
            val chooseLanguageDialogFragment: ChooseLanguageDialogFragment =
                ChooseLanguageDialogFragment.newInstance()
            chooseLanguageDialogFragment.show(
                (activity as MainActivity).supportFragmentManager,
                "exit_fragment"
            )
        }
    }

    companion object {
        fun newInstance(unit: String): BottomSettingFragment =
            BottomSettingFragment().apply {
                arguments = Bundle().apply {
                    putString(unitParam, unit)
                }
            }
    }

}