package com.example.rewardwithoutguilt.features.focus

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import com.example.rewardwithoutguilt.BuildConfig
import com.example.rewardwithoutguilt.R
import com.example.rewardwithoutguilt.data.FocusPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.rewardwithoutguilt.ui.theme.RewardWithoutGuiltTheme


data class FocusAppInfo(
    val name: String,
    val packageName: String,
    val icon: Drawable?
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusPrefs = remember { FocusPreferences(context) }

    val isFocusEnabled by focusPrefs.isFocusEnabled.collectAsState(initial = false)
    val blockedApps by focusPrefs.blockedApps.collectAsState(initial = emptySet())
    val timerEndTime by focusPrefs.timerEndTime.collectAsState(initial = 0L)
    val savedDurationMs by focusPrefs.timerDuration.collectAsState(initial = 25 * 60 * 1000L)

    var appsList by remember { mutableStateOf<List<FocusAppInfo>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var isLoadingApps by remember { mutableStateOf(true) }
    var showBreakingFocusDialog by remember { mutableStateOf(false) }
    var onConfirmBreakFocus by remember { mutableStateOf<(() -> Unit)?>(null) }

    // Fetch apps
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val pm = context.packageManager
            val mainIntent = Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER)
            val resolvedInfos = pm.queryIntentActivities(mainIntent, PackageManager.ResolveInfoFlags.of(0L))

            appsList = resolvedInfos.map { info ->
                FocusAppInfo(
                    name = info.loadLabel(pm).toString(),
                    packageName = info.activityInfo.packageName,
                    icon = info.loadIcon(pm)
                )
            }.filter { it.packageName != context.packageName }
                .distinctBy { it.packageName }
                .sortedBy { it.name }

            isLoadingApps = false
        }
    }

    val filteredApps = remember(appsList, searchQuery) {
        if (searchQuery.isBlank()) appsList
        else appsList.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    FocusScreenContent(
        isFocusEnabled = isFocusEnabled,
        blockedApps = blockedApps,
        timerEndTime = timerEndTime,
        savedDurationMs = savedDurationMs,
        filteredApps = filteredApps,
        searchQuery = searchQuery,
        onSearchQueryChange = { searchQuery = it },
        isLoadingApps = isLoadingApps,
        onStartTimer = { duration ->
            val hasUsage = hasUsageStatsPermission(context)
            val hasOverlay = hasOverlayPermission(context)

            if (!hasUsage || !hasOverlay) {
                if (!hasUsage) {
                    context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                } else {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        "package:${context.packageName}".toUri()
                    )
                    context.startActivity(intent)
                }
            } else {
                scope.launch {
                    focusPrefs.setTimer(duration, System.currentTimeMillis())
                    focusPrefs.setFocusEnabled(true)
                    toggleFocusService(context, true)
                }
            }
        },
        onResetTimer = {
            onConfirmBreakFocus = {
                scope.launch {
                    focusPrefs.clearTimer()
                    focusPrefs.setFocusEnabled(false)
                    toggleFocusService(context, false)
                }
            }
            showBreakingFocusDialog = true
        },
        onDurationChange = { newDuration ->
            scope.launch {
                focusPrefs.setTimer(newDuration, null)
            }
        },
        onToggleFocus = { enabled ->
            if (enabled) {
                val hasUsage = hasUsageStatsPermission(context)
                val hasOverlay = hasOverlayPermission(context)

                if (!hasUsage || !hasOverlay) {
                    if (!hasUsage) {
                        context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                    } else {
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            "package:${context.packageName}".toUri()
                        )
                        context.startActivity(intent)
                    }
                } else {
                    scope.launch {
                        focusPrefs.setFocusEnabled(true)
                        toggleFocusService(context, true)
                    }
                }
            } else {
                onConfirmBreakFocus = {
                    scope.launch {
                        focusPrefs.setFocusEnabled(false)
                        toggleFocusService(context, false)
                    }
                }
                showBreakingFocusDialog = true
            }
        },
        onToggleAppBlocked = { app, isBlocked ->
            scope.launch {
                if (isBlocked) {
                    focusPrefs.addBlockedApp(app.packageName)
                } else {
                    focusPrefs.removeBlockedApp(app.packageName)
                }
            }
        },
        showBreakingFocusDialog = showBreakingFocusDialog,
        onConfirmBreakFocusAction = onConfirmBreakFocus,
        onDismissBreakingDialog = { showBreakingFocusDialog = false },
        onConfirmBreakingDialog = {
            onConfirmBreakFocus?.invoke()
            showBreakingFocusDialog = false
        },
        onBack = onBack
    )
}


