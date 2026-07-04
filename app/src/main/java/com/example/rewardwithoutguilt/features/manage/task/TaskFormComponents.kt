package com.example.rewardwithoutguilt.features.manage.task

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rewardwithoutguilt.R
import com.example.rewardwithoutguilt.components.TaskIcons
import com.example.rewardwithoutguilt.data.TaskCategory
import com.example.rewardwithoutguilt.data.TaskFrequency
import com.example.rewardwithoutguilt.util.dashedBorder
import androidx.compose.ui.tooling.preview.Preview
import com.example.rewardwithoutguilt.ui.theme.RewardWithoutGuiltTheme
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll


fun getCategoryColor(category: TaskCategory?): Long = when (category) {
    TaskCategory.HEALTH -> 0xFF10B981
    TaskCategory.HOME -> 0xFFF59E0B
    TaskCategory.WORK -> 0xFF6366F1
    TaskCategory.ANIMALS -> 0xFFEC4899
    TaskCategory.PERSONAL -> 0xFF8B5CF6
    null -> 0xFF3B82F6
}

enum class TaskEditorType {
    ICON,
    CATEGORY,
    POINTS,
    FREQUENCY,
    DAYS_OF_WEEK,
    TARGET_PROGRESS,
    OPTIONAL
}

@Composable
fun PointsSelector(
    points: Int,
    onPointsChange: (Int) -> Unit,
    enabled: Boolean,
    isNegative: Boolean = false,
    addingPoints: List<Int>? = null // Kept for compatibility but we'll use a better system
) {
    val easyStr = stringResource(R.string.difficulty_easy)
    val mediumStr = stringResource(R.string.difficulty_medium)
    val hardStr = stringResource(R.string.difficulty_hard)
    val epicStr = stringResource(R.string.difficulty_epic)

    val presetsToUse = remember(isNegative, addingPoints, easyStr, mediumStr, hardStr, epicStr) {
        if (addingPoints != null) {
            addingPoints.map { Triple("", it, if (isNegative) "-$it" else "+$it") }
        } else {
            listOf(
                Triple(easyStr, 10, if (isNegative) "-10" else "+10"),
                Triple(mediumStr, 25, if (isNegative) "-25" else "+25"),
                Triple(hardStr, 50, if (isNegative) "-50" else "+50"),
                Triple(epicStr, 100, if (isNegative) "-100" else "+100")
            )
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.points_selector_label),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            color = Color.Transparent
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { if (points >= 5) onPointsChange(points - 5) },
                    enabled = enabled,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(Icons.Default.Remove, stringResource(R.string.decrease_label), modifier = Modifier.size(18.dp))
                }

                Text(
                    text = if (isNegative) "-$points" else points.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = { onPointsChange(points + 5) },
                    enabled = enabled,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.Add, stringResource(R.string.increase_label), modifier = Modifier.size(18.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            presetsToUse.forEach { (label, value, valueText) ->
                DifficultyPresetButton(
                    label = label,
                    valueText = valueText,
                    onClick = { onPointsChange(value) },
                    enabled = enabled,
                    isSelected = points == value,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun DifficultyPresetButton(
    label: String,
    valueText: String,
    onClick: () -> Unit,
    enabled: Boolean,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(60.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = enabled) { onClick() },
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        ),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (label.isNotEmpty()) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
            }
            Text(
                text = valueText,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun TargetProgressSelector(targetProgress: Int, onTargetProgressChange: (Int) -> Unit, enabled: Boolean) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.target_progress_label),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            color = Color.Transparent
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { if (targetProgress > 1) onTargetProgressChange(targetProgress - 1) },
                    enabled = enabled,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(Icons.Default.Remove, stringResource(R.string.decrease_label), modifier = Modifier.size(18.dp))
                }

                Text(
                    text = if (targetProgress == 1) stringResource(R.string.no_target_label) else stringResource(R.string.times_label, targetProgress),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = { if (targetProgress < 100) onTargetProgressChange(targetProgress + 1) },
                    enabled = enabled,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.Add, stringResource(R.string.increase_label), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun FrequencySelector(selectedFrequency: TaskFrequency, onFrequencySelected: (TaskFrequency) -> Unit, enabled: Boolean, onDaysOfWeekClick: () -> Unit = {}) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.frequency_label),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            val frequencies = listOf(
                TaskFrequency.ONE_TIME to stringResource(R.string.frequency_one_time),
                TaskFrequency.DAILY to stringResource(R.string.frequency_daily),
                TaskFrequency.WEEKLY to stringResource(R.string.frequency_weekly)
            )
            frequencies.forEach { (freq, label) ->
                val isSelected = selectedFrequency == freq
                Surface(
                    modifier = Modifier.weight(1f).height(40.dp).clip(RoundedCornerShape(12.dp)).clickable(enabled = enabled) { onFrequencySelected(freq) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(width = if (isSelected) 2.dp else 1.dp, color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        if (selectedFrequency == TaskFrequency.DAILY || selectedFrequency == TaskFrequency.WEEKLY) {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(enabled = enabled) { onDaysOfWeekClick() },
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.active_days_label),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun DayIndicators(selectedDays: Set<Int>, modifier: Modifier = Modifier) {
    val days = listOf(
        java.util.Calendar.MONDAY,
        java.util.Calendar.TUESDAY,
        java.util.Calendar.WEDNESDAY,
        java.util.Calendar.THURSDAY,
        java.util.Calendar.FRIDAY,
        java.util.Calendar.SATURDAY,
        java.util.Calendar.SUNDAY
    )
    
    val dayLabels = listOf(
        R.string.day_mon_short,
        R.string.day_tue_short,
        R.string.day_wed_short,
        R.string.day_thu_short,
        R.string.day_fri_short,
        R.string.day_sat_short,
        R.string.day_sun_short
    )

    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        days.zip(dayLabels).forEach { (day, labelRes) ->
            val isSelected = selectedDays.contains(day)
            Surface(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(6.dp)),
                shape = RoundedCornerShape(6.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(labelRes),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
fun DaysOfWeekSelector(selectedDays: Set<Int>, onDaysChanged: (Set<Int>) -> Unit, enabled: Boolean) {
    val days = listOf(
        java.util.Calendar.MONDAY,
        java.util.Calendar.TUESDAY,
        java.util.Calendar.WEDNESDAY,
        java.util.Calendar.THURSDAY,
        java.util.Calendar.FRIDAY,
        java.util.Calendar.SATURDAY,
        java.util.Calendar.SUNDAY
    )
    
    val dayLabels = listOf(
        R.string.day_mon_short,
        R.string.day_tue_short,
        R.string.day_wed_short,
        R.string.day_thu_short,
        R.string.day_fri_short,
        R.string.day_sat_short,
        R.string.day_sun_short
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.select_days_label),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            days.zip(dayLabels).forEach { (day, labelRes) ->
                val isSelected = selectedDays.contains(day)
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .clip(CircleShape)
                        .clickable(enabled = enabled) {
                            if (isSelected) onDaysChanged(selectedDays - day)
                            else onDaysChanged(selectedDays + day)
                        },
                    shape = CircleShape,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(labelRes),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        Text(
            text = stringResource(R.string.days_selector_hint),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp, start = 4.dp)
        )
    }
}

@Composable
fun IconGridSelector(
    selectedIconName: String,
    onIconSelected: (String) -> Unit,
    enabled: Boolean,
    icons: Map<String, ImageVector> = TaskIcons.icons,
    getLabel: (String) -> Int = { TaskIcons.getLabel(it) }
) {
    val columns = 8
    val iconList = icons.toList()
    val rows = iconList.chunked(columns)

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.select_icon_label),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
        )
        
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            rows.forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    rowItems.forEach { (name, icon) ->
                        val isSelected = selectedIconName == name
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(enabled = enabled) { onIconSelected(name) },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                            )
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = stringResource(getLabel(name)),
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                    // Fill remaining slots in the row to maintain column alignment
                    if (rowItems.size < columns) {
                        repeat(columns - rowItems.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OptionalSelector(isOptional: Boolean, onOptionalChange: (Boolean) -> Unit, enabled: Boolean) {
    val shape = RoundedCornerShape(12.dp)
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.optional_label),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (isOptional) {
                        Modifier.dashedBorder(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            shape = shape,
                            on = 8.dp,
                            off = 4.dp
                        )
                    } else Modifier
                ),
            shape = shape,
            border = if (isOptional) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            color = if (isOptional) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f) else Color.Transparent
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = enabled) { onOptionalChange(!isOptional) }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.optional_tag),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isOptional) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.optional_desc),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isOptional,
                    onCheckedChange = { onOptionalChange(it) },
                    enabled = enabled
                )
            }
        }
    }
}

