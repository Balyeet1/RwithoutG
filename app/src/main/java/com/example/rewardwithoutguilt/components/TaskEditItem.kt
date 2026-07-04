package com.example.rewardwithoutguilt.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rewardwithoutguilt.R
import com.example.rewardwithoutguilt.data.Task
import com.example.rewardwithoutguilt.data.TaskFrequency
import com.example.rewardwithoutguilt.ui.theme.RewardWithoutGuiltTheme
import com.example.rewardwithoutguilt.util.dashedBorder

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskEditItem(
    task: Task,
    onIconClick: () -> Unit,
    onTitleChange: (String) -> Unit,
    onPointsClick: () -> Unit,
    onFrequencyClick: (() -> Unit)? = null,
    onCategoryClick: (() -> Unit)? = null,
    onTargetProgressClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    isIconSelected: Boolean = false,
    isPointsSelected: Boolean = false,
    isFrequencySelected: Boolean = false,
    isCategorySelected: Boolean = false,
    isTargetProgressSelected: Boolean = false,
    titleColor: Color? = null,
) {
    val slipUpRed = Color(0xFFE57373)
    val darkRedBg = Color(0xFF2D1616)
    val taskColor = if (task.isBadHabit) slipUpRed else Color(task.color)
    val itemBgColor = if (task.isBadHabit) Color(0xFF1C1C24) else MaterialTheme.colorScheme.surface
    val taskShape = RoundedCornerShape(if (task.isBadHabit) 20.dp else 16.dp)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (task.isOptional) {
                    Modifier.dashedBorder(
                        width = 2.dp,
                        color = taskColor.copy(alpha = 0.3f),
                        shape = taskShape,
                        on = 8.dp,
                        off = 4.dp
                    )
                } else Modifier
            ),
        shape = taskShape,
        color = itemBgColor,
        border = if (task.isOptional) null else BorderStroke(
            2.dp,
            taskColor.copy(alpha = 0.3f)
        ),
        shadowElevation = 2.dp
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
                // Icon Section
                Box(
                    modifier = Modifier
                        .combinedClickable(onClick = onIconClick)
                        .border(
                            2.dp,
                            if (isIconSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            if (task.isBadHabit) RoundedCornerShape(12.dp) else CircleShape
                        )
                        .padding(2.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(if (task.isBadHabit) 48.dp else 40.dp),
                        shape = if (task.isBadHabit) RoundedCornerShape(12.dp) else CircleShape,
                        color = if (task.isBadHabit) darkRedBg else taskColor.copy(alpha = 0.15f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = TaskIcons.getIcon(task.iconName),
                                contentDescription = stringResource(TaskIcons.getLabel(task.iconName)),
                                tint = taskColor,
                                modifier = Modifier.size(if (task.isBadHabit) 24.dp else 20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Title Section
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp)
                ) {
                    BasicTextField(
                        value = task.title,
                        onValueChange = onTitleChange,
                        textStyle = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = titleColor ?: if (task.isBadHabit) Color.White else MaterialTheme.colorScheme.onSurface
                        ),
                        cursorBrush = SolidColor(if (task.isBadHabit) slipUpRed else MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth(),
                        decorationBox = { innerTextField ->
                            if (task.title.isEmpty()) {
                                Text(
                                    text = if (task.isBadHabit) stringResource(R.string.add_slip_up_manual) else stringResource(R.string.task_name_placeholder),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = titleColor ?: (if (task.isBadHabit) Color.White.copy(alpha = 0.4f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                                )
                            }
                            innerTextField()
                        }
                    )

                    if (!task.isBadHabit) {
                        Row(
                            modifier = Modifier.padding(top = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (task.frequency != TaskFrequency.ONE_TIME) {
                                Box(
                                    modifier = Modifier
                                        .combinedClickable(onClick = { onFrequencyClick?.invoke() })
                                        .border(
                                            1.dp,
                                            if (isFrequencySelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(2.dp)
                                ) {
                                    FrequencyChipSmall(frequency = task.frequency)
                                }
                            } else if (onFrequencyClick != null) {
                                Box(
                                    modifier = Modifier
                                        .combinedClickable(onClick = { onFrequencyClick.invoke() })
                                        .border(
                                            1.dp,
                                            if (isFrequencySelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.frequency_one_time),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = taskColor
                                    )
                                }
                            }

                            if (onCategoryClick != null) {
                                Box(
                                    modifier = Modifier
                                        .combinedClickable(onClick = { onCategoryClick.invoke() })
                                        .border(
                                            1.dp,
                                            if (isCategorySelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (task.category != null) stringResource(TaskIcons.getCategoryLabel(task.category!!)) else stringResource(R.string.category_none),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = taskColor
                                    )
                                }
                            }
                        }
                    }
                }

                // Points Pill Section
                Box(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .combinedClickable(onClick = onPointsClick)
                        .border(
                            2.dp,
                            if (isPointsSelected) (if (task.isBadHabit) Color.White else MaterialTheme.colorScheme.primary) else Color.Transparent,
                            RoundedCornerShape(if (task.isBadHabit) 8.dp else 16.dp)
                        )
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(if (task.isBadHabit) 8.dp else 16.dp))
                            .background(
                                if (task.isBadHabit) darkRedBg
                                else taskColor.copy(alpha = 0.15f)
                            )
                            .then(
                                if (task.isBadHabit) Modifier.size(width = 48.dp, height = 32.dp)
                                else Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (task.isBadHabit) "-${task.points}" else "+${task.points}",
                            style = if (task.isBadHabit) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (task.isBadHabit) slipUpRed else taskColor,
                            fontSize = if (task.isBadHabit) 14.sp else 13.sp
                        )
                    }
                }
            }

            // Progress Bar Section
            if (task.targetProgress > 1 && !task.isBadHabit) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(onClick = { onTargetProgressClick?.invoke() })
                        .border(
                            1.dp,
                            if (isTargetProgressSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 14.dp)
                        .padding(bottom = 12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Meta de Progresso",
                                style = MaterialTheme.typography.labelSmall,
                                color = taskColor.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "1 / ${task.targetProgress}",
                                style = MaterialTheme.typography.labelSmall,
                                color = taskColor,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        LinearProgressIndicator(
                            progress = { 1f / task.targetProgress.toFloat() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = taskColor,
                            trackColor = taskColor.copy(alpha = 0.1f),
                            strokeCap = StrokeCap.Round
                        )
                    }
                }
            } else if (onTargetProgressClick != null && !task.isBadHabit) {
                 Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(onClick = { onTargetProgressClick.invoke() })
                        .border(
                            1.dp,
                            if (isTargetProgressSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Adicionar meta de progresso",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
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
            Color(0xFFFFF3E0),
            Icons.Default.Refresh,
            R.string.frequency_daily
        )
        TaskFrequency.WEEKLY -> Triple(
            Color(0xFFE3F2FD),
            Icons.Default.CalendarMonth,
            R.string.frequency_weekly
        )
        else -> return
    }

    val contentColor = when (frequency) {
        TaskFrequency.DAILY -> Color(0xFFE65100)
        TaskFrequency.WEEKLY -> Color(0xFF1565C0)
        else -> MaterialTheme.colorScheme.primary
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
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

@Preview(showBackground = true)
@Composable
fun TaskEditItemPreview() {
    RewardWithoutGuiltTheme {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            TaskEditItem(
                task = Task(id = "1", title = "Morning Workout", points = 50, frequency = TaskFrequency.DAILY),
                onIconClick = {},
                onTitleChange = {},
                onPointsClick = {},
                onFrequencyClick = {},
                onCategoryClick = {},
                onTargetProgressClick = {}
            )
            
            TaskEditItem(
                task = Task(id = "2", title = "Eat Junk Food", points = 20, isBadHabit = true),
                onIconClick = {},
                onTitleChange = {},
                onPointsClick = {}
            )
        }
    }
}
