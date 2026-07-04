package com.example.rewardwithoutguilt.features.focus

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.example.rewardwithoutguilt.R
import com.example.rewardwithoutguilt.data.FocusPreferences
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

class FocusService : Service() {
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var monitoringJob: Job? = null
    private lateinit var focusPrefs: FocusPreferences
    private lateinit var usageStatsManager: UsageStatsManager

    override fun onCreate() {
        super.onCreate()
        focusPrefs = FocusPreferences(this)
        usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())
        startMonitoring()
        return START_STICKY
    }

    private fun startMonitoring() {
        monitoringJob?.cancel()
        monitoringJob = serviceScope.launch {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            while (isActive) {
                val isEnabled = focusPrefs.isFocusEnabled.first()
                if (!isEnabled) {
                    stopSelf()
                    break
                }

                // Timer Logic
                val endTime = focusPrefs.timerEndTime.first()
                if (endTime > 0) {
                    val remaining = endTime - System.currentTimeMillis()
                    if (remaining <= 0) {
                        focusPrefs.setFocusEnabled(false)
                        showSessionEndedNotification()
                        stopSelf()
                        break
                    }
                    updateForegroundNotification(remaining)
                }

                if (powerManager.isInteractive) {
                    val blockedApps = focusPrefs.blockedApps.first()
                    val currentApp = getForegroundApp()

                    // Check if the current app is in the blocked list and is not our own app
                    if (currentApp != null && blockedApps.contains(currentApp) && currentApp != packageName) {
                        launchBlockScreen()
                    }
                }

                delay(1000) // Poll every 1 second to keep timer accurate and responsive
            }
        }
    }

    private fun updateForegroundNotification(remainingMs: Long) {
        val seconds = (remainingMs / 1000) % 60
        val minutes = (remainingMs / (1000 * 60)) % 60
        val hours = (remainingMs / (1000 * 60 * 60))
        
        val timeStr = if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }

        val notification = createNotification("Focus Mode: $timeStr remaining")
        val manager = getSystemService(NotificationManager::class.java)
        manager?.notify(NOTIFICATION_ID, notification)
    }

    private fun showSessionEndedNotification() {
        val manager = getSystemService(NotificationManager::class.java)
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Focus Mode Ended")
            .setContentText("Well done! Your focus session has finished.")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        manager?.notify(1002, notification)
    }

    private fun getForegroundApp(): String? {
        val time = System.currentTimeMillis()
        val stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 10000, time)
        if (stats.isNullOrEmpty()) return null
        
        return stats.maxByOrNull { it.lastTimeUsed }?.packageName
    }

    private fun launchBlockScreen() {
        val intent = Intent(this, BlockedAppActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        startActivity(intent)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Focus Mode",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Running focus mode to block distracting apps"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun createNotification(contentText: String = "Monitoring apps to keep you focused"): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Focus Mode is ON")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        monitoringJob?.cancel()
        serviceScope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "focus_service_channel"
    }
}