@Composable
fun CategorySelector(selectedCategory: TaskCategory?, onCategorySelected: (TaskCategory?) -> Unit, enabled: Boolean) {
    val categories = listOf(
        null to (R.string.category_none to Icons.Default.Block),
        TaskCategory.HEALTH to (R.string.category_health to Icons.Default.Favorite),
        TaskCategory.HOME to (R.string.category_home to Icons.Default.Home),
        TaskCategory.WORK to (R.string.category_work to Icons.Default.Work),
        TaskCategory.ANIMALS to (R.string.category_animals to Icons.Default.Pets),
        TaskCategory.PERSONAL to (R.string.category_personal to Icons.Default.Person)
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.category_label),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { (category, info) ->
                val (labelRes, icon) = info
                val isSelected = selectedCategory == category
                val categoryColor = Color(getCategoryColor(category))
                Surface(
                    modifier = Modifier
                        .height(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(enabled = enabled) { onCategorySelected(category) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) categoryColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) categoryColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = if (isSelected) categoryColor else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = stringResource(labelRes),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) categoryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TaskFormComponentsPreview() {
    RewardWithoutGuiltTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text("Category Selector", style = MaterialTheme.typography.titleMedium)
            CategorySelector(
                selectedCategory = TaskCategory.HEALTH,
                onCategorySelected = {},
                enabled = true
            )
            
            HorizontalDivider()
            
            Text("Points Selector (+ / -)", style = MaterialTheme.typography.titleMedium)
            PointsSelector(
                points = 25,
                onPointsChange = {},
                enabled = true,
                isNegative = false
            )
            
            HorizontalDivider()
            
            Text("Target Progress Selector", style = MaterialTheme.typography.titleMedium)
            TargetProgressSelector(
                targetProgress = 3,
                onTargetProgressChange = {},
                enabled = true
            )
            
            HorizontalDivider()
            
            Text("Frequency Selector", style = MaterialTheme.typography.titleMedium)
            FrequencySelector(
                selectedFrequency = TaskFrequency.DAILY,
                onFrequencySelected = {},
                enabled = true
            )
            
            HorizontalDivider()
            
            Text("Days Of Week Selector", style = MaterialTheme.typography.titleMedium)
            DaysOfWeekSelector(
                selectedDays = setOf(java.util.Calendar.MONDAY, java.util.Calendar.WEDNESDAY, java.util.Calendar.FRIDAY),
                onDaysChanged = {},
                enabled = true
            )
            
            HorizontalDivider()
            
            Text("Icon Grid Selector", style = MaterialTheme.typography.titleMedium)
            IconGridSelector(
                selectedIconName = "default",
                onIconSelected = {},
                enabled = true
            )
            
            HorizontalDivider()
            
            Text("Optional Selector", style = MaterialTheme.typography.titleMedium)
            OptionalSelector(
                isOptional = true,
                onOptionalChange = {},
                enabled = true
            )
        }
    }
}

