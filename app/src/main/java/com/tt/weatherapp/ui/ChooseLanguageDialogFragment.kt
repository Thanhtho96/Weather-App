package com.tt.weatherapp.ui

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.fragment.app.DialogFragment
import com.tt.weatherapp.R
import kotlinx.android.synthetic.main.fragment_choose_languge_dialog.*
import org.koin.android.ext.android.inject

class ChooseLanguageDialogFragment : DialogFragment(), View.OnClickListener {

    private val sharedPref: SharedPreferences by inject()
    private lateinit var chosenLang: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_choose_languge_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        radioGroupLanguage.setOnCheckedChangeListener { _: RadioGroup?, checkedId: Int ->
            if (!view.findViewById<RadioButton>(checkedId).isPressed) return@setOnCheckedChangeListener
            when (checkedId) {
                radioButtonEnglish.id -> {
                    chosenLang = "en"
                }
                radioButtonVietnamese.id -> {
                    chosenLang = "vi"
                }
            }
        }

        when (sharedPref.getString("language", "en")) {
            "en" -> {
                chosenLang = "en"
                radioButtonEnglish.isChecked = true
            }
            "vi" -> {
                chosenLang = "vi"
                radioButtonVietnamese.isChecked = true
            }
        }

        button_ok.setOnClickListener(this)
        button_cancel.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v) {
            button_ok -> {
                if (chosenLang != sharedPref.getString("language", "en")) {
                    sharedPref.edit().putString("language", chosenLang).apply()
                    (activity as SettingActivity).reLaunchActivity()
                    return
                } else {
                    dismiss()
                    return
                }
            }
        }
        dismiss()
        return
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            ChooseLanguageDialogFragment()
    }
}