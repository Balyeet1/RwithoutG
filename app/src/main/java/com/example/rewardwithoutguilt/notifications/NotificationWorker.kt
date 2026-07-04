package com.example.rewardwithoutguilt.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.rewardwithoutguilt.BuildConfig
import com.example.rewardwithoutguilt.MainActivity
import com.example.rewardwithoutguilt.R
import com.example.rewardwithoutguilt.data.NotificationPreferences
import com.example.rewardwithoutguilt.util.Constants
import kotlinx.coroutines.flow.first

class NotificationWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("NotificationWorker", "doWork triggered")
        
        val prefs = NotificationPreferences(applicationContext)
        val enabled = prefs.notificationsEnabled.first()
        
        if (enabled) {
            showNotification(applicationContext)
        }

        // Schedule the next reminder for tomorrow
        val hour = prefs.notificationHour.first()
        val minute = prefs.notificationMinute.first()
        NotificationScheduler.scheduleNotification(applicationContext, hour, minute, enabled)

        return Result.success()
    }

    companion object {
        private const val TAG = "NotificationWorker"
        fun showNotification(context: Context) {
            Log.d(TAG, "showNotification called")
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    Constants.NOTIFICATION_CHANNEL_ID,
                    context.getString(R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = context.getString(R.string.notification_channel_description)
                }
                notificationManager.createNotificationChannel(channel)
            }

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
            )

            val titlePrefix = if (BuildConfig.FLAVOR == "dev" && BuildConfig.BUILD_TYPE == "debug") "Debug: " else ""
            val title = titlePrefix + context.getString(R.string.notification_title)

            val notification = NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher) // Use app icon as placeholder
                .setContentTitle(title)
                .setContentText(context.getString(R.string.notification_content))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            notificationManager.notify(Constants.NOTIFICATION_ID, notification)
        }
    }
}
