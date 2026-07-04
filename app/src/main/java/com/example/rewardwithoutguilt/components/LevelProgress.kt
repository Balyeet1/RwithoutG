package com.example.rewardwithoutguilt.components

import android.content.res.Configuration
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rewardwithoutguilt.data.ActivityType
import com.example.rewardwithoutguilt.data.TaskPreferences
import com.example.rewardwithoutguilt.ui.theme.RewardWithoutGuiltTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private data class XpEvent(val id: String, val delta: Int, val color: Long)

@Composable
fun LevelProgress(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val taskPrefs = remember { TaskPreferences(context) }
    val totalXp by taskPrefs.totalXp.collectAsState(initial = 0)

    // Dynamic leveling: Level 1 needs 1000, Level 2 needs 1500, Level 3 needs 2000...
    val levelInfo = remember(totalXp) {
        var level = 1
        var accumulatedXp = 0
        var xpRequiredForNext = 1000

        while (totalXp >= accumulatedXp + xpRequiredForNext) {
            accumulatedXp += xpRequiredForNext
            level++
            xpRequiredForNext += 500
        }

        val xpInLevel = totalXp - accumulatedXp
        Triple(level, xpInLevel, xpRequiredForNext)
    }

    val level = levelInfo.first
    val xpInLevel = levelInfo.second
    val xpPerLevel = levelInfo.third
    val progress = xpInLevel.toFloat() / xpPerLevel

    val history by taskPrefs.completedHistory.collectAsState(initial = emptyList())
    var lastHistoryIds by remember { mutableStateOf<Set<String>?>(null) }
    var activeEvents by remember { mutableStateOf(listOf<XpEvent>()) }

    LaunchedEffect(history) {
        if (history.isEmpty()) return@LaunchedEffect
        val currentIds = history.map { it.historyId }.toSet()
        
        if (lastHistoryIds != null) {
            val newItems = history.filter { it.historyId !in lastHistoryIds!! }
            newItems.forEach { item ->
                // Show XP gains (tasks) and losses (slip ups), but hide rewards as they don't affect level XP
                if (item.points != 0 && item.type != ActivityType.REWARD) {
                    val delta = if (item.type == ActivityType.SLIP_UP) -item.points else item.points
                    activeEvents = activeEvents + XpEvent(
                        id = item.historyId,
                        delta = delta,
                        color = item.color
                    )
                }
            }
        }
        lastHistoryIds = currentIds
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface.copy(0.9f),
        tonalElevation = 2.dp,
        border = if (MaterialTheme.colorScheme.outlineVariant != Color.Transparent)
                    BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f))
                 else null
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 12.dp, horizontal = 16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Level Circle
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurface.copy(0.05f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = level.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(15.dp))

            Column(
                modifier = Modifier.weight(1f).padding(end = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "NÍVEL $level",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            letterSpacing = 0.5.sp
                        )

                        Text(
                            text = buildAnnotatedString {
                                withStyle(style = SpanStyle(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Black
                                )) {
                                    append(xpInLevel.toString())
                                }
                                withStyle(style = SpanStyle(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    fontWeight = FontWeight.Medium
                                )) {
                                    append(" / $xpPerLevel XP")
                                }
                            },
                            style = MaterialTheme.typography.labelSmall
                        )
                    }

                    // Animation container aligned to the middle as per arrow
                    Box(
                        modifier = Modifier.matchParentSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        activeEvents.forEach { event ->
                            key(event.id) {
                                FloatingXpGain(
                                    event = event,
                                    onFinished = {
                                        activeEvents = activeEvents.filter { it.id != event.id }
                                    }
                                )
                            }
                        }
                    }
                }

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round,
                    gapSize = 0.dp,
                    drawStopIndicator = {}
                )
            }
        }
    }
}

@Composable
private fun FloatingXpGain(event: XpEvent, onFinished: () -> Unit) {
    val yOffset = remember { Animatable(0f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        val duration = 1500L
        launch {
            yOffset.animateTo(-60f, animationSpec = tween(duration.toInt(), easing = LinearEasing))
        }
        launch {
            alpha.animateTo(1f, animationSpec = tween(300))
            delay(duration - 600)
            alpha.animateTo(0f, animationSpec = tween(300))
        }
        delay(duration)
        onFinished()
    }

    Surface(
        modifier = Modifier
            .offset { IntOffset(-50, yOffset.value.roundToInt()) }
            .graphicsLayer { this.alpha = alpha.value },
        color = Color.Transparent
    ) {
        val displayDelta = if (event.delta > 0) "+${event.delta}" else "${event.delta}"
        Text(
            text = "$displayDelta XP",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black,
            color = Color(event.color)
        )
    }
}

@Preview(showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
fun LevelProgressPreview() {
    RewardWithoutGuiltTheme {
        LevelProgress()
    }
}
