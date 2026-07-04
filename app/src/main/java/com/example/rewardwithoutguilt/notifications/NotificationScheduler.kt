package com.example.rewardwithoutguilt.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.rewardwithoutguilt.BuildConfig
import java.util.Calendar

object NotificationScheduler {
    private const val TAG = "NotificationScheduler"
    private const val REQUEST_CODE = 1001

    fun scheduleNotification(context: Context, hour: Int, minute: Int, enabled: Boolean) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (!enabled) {
            Log.d(TAG, "Notifications disabled, cancelling alarm")
            alarmManager.cancel(pendingIntent)
            return
        }

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // If the time is in the past, schedule for tomorrow
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        Log.d(TAG, "Scheduling alarm for ${calendar.time}. Current time: ${System.currentTimeMillis()}")

        // Use setAndAllowWhileIdle to ensure it triggers even in Doze mode
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }

    fun debugPushNotification(context: Context) {
        if (BuildConfig.FLAVOR == "dev" && BuildConfig.BUILD_TYPE == "debug") {
            NotificationWorker.showNotification(context)
        }
    }
}
