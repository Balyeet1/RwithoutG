package com.example.rewardwithoutguilt.more

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.example.rewardwithoutguilt.BuildConfig
import com.example.rewardwithoutguilt.R
import com.example.rewardwithoutguilt.data.TaskPreferences
import com.example.rewardwithoutguilt.ui.theme.RewardWithoutGuiltTheme
import kotlinx.coroutines.launch

@Composable
fun MoreMenu(
    onDismissRequest: () -> Unit,
    onNavigateToEditGreeting: () -> Unit,
    onNavigateToNotificationSettings: () -> Unit,
    onNavigateToFocus: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val taskPrefs = remember { TaskPreferences(context) }

    Popup(
        alignment = Alignment.TopEnd,
        offset = IntOffset(0, 150),
        onDismissRequest = onDismissRequest,
        properties = PopupProperties(focusable = true)
    ) {
        Surface(
            modifier = modifier
                .width(280.dp)
                .padding(end = 16.dp),
            shape = RoundedCornerShape(12.dp),
            tonalElevation = 8.dp,
            shadowElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                UserHeader()
                HorizontalDivider()
                MenuItem(
                    text = stringResource(R.string.menu_change_greeting),
                    onClick = {
                        onDismissRequest()
                        onNavigateToEditGreeting()
                    }
                )
                MenuItem(
                    text = stringResource(R.string.menu_notification_settings),
                    onClick = {
                        onDismissRequest()
                        onNavigateToNotificationSettings()
                    }
                )
                MenuItem(
                    text = stringResource(R.string.menu_focus),
                    onClick = {
                        onDismissRequest()
                        onNavigateToFocus()
                    }
                )
                if (BuildConfig.FLAVOR == "dev" && BuildConfig.BUILD_TYPE == "debug") {
                    MenuItem(
                        text = "Debug: Fast Forward 24h",
                        onClick = {
                            scope.launch {
                                taskPrefs.debugFastForward24h()
                                onDismissRequest()
                            }
                        }
                    )
                }
                HorizontalDivider()
                TextButton(
                    onClick = onDismissRequest,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(end = 8.dp)
                ) {
                    Text(stringResource(R.string.close))
                }
            }
        }
    }
}

@Composable
private fun UserHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    MaterialTheme.colorScheme.primaryContainer,
                    RoundedCornerShape(16.dp)
                )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.user_name_placeholder),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = stringResource(R.string.signed_in),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun MenuItem(
    text: String,
    shortcut: String? = null,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        if (shortcut != null) {
            Text(
                text = shortcut,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MoreMenuPreview() {
    RewardWithoutGuiltTheme {
        Box(modifier = Modifier.fillMaxWidth()) {
            MoreMenu(
                onDismissRequest = {},
                onNavigateToEditGreeting = {},
                onNavigateToNotificationSettings = {},
                onNavigateToFocus = {}
            )
        }
    }
}
