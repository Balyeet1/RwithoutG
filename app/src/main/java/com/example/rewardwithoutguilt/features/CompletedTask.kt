package com.example.rewardwithoutguilt.features

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rewardwithoutguilt.R
import com.example.rewardwithoutguilt.components.RewardIcons
import com.example.rewardwithoutguilt.components.TaskIcons
import com.example.rewardwithoutguilt.data.ActivityType
import com.example.rewardwithoutguilt.data.CompletedTaskRecord
import com.example.rewardwithoutguilt.data.TaskCategory
import com.example.rewardwithoutguilt.data.TaskPreferences
import com.example.rewardwithoutguilt.ui.theme.RewardWithoutGuiltTheme
import com.example.rewardwithoutguilt.util.Constants
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun CompletedTaskScreen(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val taskPrefs = remember { TaskPreferences(context) }
    val historyOpt by taskPrefs.completedHistory.collectAsState(initial = null)
    val history = historyOpt ?: emptyList()

    val activeTasksOpt by taskPrefs.tasks.collectAsState(initial = null)
    val activeTaskIds = remember(activeTasksOpt) {
        activeTasksOpt?.map { it.id }?.toSet() ?: emptySet()
    }

    val scope = rememberCoroutineScope()
    var animatingIds by remember { mutableStateOf(setOf<String>()) }
    var selectedTab by remember { mutableStateOf(StatsTab.TODAY) }
    var selectedDayMillis by remember { mutableStateOf<Long?>(null) }
    var historyFilter by remember { mutableStateOf(HistoryFilter.ALL) }

    val onToggleTask = remember(taskPrefs, scope) {
        { id: String ->
            if (!animatingIds.contains(id)) {
                animatingIds += id
                scope.launch {
                    taskPrefs.toggleTaskCompletion(id, forceUndo = true)
                    delay(Constants.UNDO_ANIMATION_DELAY)
                    animatingIds -= id
                }
            }
        }
    }

    val processedData = remember(history) {
        val aggregated = history.groupBy { it.completionDate.toStartOfDay() }
            .flatMap { (_, dayRecords) ->
                val (tasks, others) = dayRecords.partition { it.type == ActivityType.TASK }
                val aggregatedDayTasks = tasks.groupBy { it.taskId }.map { (_, records) ->
                    val latest = records.maxBy { it.completionDate }
                    latest.copy(
                        points = records.sumOf { it.points },
                        currentProgress = records.maxOf { it.currentProgress }
                    )
                }
                aggregatedDayTasks + others
            }.sortedByDescending { it.completionDate }

        val grouped = aggregated.groupBy { it.completionDate.toStartOfDay() }
        aggregated to grouped
    }

    val aggregatedTasks = processedData.first
    val groupedTasks = processedData.second

    val filteredGroupedTasks = remember(groupedTasks, historyFilter) {
        groupedTasks.mapValues { (_, records) ->
            records.filter { record ->
                when (historyFilter) {
                    HistoryFilter.ALL -> true
                    HistoryFilter.TASKS -> record.type == ActivityType.TASK
                    HistoryFilter.SLIP_UPS -> record.type == ActivityType.SLIP_UP
                    HistoryFilter.REWARDS -> record.type == ActivityType.REWARD
                }
            }
        }.filterValues { it.isNotEmpty() }
    }

    val todayMillis = remember { System.currentTimeMillis().toStartOfDay() }
    val todayTasks = groupedTasks[todayMillis] ?: emptyList()

    val todayStats = remember(todayTasks) {
        val count = todayTasks.count { it.type == ActivityType.TASK }
        val points = todayTasks.filter { it.type == ActivityType.TASK }.sumOf { it.points }
        val spent = todayTasks.filter { it.type == ActivityType.REWARD }.sumOf { it.points }
        val slipUps = todayTasks.filter { it.type == ActivityType.SLIP_UP }.sumOf { it.points }
        intArrayOf(count, points, spent, slipUps)
    }
    val todayCount = todayStats[0]
    val todayPoints = todayStats[1]
    val todaySpent = todayStats[2]
    val todaySlipUps = todayStats[3]

    val currentWeekMillis = remember {
        val cal = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis().toStartOfDay()
            val dayOfWeek = get(Calendar.DAY_OF_WEEK)
            val daysToSubtract =
                if (dayOfWeek == Calendar.SUNDAY) 6 else dayOfWeek - Calendar.MONDAY
            add(Calendar.DAY_OF_YEAR, -daysToSubtract)
        }
        val mondayCal = cal.clone() as Calendar
        (0..6).map { day ->
            (mondayCal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, day) }.timeInMillis
        }
    }

    val currentWeekTasks = remember(aggregatedTasks, currentWeekMillis) {
        val start = currentWeekMillis.first()
        val end = currentWeekMillis.last() + Constants.DAY_IN_MILLIS
        aggregatedTasks.filter { it.completionDate in start until end }
    }

    val weeklyStats = remember(currentWeekTasks) {
        val points = currentWeekTasks.filter { it.type == ActivityType.TASK }.sumOf { it.points }
        val count = currentWeekTasks.count { it.type == ActivityType.TASK }
        intArrayOf(points, count)
    }
    val weeklyPoints = weeklyStats[0]
    val weeklyCount = weeklyStats[1]

    val dailyPoints = remember(currentWeekTasks, currentWeekMillis) {
        currentWeekMillis.associateWith { dayMillis ->
            val dayActivities = currentWeekTasks.filter { it.completionDate.toStartOfDay() == dayMillis }
            val taskPoints = dayActivities.filter { it.type == ActivityType.TASK }.sumOf { it.points }
            val slipUpPoints = dayActivities.filter { it.type == ActivityType.SLIP_UP }.sumOf { it.points }
            (taskPoints - slipUpPoints).coerceAtLeast(0)
        }
    }

    val categoryPoints = remember(currentWeekTasks, selectedDayMillis) {
        val tasksToAnalyze =
            if (selectedDayMillis == null) currentWeekTasks else currentWeekTasks.filter { it.completionDate.toStartOfDay() == selectedDayMillis }

        val taskPointsMap = tasksToAnalyze.filter { it.type == ActivityType.TASK }
            .groupBy { it.category }
            .mapValues { it.value.sumOf { task -> task.points } }

        val slipUpPoints = tasksToAnalyze.filter { it.type == ActivityType.SLIP_UP }.sumOf { it.points }

        val items = mutableListOf<EnergyFocusItem>()

        TaskCategory.entries.forEach { category ->
            items.add(
                EnergyFocusItem(
                    labelRes = category.toLabelResource(),
                    points = taskPointsMap[category] ?: 0,
                    color = getCategoryColor(category)
                )
            )
        }

        if (taskPointsMap.getOrDefault(null, 0) > 0) {
            items.add(
                EnergyFocusItem(
                    labelRes = (null as TaskCategory?).toLabelResource(),
                    points = taskPointsMap[null] ?: 0,
                    color = getCategoryColor(null)
                )
            )
        }

        if (slipUpPoints > 0) {
            items.add(
                EnergyFocusItem(
                    labelRes = R.string.bad_habit_label_short,
                    points = slipUpPoints,
                    color = Color(0xFFE57373),
                    isSlipUp = true
                )
            )
        }

        items.sortedByDescending { it.points }
    }

    CompletedTaskContent(
        tasksByDate = if (selectedTab == StatsTab.TODAY) filteredGroupedTasks else groupedTasks,
        todayCount = todayCount,
        todayPoints = todayPoints,
        todaySpent = todaySpent,
        todaySlipUps = todaySlipUps,
        weeklyPoints = weeklyPoints,
        weeklyCount = weeklyCount,
        dailyPoints = dailyPoints,
        categoryPoints = categoryPoints,
        selectedTab = selectedTab,
        selectedDayMillis = selectedDayMillis,
        historyFilter = historyFilter,
        onTabSelected = { selectedTab = it; if (it == StatsTab.TODAY) selectedDayMillis = null },
        onDaySelected = { day -> selectedDayMillis = if (selectedDayMillis == day) null else day },
        onFilterSelected = { historyFilter = it },
        onToggleTask = onToggleTask,
        animatingIds = animatingIds,
        modifier = modifier,
        isLoading = historyOpt == null,
        activeTaskIds = activeTaskIds
    )
}

