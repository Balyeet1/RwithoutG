package com.example.rewardwithoutguilt.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rewardwithoutguilt.R
import com.example.rewardwithoutguilt.data.Task
import com.example.rewardwithoutguilt.data.TaskFrequency
import com.example.rewardwithoutguilt.features.manage.task.DayIndicators
import com.example.rewardwithoutguilt.ui.theme.RewardWithoutGuiltTheme
import com.example.rewardwithoutguilt.util.dashedBorder
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskItem(
    task: Task,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    highlight: Boolean = false,
    showDayIndicators: Boolean = false,
    isAnimating: Boolean = false
) {
    val scope = rememberCoroutineScope()
    var isCompleting by remember(task.id) { mutableStateOf(false) }

    val pointsOffset = remember { Animatable(0f) }
    val pointsAlpha = remember { Animatable(1f) }
    val itemAlpha = remember { Animatable(1f) }

    LaunchedEffect(isAnimating) {
        if (isAnimating) {
            isCompleting = !task.isEffectivelyCompleted
            if (isCompleting) {
                val anim1 = async {
                    pointsOffset.animateTo(
                        targetValue = -150f,
                        animationSpec = tween(durationMillis = 700)
                    )
                }
                val anim2 = async {
                    pointsAlpha.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(durationMillis = 700)
                    )
                }
                val anim3 = async {
                    itemAlpha.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(durationMillis = 700)
                    )
                }
                anim1.await()
                anim2.await()
                anim3.await()
            } else {
                itemAlpha.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 700)
                )
            }
        } else {
            pointsOffset.snapTo(0f)
            pointsAlpha.snapTo(1f)
            itemAlpha.snapTo(1f)
        }
    }

    val highlightAnim = remember { Animatable(0f) }
    LaunchedEffect(highlight) {
        if (highlight) {
            repeat(2) {
                highlightAnim.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 600, easing = LinearOutSlowInEasing)
                )
                highlightAnim.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 1000, easing = FastOutLinearInEasing)
                )
            }
        }
    }

    val slipUpRed = Color(0xFFE57373)
    val darkRedBg = Color(0xFF2D1616)
    val taskColor = if (task.isBadHabit) slipUpRed else Color(task.color)
    val itemBgColor =
        if (task.isBadHabit) Color(0xFF1C1C24) else if (isSelected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface.copy(
            0.9f
        )
    val taskShape = RoundedCornerShape(if (task.isBadHabit) 20.dp else 16.dp)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { alpha = itemAlpha.value }
            .then(
                if (task.isOptional) {
                    Modifier.dashedBorder(
                        width = if (isSelected) 1.5.dp else if (highlight) (1f + highlightAnim.value).dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                        else if (highlight) taskColor.copy(alpha = 0.35f + (highlightAnim.value * 0.65f))
                        else taskColor.copy(alpha = 0.35f),
                        shape = taskShape,
                        on = 8.dp,
                        off = 4.dp
                    )
                } else Modifier
            )
            .combinedClickable(
                enabled = enabled,
                onClick = {
                    if (isAnimating) return@combinedClickable
                    onClick()
                },
                onLongClick = onLongClick
            ),
        shape = taskShape,
        color = itemBgColor,
        border = if (task.isOptional) null else if (task.isBadHabit && !isSelected && !highlight) null else BorderStroke(
            width = if (isSelected) 1.5.dp else if (highlight) (1f + highlightAnim.value).dp else 1.dp,
            if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
            else if (highlight) taskColor.copy(alpha = 0.35f + (highlightAnim.value * 0.65f))
            else taskColor.copy(alpha = 0.35f)
        ),
        shadowElevation = if (highlight) (2f + (highlightAnim.value * 2f)).dp else 2.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Selection indicator or Icon
                if (isSelectionMode) {
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                } else {
                    Surface(
                        modifier = Modifier.size(if (task.isBadHabit) 48.dp else 40.dp),
                        shape = if (task.isBadHabit) RoundedCornerShape(12.dp) else CircleShape,
                        color = if (isSelected) Color.Transparent
                        else if (task.isBadHabit) darkRedBg
                        else taskColor.copy(alpha = 0.15f),
                        border = if (task.isBadHabit) BorderStroke(
                            1.dp,
                            slipUpRed.copy(alpha = 0.2f)
                        ) else null
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = TaskIcons.getIcon(task.iconName),
                                contentDescription = stringResource(TaskIcons.getLabel(task.iconName)),
                                tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                else if (task.isEffectivelyCompleted || isAnimating) MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                    alpha = 0.4f
                                )
                                else taskColor,
                                modifier = Modifier.size(if (task.isBadHabit) 24.dp else 20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textDecoration = if (task.isEffectivelyCompleted || (isAnimating && isCompleting)) TextDecoration.LineThrough else null,
                            color = if (task.isBadHabit) Color.White else if (isAnimating) MaterialTheme.colorScheme.onSurface.copy(
                                alpha = 0.6f
                            )
                            else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (!task.isBadHabit) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (task.frequency != TaskFrequency.ONE_TIME) {
                                    FrequencyChipSmall(frequency = task.frequency)
                                }
                                
                                if (task.isOptional) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = taskColor.copy(alpha = 0.1f)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.optional_tag),
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = taskColor.copy(alpha = 0.7f),
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }

                            if (task.targetProgress > 1) {
                                val totalPointsForGoal = (task.points * task.targetProgress)
                                Text(
                                    modifier = modifier.padding(top = 4.dp),
                                    text = "Total: $totalPointsForGoal pts (+10 bônus)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = taskColor.copy(alpha = 0.8f)
                                )
                            }
                            
                            if (showDayIndicators && task.daysOfWeek.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                DayIndicators(selectedDays = task.daysOfWeek)
                            }
                        }
                    }
                }

                // Points pill at the end
                if (!isSelectionMode) {
                    val isUndoing = isAnimating && !isCompleting
                    val effectivelyCompleted = task.isEffectivelyCompleted && !isAnimating

                    // Logic for bonus points on pill
                    val currentPointsAward = if (task.isEffectivelyCompleted) {
                        // If it's already completed, the last award was either points or points+10
                        if (task.targetProgress > 1) task.points + 10 else task.points
                    } else {
                        // If it's being completed now
                        if (task.targetProgress > 1 && task.currentProgress + 1 >= task.targetProgress) {
                            task.points + 10
                        } else {
                            task.points
                        }
                    }

                    Box(
                        modifier = Modifier
                            .offset { IntOffset(0, pointsOffset.value.roundToInt()) }
                            .alpha(if (effectivelyCompleted) 0f else pointsAlpha.value)
                            .clip(RoundedCornerShape(if (task.isBadHabit) 8.dp else 16.dp))
                            .background(
                                if (isUndoing || task.isBadHabit) darkRedBg
                                else taskColor.copy(alpha = 0.15f)
                            )
                            .then(
                                if (task.isBadHabit) Modifier.size(width = 48.dp, height = 32.dp)
                                else Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isUndoing || task.isBadHabit) "-${if (task.isBadHabit) task.points else currentPointsAward}" else "+$currentPointsAward",
                            style = if (task.isBadHabit) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isUndoing || task.isBadHabit) slipUpRed else taskColor,
                            fontSize = if (task.isBadHabit) 14.sp else 13.sp
                        )
                    }
                }
            }

            // Progress Bar
            if (task.targetProgress > 1 && !task.isEffectivelyCompleted && !isSelectionMode) {
                val progress = task.currentProgress.toFloat() / task.targetProgress.toFloat()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp)
                        .padding(bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Progresso",
                            style = MaterialTheme.typography.labelSmall,
                            color = taskColor.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${task.currentProgress} / ${task.targetProgress}",
                            style = MaterialTheme.typography.labelSmall,
                            color = taskColor,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = taskColor,
                        trackColor = taskColor.copy(alpha = 0.1f),
                        strokeCap = StrokeCap.Round
                    )
                }
            }
        }
    }
}

