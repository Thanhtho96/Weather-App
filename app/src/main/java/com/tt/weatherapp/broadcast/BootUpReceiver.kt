package com.tt.weatherapp.broadcast

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.util.*

private const val PUSH_NOTIFICATION_MORNING = 77777

class BootUpReceiver : BroadcastReceiver() {
    private var alarmManager: AlarmManager? = null

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action === "android.intent.action.BOOT_COMPLETED") {
            alarmManager =
                context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

            val myIntent = Intent(context, PushWeatherNotificationReceiver::class.java).apply {
            }

            val pendingIntentMorning = PendingIntent.getBroadcast(
                context, PUSH_NOTIFICATION_MORNING, myIntent, PendingIntent.FLAG_UPDATE_CURRENT
            )

            val now = Calendar.getInstance()

            val calendarMorning: Calendar = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, 6)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }

            if (now.after(calendarMorning)) calendarMorning.add(Calendar.DATE, 1)

            alarmManager?.setInexactRepeating(
                AlarmManager.RTC,
                calendarMorning.timeInMillis,
                AlarmManager.INTERVAL_HALF_DAY,
                pendingIntentMorning
            )
        }
    }
}