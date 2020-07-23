package com.tt.weatherapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.tt.weatherapp.R
import kotlinx.android.synthetic.main.fragment_exit_dialog.*

class ExitDialogFragment : DialogFragment(), View.OnClickListener {

    private lateinit var mListener: OnButtonChoiceClickListener

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_exit_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        button_yes.setOnClickListener(this)
        button_no.setOnClickListener(this)
    }

    interface OnButtonChoiceClickListener {
        fun onYesClick()
    }

    override fun onClick(v: View?) {
        when (v) {
            button_yes -> {
                mListener.onYesClick()
            }
        }
        dismiss()
    }

    companion object {
        @JvmStatic
        fun newInstance(onButtonChoiceClickListener: OnButtonChoiceClickListener) =
            ExitDialogFragment().apply {
                mListener = onButtonChoiceClickListener
            }
    }
}