enum class StatsTab { TODAY, THIS_WEEK }

enum class HistoryFilter(val labelRes: Int) {
    ALL(R.string.filter_all),
    TASKS(R.string.filter_tasks),
    SLIP_UPS(R.string.filter_slipups),
    REWARDS(R.string.filter_rewards)
}

data class EnergyFocusItem(
    val labelRes: Int,
    val points: Int,
    val color: Color,
    val isSlipUp: Boolean = false
)

@Composable
fun StatsTabSwitcher(
    selectedTab: StatsTab,
    onTabSelected: (StatsTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) {
        Row(modifier = Modifier.padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
            TabItem(
                text = stringResource(R.string.today),
                isSelected = selectedTab == StatsTab.TODAY,
                onClick = { onTabSelected(StatsTab.TODAY) },
                modifier = Modifier.weight(1f)
            )
            TabItem(
                text = stringResource(R.string.stats_last_7_days),
                isSelected = selectedTab == StatsTab.THIS_WEEK,
                onClick = { onTabSelected(StatsTab.THIS_WEEK) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TabItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() },
        color = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
        shape = RoundedCornerShape(8.dp),
        shadowElevation = if (isSelected) 2.dp else 0.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun WeeklySummaryCard(points: Int, habitCount: Int) {
    val progress = (points.toFloat() / Constants.WEEKLY_POINTS_GOAL).coerceIn(0f, 1f)
    val isGoalReached = points >= Constants.WEEKLY_POINTS_GOAL
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.stats_total_week),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    letterSpacing = 1.sp
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = points.toString(),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.stats_points_gained),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.stats_habits_completed, habitCount),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(64.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    strokeCap = StrokeCap.Round,
                    strokeWidth = 4.dp
                )
                Surface(
                    modifier = Modifier.size(52.dp),
                    shape = CircleShape,
                    color = if (isGoalReached) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(
                        alpha = 0.2f
                    ),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = if (isGoalReached) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ActivityChart(
    dailyPoints: Map<Long, Int>,
    selectedDayMillis: Long?,
    onDaySelected: (Long) -> Unit
) {
    val maxPoints = dailyPoints.values.maxOrNull()?.coerceAtLeast(1) ?: 1
    val todayMillis = remember { System.currentTimeMillis().toStartOfDay() }
    val dayInitials = remember(dailyPoints) {
        val locale =
            if (Locale.getDefault().language == "pt") Locale.forLanguageTag("pt-BR") else Locale.getDefault()
        val sdf = SimpleDateFormat("E", locale)
        dailyPoints.keys.sorted().map { sdf.format(Date(it)).take(1).uppercase() }
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.stats_activity),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                dailyPoints.keys.sorted().forEachIndexed { index, dateMillis ->
                    val points = dailyPoints[dateMillis] ?: 0
                    val barHeightFraction = points.toFloat() / maxPoints
                    val highlight =
                        dateMillis == selectedDayMillis || (selectedDayMillis == null && dateMillis == todayMillis)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onDaySelected(dateMillis) }) {
                        if (points > 0) Text(
                            text = points.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                alpha = 0.6f
                            )
                        )
                        else Spacer(modifier = Modifier.height(14.dp))
                        Box(
                            modifier = Modifier
                                .width(14.dp)
                                .weight(1f)
                                .padding(top = 4.dp),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(barHeightFraction.coerceAtLeast(0.05f))
                                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                    .background(
                                        if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(
                                            alpha = 0.3f
                                        )
                                    )
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = dayInitials.getOrNull(index) ?: "?",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EnergyFocusSection(items: List<EnergyFocusItem>) {
    val totalPoints = items.sumOf { it.points }.coerceAtLeast(1)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.BarChart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.stats_energy_focus),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items.forEach { item ->
                    val progress = item.points.toFloat() / totalPoints
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(item.labelRes),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (item.isSlipUp) "-${item.points} pts" else stringResource(R.string.points_label, item.points),
                                style = MaterialTheme.typography.labelMedium,
                                color = if (item.isSlipUp) item.color else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape),
                            color = item.color,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            strokeCap = StrokeCap.Round
                        )
                    }
                }
            }
        }
    }
}