@Composable
fun FocusTimerSection(
    savedDurationMs: Long,
    timerEndTime: Long,
    onStart: (Long) -> Unit,
    onReset: () -> Unit,
    onDurationChange: (Long) -> Unit
) {
    var currentTimeMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var isEditing by remember { mutableStateOf(false) }

    LaunchedEffect(timerEndTime) {
        while (timerEndTime > 0) {
            currentTimeMs = System.currentTimeMillis()
            delay(500)
        }
    }

    val isRunning = timerEndTime > currentTimeMs
    val displayMs = if (isRunning) timerEndTime - currentTimeMs else savedDurationMs

    val hours = (displayMs / (1000 * 60 * 60)).toInt()
    val minutes = ((displayMs / (1000 * 60)) % 60).toInt()
    val seconds = ((displayMs / 1000) % 60).toInt()

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isEditing && !isRunning) {
            TimerEditField(
                initialHours = hours,
                initialMinutes = minutes,
                initialSeconds = seconds,
                onConfirm = { newDuration ->
                    onDurationChange(newDuration)
                    isEditing = false
                },
                onCancel = { isEditing = false }
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.clickable(enabled = !isRunning) { isEditing = true }
            ) {
                TimeSegment(value = hours)
                Text(":", style = MaterialTheme.typography.displayMedium.copy(fontSize = 48.sp))
                TimeSegment(value = minutes)
                Text(":", style = MaterialTheme.typography.displayMedium.copy(fontSize = 48.sp))
                TimeSegment(value = seconds)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TimerButton(
                text = if (isRunning) "Stop" else "Start",
                color = if (isRunning) MaterialTheme.colorScheme.error else Color.Black,
                textColor = Color.White,
                onClick = {
                    if (isRunning) onReset() else onStart(savedDurationMs)
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Quick Presets Section
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PresetButton(
                text = "Sleep (8h)",
                onClick = { if (!isRunning) onDurationChange(8 * 3600000L) })
            PresetButton(
                text = "Study (2h)",
                onClick = { if (!isRunning) onDurationChange(2 * 3600000L) })
            PresetButton(
                text = "Break (10m)",
                onClick = { if (!isRunning) onDurationChange(10 * 60000L) })
        }
    }
}

@Composable
fun TimerEditField(
    initialHours: Int,
    initialMinutes: Int,
    initialSeconds: Int,
    onConfirm: (Long) -> Unit,
    onCancel: () -> Unit
) {
    val locale = LocalConfiguration.current.locales[0]
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    BackHandler(enabled = true) {
        onCancel()
    }

    // Format: HHMMSS
    var textValue by remember {
        val initialText = String.format(
            locale,
            "%02d%02d%02d",
            initialHours,
            initialMinutes,
            initialSeconds
        )
        mutableStateOf(TextFieldValue(initialText, selection = TextRange(initialText.length)))
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    BasicTextField(
        value = textValue,
        onValueChange = { newValue ->
            val digitsOnly = newValue.text.filter { it.isDigit() }.take(6)
            textValue = TextFieldValue(digitsOnly, selection = TextRange(digitsOnly.length))
        },
        modifier = Modifier
            .focusRequester(focusRequester)
            .width(IntrinsicSize.Min),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.NumberPassword,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                val padded = textValue.text.padStart(6, '0')
                val h = padded.substring(0, 2).toIntOrNull() ?: 0
                val m = padded.substring(2, 4).toIntOrNull() ?: 0
                val s = padded.substring(4, 6).toIntOrNull() ?: 0
                onConfirm(h * 3600000L + m * 60000L + s * 1000L)
                focusManager.clearFocus()
            }
        ),
        decorationBox = {
            val padded = textValue.text.padStart(6, '0')
            val h = padded.substring(0, 2)
            val m = padded.substring(2, 4)
            val s = padded.substring(4, 6)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = h,
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontSize = 48.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                Text(":", style = MaterialTheme.typography.displayMedium.copy(fontSize = 48.sp))
                Text(
                    text = m,
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontSize = 48.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                Text(":", style = MaterialTheme.typography.displayMedium.copy(fontSize = 48.sp))
                Text(
                    text = s,
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontSize = 48.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
    )
}

@Composable
fun TimeSegment(value: Int) {
    val locale = LocalConfiguration.current.locales[0]
    Text(
        text = String.format(locale, "%02d", value),
        style = MaterialTheme.typography.displayMedium.copy(fontSize = 48.sp),
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .padding(horizontal = 4.dp)
    )
}

@Composable
fun TimerButton(text: String, color: Color, textColor: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(color)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun PresetButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        ),
        shape = MaterialTheme.shapes.medium,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun BreakingFocusDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val isDebug = BuildConfig.FLAVOR == "dev" && BuildConfig.BUILD_TYPE == "debug"
    val initialTime = if (isDebug) 5 else 120
    var remainingSeconds by remember { mutableIntStateOf(initialTime) }
    var textInput by remember { mutableStateOf("") }
    val confirmationText = "I give up"

    LaunchedEffect(Unit) {
        while (remainingSeconds > 0) {
            delay(1000)
            remainingSeconds--
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = {},
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Breaking Focus?",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "You are choosing to stop focusing. Take a moment to think if this is really what you want right now.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Mandatory Pause:", style = MaterialTheme.typography.bodyLarge)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (remainingSeconds > 0) {
                                    CircularProgressIndicator(
                                        progress = { remainingSeconds.toFloat() / initialTime.toFloat() },
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = Color(0xFFFFA500)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text(
                                    text = "${remainingSeconds}s",
                                    color = Color(0xFFFFA500),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = buildAnnotatedString {
                                append("Type ")
                                withStyle(
                                    style = androidx.compose.ui.text.SpanStyle(
                                        fontStyle = FontStyle.Italic,
                                        fontWeight = FontWeight.Bold
                                    )
                                ) {
                                    append(confirmationText)
                                }
                                append(" to confirm:")
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = textInput,
                            onValueChange = { textInput = it },
                            placeholder = { Text("...") },
                            enabled = remainingSeconds == 0,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledContainerColor = Color.Transparent,
                                disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onDismiss,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Keep Focusing", textAlign = TextAlign.Center)
                            }

                            Button(
                                onClick = onConfirm,
                                enabled = remainingSeconds == 0 && textInput.trim()
                                    .equals(confirmationText, ignoreCase = true),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Disable focus", textAlign = TextAlign.Center)
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun FocusToggleSection(
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isEnabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.focus_enable_switch),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                if (isEnabled) {
                    Text(
                        text = "Focus monitoring is active",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Switch(
                checked = isEnabled,
                onCheckedChange = onToggle
            )
        }
    }
}

@Composable
fun AppListItem(
    app: FocusAppInfo,
    isBlocked: Boolean,
    onToggleBlocked: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        app.icon?.let { icon ->
            Image(
                bitmap = icon.toBitmap().asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )
        } ?: Spacer(modifier = Modifier.size(40.dp))

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = app.name, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = app.packageName,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Checkbox(
            checked = isBlocked,
            onCheckedChange = onToggleBlocked
        )
    }
}

private fun hasUsageStatsPermission(context: Context): Boolean {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val mode = appOps.unsafeCheckOpNoThrow(
        AppOpsManager.OPSTR_GET_USAGE_STATS,
        android.os.Process.myUid(),
        context.packageName
    )
    return mode == AppOpsManager.MODE_ALLOWED
}

private fun hasOverlayPermission(context: Context): Boolean {
    return Settings.canDrawOverlays(context)
}

private fun toggleFocusService(context: Context, enabled: Boolean) {
    val intent = Intent(context, FocusService::class.java)
    if (enabled) {
        context.startForegroundService(intent)
    } else {
        context.stopService(intent)
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FocusScreenContent(
    isFocusEnabled: Boolean,
    blockedApps: Set<String>,
    timerEndTime: Long,
    savedDurationMs: Long,
    filteredApps: List<FocusAppInfo>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    isLoadingApps: Boolean,
    onStartTimer: (Long) -> Unit,
    onResetTimer: () -> Unit,
    onDurationChange: (Long) -> Unit,
    onToggleFocus: (Boolean) -> Unit,
    onToggleAppBlocked: (FocusAppInfo, Boolean) -> Unit,
    showBreakingFocusDialog: Boolean,
    onConfirmBreakFocusAction: (() -> Unit)?,
    onDismissBreakingDialog: () -> Unit,
    onConfirmBreakingDialog: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (showBreakingFocusDialog) {
        BreakingFocusDialog(
            onDismiss = onDismissBreakingDialog,
            onConfirm = onConfirmBreakingDialog
        )
    }

    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                // Timer Section
                FocusTimerSection(
                    savedDurationMs = savedDurationMs,
                    timerEndTime = timerEndTime,
                    onStart = onStartTimer,
                    onReset = onResetTimer,
                    onDurationChange = onDurationChange
                )
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Toggle Section (Manual)
                FocusToggleSection(
                    isEnabled = isFocusEnabled,
                    onToggle = onToggleFocus
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }

            stickyHeader {
                Column(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
                    // App List Header
                    Text(
                        text = stringResource(R.string.focus_apps_list_header),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
                    )

                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(stringResource(R.string.search_apps_hint)) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            if (isLoadingApps) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else {
                items(filteredApps, key = { it.packageName }) { app ->
                    AppListItem(
                        app = app,
                        isBlocked = blockedApps.contains(app.packageName),
                        onToggleBlocked = { isBlocked -> onToggleAppBlocked(app, isBlocked) }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FocusScreenPreview() {
    RewardWithoutGuiltTheme {
        val mockApps = listOf(
            FocusAppInfo("YouTube", "com.google.android.youtube", null),
            FocusAppInfo("Instagram", "com.instagram.android", null),
            FocusAppInfo("Twitter / X", "com.twitter.android", null)
        )
        FocusScreenContent(
            isFocusEnabled = false,
            blockedApps = setOf("com.instagram.android"),
            timerEndTime = 0L,
            savedDurationMs = 25 * 60 * 1000L,
            filteredApps = mockApps,
            searchQuery = "",
            onSearchQueryChange = {},
            isLoadingApps = false,
            onStartTimer = {},
            onResetTimer = {},
            onDurationChange = {},
            onToggleFocus = {},
            onToggleAppBlocked = { _, _ -> },
            showBreakingFocusDialog = false,
            onConfirmBreakFocusAction = null,
            onDismissBreakingDialog = {},
            onConfirmBreakingDialog = {},
            onBack = {}
        )
    }
}
