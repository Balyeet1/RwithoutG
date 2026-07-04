package com.example.rewardwithoutguilt.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.rewardwithoutguilt.data.FocusPreferences
import com.example.rewardwithoutguilt.data.NotificationPreferences
import com.example.rewardwithoutguilt.features.focus.FocusService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import android.os.Build

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d("NotificationReceiver", "Received intent with action: $action")

        if (Intent.ACTION_BOOT_COMPLETED == action) {
            rescheduleAlarms(context)
            startFocusServiceIfNeeded(context)
        } else {
            showReminder(context)
        }
    }

    private fun showReminder(context: Context) {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            val prefs = NotificationPreferences(context)
            val enabled = prefs.notificationsEnabled.first()
            if (enabled) {
                NotificationWorker.showNotification(context)
                
                // Reschedule for tomorrow
                val hour = prefs.notificationHour.first()
                val minute = prefs.notificationMinute.first()
                NotificationScheduler.scheduleNotification(context, hour, minute, true)
            }
        }
    }

    private fun rescheduleAlarms(context: Context) {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            val prefs = NotificationPreferences(context)
            val enabled = prefs.notificationsEnabled.first()
            val hour = prefs.notificationHour.first()
            val minute = prefs.notificationMinute.first()
            
            if (enabled) {
                NotificationScheduler.scheduleNotification(context, hour, minute, true)
            }
        }
    }

    private fun startFocusServiceIfNeeded(context: Context) {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            val focusPrefs = FocusPreferences(context)
            if (focusPrefs.isFocusEnabled.first()) {
                val intent = Intent(context, FocusService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            }
        }
    }
}