@Composable
private fun FrequencyChipSmall(frequency: TaskFrequency) {
    val (_, icon, labelRes) = when (frequency) {
        TaskFrequency.DAILY -> Triple(
            Color(0xFFFFF3E0), // Light Orange
            Icons.Default.Refresh,
            R.string.frequency_daily
        )

        TaskFrequency.WEEKLY -> Triple(
            Color(0xFFE3F2FD), // Light Blue
            Icons.Default.CalendarMonth,
            R.string.frequency_weekly
        )

        else -> return
    }

    val contentColor = when (frequency) {
        TaskFrequency.DAILY -> Color(0xFFE65100) // Deep Orange
        TaskFrequency.WEEKLY -> Color(0xFF1565C0) // Deep Blue
        else -> MaterialTheme.colorScheme.primary
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.alpha(0.9f),
        border = BorderStroke(1.dp, contentColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = contentColor
            )
            Text(
                text = stringResource(labelRes),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                fontSize = 10.sp
            )
        }
    }
}

@Preview(showBackground = true, name = "Task Item - Normal")
@Composable
fun TaskItemNormalPreview() {
    RewardWithoutGuiltTheme {
        TaskItem(
            task = Task("1", "Morning Workout", 50, false, frequency = TaskFrequency.DAILY),
            onClick = {},
            onLongClick = {},
            isSelected = false,
            isSelectionMode = false
        )
    }
}

@Preview(showBackground = true, name = "Task Item - Bad Habit")
@Composable
fun TaskItemBadHabitPreview() {
    RewardWithoutGuiltTheme {
        TaskItem(
            task = Task("2", "Eat Junk Food", 20, isBadHabit = true),
            onClick = {},
            onLongClick = {},
            isSelected = false,
            isSelectionMode = false
        )
    }
}