private fun getCategoryColor(category: TaskCategory?): Color = when (category) {
    TaskCategory.HEALTH -> Color(0xFF10B981); TaskCategory.HOME -> Color(0xFFF59E0B)
    TaskCategory.WORK -> Color(0xFF3B82F6); TaskCategory.ANIMALS -> Color(0xFF8B5CF6)
    TaskCategory.PERSONAL -> Color(0xFFEC4899); null -> Color(0xFF94A3B8)
}

private fun TaskCategory?.toLabelResource(): Int = when (this) {
    TaskCategory.HEALTH -> R.string.category_health; TaskCategory.HOME -> R.string.category_home
    TaskCategory.WORK -> R.string.category_work; TaskCategory.ANIMALS -> R.string.category_animals
    TaskCategory.PERSONAL -> R.string.category_personal; null -> R.string.category_none
}

@Composable
fun CompletedTaskContent(
    tasksByDate: Map<Long, List<CompletedTaskRecord>>,
    todayCount: Int,
    todayPoints: Int,
    todaySpent: Int,
    todaySlipUps: Int = 0,
    weeklyPoints: Int,
    weeklyCount: Int,
    dailyPoints: Map<Long, Int>,
    categoryPoints: List<EnergyFocusItem>,
    selectedTab: StatsTab,
    selectedDayMillis: Long?,
    historyFilter: HistoryFilter,
    onTabSelected: (StatsTab) -> Unit,
    onDaySelected: (Long) -> Unit,
    onFilterSelected: (HistoryFilter) -> Unit,
    onToggleTask: (String) -> Unit,
    animatingIds: Set<String>,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    activeTaskIds: Set<String> = emptySet()
) {
    var isFilterExpanded by remember { mutableStateOf(false) }

    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }; return@Surface
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = stringResource(R.string.completed_header),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            item {
                StatsTabSwitcher(
                    selectedTab = selectedTab,
                    onTabSelected = onTabSelected,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            if (selectedTab == StatsTab.TODAY) item {
                TodaySummaryCard(
                    count = todayCount,
                    points = todayPoints,
                    spent = todaySpent,
                    slipUps = todaySlipUps
                )
            }
            else {
                item { WeeklySummaryCard(points = weeklyPoints, habitCount = weeklyCount) }
                item {
                    ActivityChart(
                        dailyPoints = dailyPoints,
                        selectedDayMillis = selectedDayMillis,
                        onDaySelected = onDaySelected
                    )
                }
                if (categoryPoints.isNotEmpty()) item { EnergyFocusSection(items = categoryPoints) }

                if (selectedDayMillis != null) {
                    val selectedDayTasks = tasksByDate[selectedDayMillis] ?: emptyList()
                    if (selectedDayTasks.isNotEmpty()) {
                        item {
                            Column(modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    val locale =
                                        if (Locale.getDefault().language == "pt") Locale.forLanguageTag("pt-BR") else Locale.getDefault()
                                    val dateStr = SimpleDateFormat("dd 'de' MMMM", locale).format(Date(selectedDayMillis))
                                    Text(
                                        text = "${stringResource(R.string.activity_history)} - $dateStr",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                        items(selectedDayTasks, key = { it.historyId }) { task ->
                            CompletedTaskItem(
                                task = task,
                                onUndo = { onToggleTask(task.taskId) },
                                isAnimating = animatingIds.contains(task.taskId),
                                exists = activeTaskIds.contains(task.taskId)
                            )
                        }
                    }
                }
            }
            if (selectedTab == StatsTab.TODAY && tasksByDate.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.activity_history),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            FilterDropdownButton(
                                selectedFilter = historyFilter,
                                isExpanded = isFilterExpanded,
                                onClick = { isFilterExpanded = !isFilterExpanded }
                            )
                        }
                        AnimatedVisibility(
                            visible = isFilterExpanded,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            HistoryFilterRow(
                                selectedFilter = historyFilter,
                                onFilterSelected = {
                                    onFilterSelected(it)
                                    isFilterExpanded = false
                                },
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
                tasksByDate.keys.sortedDescending().forEach { dateMillis ->
                    item { DateSeparator(dateMillis = dateMillis) }
                    items(tasksByDate[dateMillis]!!, key = { it.historyId }) { task ->
                        CompletedTaskItem(
                            task = task,
                            onUndo = { onToggleTask(task.taskId) },
                            isAnimating = animatingIds.contains(task.taskId),
                            exists = activeTaskIds.contains(task.taskId)
                        )
                    }
                }
            } else if (selectedTab == StatsTab.TODAY) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.no_completed_tasks),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TodaySummaryCard(count: Int, points: Int, spent: Int, slipUps: Int = 0) {
    val isDark = isSystemInDarkTheme()
    val spentColor = if (isDark) Color(0xFF818CF8) else Color(0xFF4338CA)
    val slipUpRed = Color(0xFFE57373)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.today_summary_label),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = stringResource(R.string.tasks_count, count),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stringResource(R.string.gains_label),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        letterSpacing = 1.sp
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "+$points",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
            if (spent > 0 || slipUps > 0) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                )
                if (spent > 0) ActivitySummaryRow(
                    label = stringResource(R.string.spent_today_label),
                    value = spent,
                    color = spentColor
                )
                if (spent > 0 && slipUps > 0) Spacer(modifier = Modifier.height(8.dp))
                if (slipUps > 0) ActivitySummaryRow(
                    label = stringResource(R.string.slip_ups_today_label),
                    value = slipUps,
                    color = slipUpRed
                )
            }
        }
    }
}

