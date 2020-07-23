package com.tt.weatherapp.util

import java.text.DecimalFormat

object DecimalFormat {
    fun format(doubleValue: Double): String {
        val df = DecimalFormat("#.##")
        return df.format(doubleValue)
    }

    fun formatGetDistance(doubleValue: Double): String {
        val df = DecimalFormat("#")
        return df.format(doubleValue)
    }
}