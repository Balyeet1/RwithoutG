package com.example.rewardwithoutguilt.more

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.rewardwithoutguilt.BuildConfig
import com.example.rewardwithoutguilt.R
import com.example.rewardwithoutguilt.data.NotificationPreferences
import com.example.rewardwithoutguilt.notifications.NotificationScheduler
import kotlinx.coroutines.launch
import java.util.Locale
import androidx.compose.ui.tooling.preview.Preview
import com.example.rewardwithoutguilt.ui.theme.RewardWithoutGuiltTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember { NotificationPreferences(context) }

    val notificationsEnabled by prefs.notificationsEnabled.collectAsState(initial = true)
    val hour by prefs.notificationHour.collectAsState(initial = 21)
    val minute by prefs.notificationMinute.collectAsState(initial = 0)

    var showTimePicker by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            scope.launch {
                prefs.updateNotificationSettings(true, hour, minute)
                NotificationScheduler.scheduleNotification(context, hour, minute, true)
            }
        }
    }

    NotificationSettingsScreenContent(
        notificationsEnabled = notificationsEnabled,
        hour = hour,
        minute = minute,
        showTimePicker = showTimePicker,
        onShowTimePickerChange = { showTimePicker = it },
        onToggleNotifications = { enabled ->
            if (enabled) {
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    scope.launch {
                        prefs.updateNotificationSettings(true, hour, minute)
                        NotificationScheduler.scheduleNotification(context, hour, minute, true)
                    }
                }
            } else {
                scope.launch {
                    prefs.updateNotificationSettings(enabled, hour, minute)
                    NotificationScheduler.scheduleNotification(context, hour, minute, enabled)
                }
            }
        },
        onTimeSelected = { newHour, newMinute ->
            scope.launch {
                prefs.updateNotificationSettings(notificationsEnabled, newHour, newMinute)
                NotificationScheduler.scheduleNotification(context, newHour, newMinute, notificationsEnabled)
            }
            showTimePicker = false
        },
        onDebugTrigger = {
            NotificationScheduler.debugPushNotification(context)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreenContent(
    notificationsEnabled: Boolean,
    hour: Int,
    minute: Int,
    showTimePicker: Boolean,
    onShowTimePickerChange: (Boolean) -> Unit,
    onToggleNotifications: (Boolean) -> Unit,
    onTimeSelected: (Int, Int) -> Unit,
    onDebugTrigger: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // First Camp: Active/Inactive Toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.notification_enable),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Switch(
                checked = notificationsEnabled,
                onCheckedChange = onToggleNotifications
            )
        }

        HorizontalDivider()

        // Second Camp: Notification Time
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onShowTimePickerChange(true) }
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.notification_time),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = String.format(Locale.getDefault(), "%02d:%02d", hour, minute),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        HorizontalDivider()

        // Debug Feature: Push Notification Button
        if (BuildConfig.FLAVOR == "dev" && BuildConfig.BUILD_TYPE == "debug") {
            TextButton(
                onClick = onDebugTrigger,
                enabled = notificationsEnabled,
                modifier = Modifier
                    .padding(top = 16.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                Text("Debug: Push Notification Now")
            }
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = hour,
            initialMinute = minute,
            is24Hour = true
        )

        AlertDialog(
            onDismissRequest = { onShowTimePickerChange(false) },
            confirmButton = {
                TextButton(onClick = {
                    onTimeSelected(timePickerState.hour, timePickerState.minute)
                }) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { onShowTimePickerChange(false) }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    TimePicker(state = timePickerState)
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun NotificationSettingsScreenPreview() {
    RewardWithoutGuiltTheme {
        NotificationSettingsScreenContent(
            notificationsEnabled = true,
            hour = 21,
            minute = 15,
            showTimePicker = false,
            onShowTimePickerChange = {},
            onToggleNotifications = {},
            onTimeSelected = { _, _ -> },
            onDebugTrigger = {}
        )
    }
}

