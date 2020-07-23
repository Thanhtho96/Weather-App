package com.tt.weatherapp.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.tt.weatherapp.R
import kotlinx.android.synthetic.main.fragment_rating_dialog.*

class RatingDialogFragment : DialogFragment(), View.OnClickListener {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_rating_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
            button_ok.isEnabled = rating != 0f
        }

        button_ok.setOnClickListener(this)
        button_cancel.setOnClickListener(this)


    }

    override fun onClick(v: View?) {
        when (v) {
            button_ok -> {
                val appPackageName: String = requireContext().packageName

                if (ratingBar.rating < 5) {
                    val sendMailIntent: Intent = Intent().apply {
                        action = Intent.ACTION_SEND_MULTIPLE
                        putExtra(
                            Intent.EXTRA_TEXT,
                            resources.getQuantityString(
                                R.plurals.text_please_write_review,
                                ratingBar.rating.toInt(),
                                ratingBar.rating.toInt()
                            )
                        )
                        putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.system_email)))
                        putExtra(
                            Intent.EXTRA_SUBJECT,
                            getString(
                                R.string.text_email_review_subject,
                                getString(R.string.app_name)
                            )
                        )
                        type = "text/plain"
                    }
                    requireActivity().startActivityForResult(
                        sendMailIntent,
                        MainActivity.RATING_REQUEST
                    )
                } else {
                    try {
                        requireActivity().startActivityForResult(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("market://details?id=$appPackageName")
                            ), MainActivity.RATING_REQUEST
                        )
                    } catch (activityNotFoundException: ActivityNotFoundException) {
                        requireActivity().startActivityForResult(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
                            ), MainActivity.RATING_REQUEST
                        )
                    }
                }
            }
        }
        dismiss()
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            RatingDialogFragment()
    }
}