@Composable
private fun ActivitySummaryRow(label: String, value: Int, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color.copy(alpha = 0.7f),
            letterSpacing = 1.sp
        )
        Surface(shape = RoundedCornerShape(12.dp), color = color.copy(alpha = 0.1f)) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "-$value",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = color
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.AccountBalanceWallet,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun DateSeparator(dateMillis: Long) {
    val todayMillis = remember { System.currentTimeMillis().toStartOfDay() }
    val yesterdayMillis = remember(todayMillis) { todayMillis - Constants.DAY_IN_MILLIS }

    val locale = remember {
        if (Locale.getDefault().language == "pt") Locale.forLanguageTag("pt-BR") else Locale.getDefault()
    }

    val date = remember(dateMillis) { Date(dateMillis) }
    val dayOfWeek = remember(date, locale) {
        SimpleDateFormat("EEEE", locale).format(date).replaceFirstChar { it.uppercase() }
    }

    val dateText = when (dateMillis) {
        todayMillis -> "${stringResource(R.string.today)} ($dayOfWeek)"
        yesterdayMillis -> "${stringResource(R.string.yesterday)} ($dayOfWeek)"
        else -> {
            val dayOfMonth = SimpleDateFormat("dd 'de' MMMM", locale).format(date)
            "$dayOfWeek, $dayOfMonth"
        }
    }
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = dateText,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    }
}

@Composable
fun FilterDropdownButton(
    selectedFilter: HistoryFilter,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.height(32.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF818CF8)) // Indigo-ish dot from image
            )
            Text(
                text = stringResource(selectedFilter.labelRes),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun HistoryFilterRow(
    selectedFilter: HistoryFilter,
    onFilterSelected: (HistoryFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth().padding(top = 4.dp).height(40.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp), // Reduced from 4.dp
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            HistoryFilter.entries.forEach { filter ->
                val isSelected = filter == selectedFilter
                Surface(
                    onClick = { onFilterSelected(filter) },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.surface.copy(alpha = 0.5f) else Color.Transparent,
                    border = if (isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)) else null,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(filter.labelRes),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CompletedTaskItem(
    task: CompletedTaskRecord,
    onUndo: () -> Unit,
    isAnimating: Boolean,
    exists: Boolean = true
) {
    val isDark = isSystemInDarkTheme()
    val isReward = task.type == ActivityType.REWARD
    val isSlipUp = task.type == ActivityType.SLIP_UP
    val slipUpRed = Color(0xFFE57373)
    val itemColor = when {
        isReward -> if (isDark) Color(0xFF818CF8) else Color(0xFF4338CA)
        isSlipUp -> slipUpRed; else -> Color(task.color)
    }
    val canUndo = remember(
        task.completionDate,
        task.type,
        exists
    ) {
        val isSameDay = System.currentTimeMillis().toStartOfDay() == task.completionDate.toStartOfDay()
        isSameDay && task.type == ActivityType.TASK && exists
    }
    Surface(
        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
        color = when {
            isReward -> itemColor.copy(alpha = 0.05f); isSlipUp -> Color(0xFF121214); else -> MaterialTheme.colorScheme.surface
        },
        border = BorderStroke(
            2.dp,
            if (isReward || isSlipUp) itemColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.outlineVariant.copy(
                alpha = 0.3f
            )
        ),
        shadowElevation = if (isReward || isSlipUp) 0.dp else 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = if (isReward || isSlipUp) RoundedCornerShape(10.dp) else CircleShape,
                color = if (isSlipUp) Color(0xFF1E1010) else itemColor.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isReward) RewardIcons.getIcon(
                            task.iconName
                        ) else TaskIcons.getIcon(task.iconName),
                        contentDescription = null,
                        tint = itemColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSlipUp) slipUpRed else if (isReward) itemColor else MaterialTheme.colorScheme.onSurface
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val label = when {
                        isReward -> stringResource(R.string.reward_spent_label); isSlipUp -> stringResource(
                            R.string.bad_habit_label_short
                        ); else -> task.category?.toLabelResource()?.let { stringResource(it) }
                            ?: stringResource(R.string.category_none)
                    }
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (isReward && task.targetProgress == Constants.REUSABLE_REWARD_FLAG) {
                        TextSeparator(); Icon(
                            imageVector = Icons.Default.Repeat,
                            contentDescription = null,
                            modifier = Modifier.size(10.dp),
                            tint = itemColor.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.width(2.dp)); Text(
                            text = stringResource(R.string.reward_reusable_badge).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = itemColor.copy(alpha = 0.7f)
                        )
                    }
                    if (!isReward && !isSlipUp && task.targetProgress > 1) {
                        TextSeparator(); Text(
                            text = "${task.currentProgress}/${task.targetProgress} ${
                                stringResource(
                                    R.string.times_suffix
                                )
                            }",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = itemColor
                        )
                        if (task.currentProgress == task.targetProgress) Text(
                            text = stringResource(
                                R.string.bonus_points_label
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = itemColor,
                            modifier = Modifier.padding(start = 2.dp)
                        )
                    }
                }
            }
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Text(
                    text = if (isReward || isSlipUp) "-${task.points}" else "+${task.points}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = itemColor
                )
                Text(
                    text = stringResource(R.string.points_suffix),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
            if (canUndo) IconButton(
                onClick = onUndo,
                enabled = !isAnimating,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = stringResource(R.string.undo_description),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }
            else Spacer(modifier = Modifier.width(32.dp))
        }
    }
}

@Composable
private fun TextSeparator() {
    Text(
        text = " • ",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    )
}

private fun Long.toStartOfDay(): Long = Calendar.getInstance().apply {
    timeInMillis = this@toStartOfDay; set(Calendar.HOUR_OF_DAY, 0); set(
    Calendar.MINUTE,
    0
); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
}.timeInMillis

@Preview(showBackground = true, name = "Filter UI Components")
@Composable
fun FilterPreview() {
    RewardWithoutGuiltTheme {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Dropdown Button:", style = MaterialTheme.typography.labelSmall)
            FilterDropdownButton(
                selectedFilter = HistoryFilter.ALL,
                isExpanded = false,
                onClick = {}
            )
            
            Text("Dropdown Button (Expanded):", style = MaterialTheme.typography.labelSmall)
            FilterDropdownButton(
                selectedFilter = HistoryFilter.ALL,
                isExpanded = true,
                onClick = {}
            )

            Text("Filter Row (Radio Style):", style = MaterialTheme.typography.labelSmall)
            HistoryFilterRow(
                selectedFilter = HistoryFilter.ALL,
                onFilterSelected = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "Completed Tasks List")
@Composable
fun CompletedTaskContentPreview() {
    RewardWithoutGuiltTheme {
        val today = System.currentTimeMillis().toStartOfDay()
        val yesterday = today - Constants.DAY_IN_MILLIS
        val records = listOf(
            CompletedTaskRecord(
                "h1",
                "1",
                "Trabalhar no Projeto",
                50,
                today,
                "work",
                0xFF3B82F6L,
                TaskCategory.WORK
            ),
            CompletedTaskRecord(
                "h2",
                "2",
                "Academia",
                30,
                today,
                "fitness_center",
                0xFF10B981L,
                TaskCategory.HEALTH
            ),
            CompletedTaskRecord(
                "h3",
                "3",
                "Limpar a Casa",
                20,
                yesterday,
                "home",
                0xFFF59E0BL,
                TaskCategory.HOME
            )
        )
        val grouped = records.groupBy { it.completionDate.toStartOfDay() }
        CompletedTaskContent(
            tasksByDate = grouped,
            todayCount = 2,
            todayPoints = 80,
            todaySpent = 0,
            weeklyPoints = 150,
            weeklyCount = 5,
            dailyPoints = grouped.mapValues { it.value.sumOf { t -> t.points } },
            categoryPoints = listOf(
                EnergyFocusItem(TaskCategory.WORK.toLabelResource(), 50, getCategoryColor(TaskCategory.WORK)),
                EnergyFocusItem(TaskCategory.HEALTH.toLabelResource(), 30, getCategoryColor(TaskCategory.HEALTH)),
                EnergyFocusItem(TaskCategory.HOME.toLabelResource(), 20, getCategoryColor(TaskCategory.HOME))
            ),
            selectedTab = StatsTab.TODAY,
            selectedDayMillis = null,
            historyFilter = HistoryFilter.ALL,
            onTabSelected = {},
            onDaySelected = {},
            onFilterSelected = {},
            onToggleTask = {},
            animatingIds = emptySet(),
            activeTaskIds = setOf("1", "2", "3")
        )
    }
}
