package com.example.rewardwithoutguilt.features

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.rewardwithoutguilt.R
import com.example.rewardwithoutguilt.components.ExpandableFilterSection
import com.example.rewardwithoutguilt.components.LevelProgress
import com.example.rewardwithoutguilt.components.TaskList
import com.example.rewardwithoutguilt.data.Task
import com.example.rewardwithoutguilt.data.TaskFilters
import com.example.rewardwithoutguilt.data.TaskPreferences
import com.example.rewardwithoutguilt.ui.theme.RewardWithoutGuiltTheme
import com.example.rewardwithoutguilt.util.Constants
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TasksScreen(
    onEditTask: (String) -> Unit = {},
    onNavigateToPuzzle: () -> Unit = {},
    highlightedTaskId: String? = null,
    onHighlightDismissed: () -> Unit = {}
) {
    val context = LocalContext.current
    val taskPrefs = remember { TaskPreferences(context) }
    val allTasksOpt by taskPrefs.tasks.collectAsState(initial = null)
    val allTasks = allTasksOpt ?: emptyList()
    val activeFilters by taskPrefs.filters.collectAsState(initial = TaskFilters())
    val isOptionalExpanded by taskPrefs.isOptionalExpanded.collectAsState(initial = false)
    val isOtherDaysExpanded by taskPrefs.isOtherDaysExpanded.collectAsState(initial = false)

    val listState = rememberLazyListState()
    var animatingIds by remember { mutableStateOf(setOf<String>()) }

    val pendingTasks = remember(allTasks, animatingIds) {
        allTasks.filter { !it.isBadHabit && (!it.isEffectivelyCompleted || animatingIds.contains(it.id)) }
    }

    val filteredTasks = remember(pendingTasks, activeFilters) {
        pendingTasks.filter { task ->
            val categoryMatch = activeFilters.categories.isEmpty() ||
                    (task.category != null && activeFilters.categories.contains(task.category))
            val frequencyMatch = activeFilters.frequencies.isEmpty() ||
                    activeFilters.frequencies.contains(task.frequency)
            categoryMatch && frequencyMatch
        }.sortedByDescending { it.points }
    }

    val (mandatoryTasks, optionalTasks, otherDaysTasks) = remember(filteredTasks) {
        val today = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK)
        
        val (currentDayTasks, nonCurrentDayTasks) = filteredTasks.partition { task ->
            task.daysOfWeek.isEmpty() || task.daysOfWeek.contains(today)
        }
        
        val (mandatory, optional) = currentDayTasks.partition { !it.isOptional }
        Triple(mandatory, optional, nonCurrentDayTasks)
    }

    LaunchedEffect(activeFilters) {
        if (filteredTasks.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    LaunchedEffect(highlightedTaskId, filteredTasks) {
        if (highlightedTaskId != null && filteredTasks.isNotEmpty()) {
            val index = filteredTasks.indexOfFirst { it.id == highlightedTaskId }
            if (index != -1) {
                delay(300) // Small delay to ensure list is ready
                
                // Scroll to the item so it lands at the top of the list
                listState.animateScrollToItem(index)
                
                delay(3500) // Duration for the pulsing effect
                onHighlightDismissed()
            } else if (allTasksOpt != null) {
                onHighlightDismissed()
            }
        }
    }

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        taskPrefs.syncXpIfMissing()
        taskPrefs.syncTasks()
    }

    val onToggleTask = remember(taskPrefs, scope, allTasks) {
        { id: String ->
            val task = allTasks.find { it.id == id }
            if (task != null) {
                val willComplete = task.isEffectivelyCompleted ||
                        task.targetProgress <= 1 ||
                        task.currentProgress + 1 >= task.targetProgress

                if (willComplete) {
                    if (!animatingIds.contains(id)) {
                        animatingIds = animatingIds + id
                        scope.launch {
                            taskPrefs.toggleTaskCompletion(id)
                            delay(Constants.UNDO_ANIMATION_DELAY)
                            animatingIds = animatingIds - id
                        }
                    }
                } else {
                    scope.launch {
                        taskPrefs.toggleTaskCompletion(id)
                    }
                }
            }
        }
    }
    
    val onDeleteTasks = remember(taskPrefs, scope) {
        { ids: Set<String> -> scope.launch { taskPrefs.deleteTasks(ids) } }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            LevelProgress(onClick = onNavigateToPuzzle)
            ExpandableFilterSection(
                filters = activeFilters,
                onFiltersChange = { scope.launch { taskPrefs.updateFilters(it) } },
                modifier = Modifier.padding(top = 16.dp)
            )

            TaskList(
                tasks = mandatoryTasks,
                optionalTasks = optionalTasks,
                otherDaysTasks = otherDaysTasks,
                emptyMessage = if (activeFilters.isEmpty) {
                    stringResource(R.string.no_pending_tasks)
                } else {
                    stringResource(R.string.no_tasks_match_filters)
                },
                onToggleTask = onToggleTask,
                onDeleteTasks = { onDeleteTasks(it) },
                onEditTask = onEditTask,
                isOptionalExpanded = isOptionalExpanded,
                onOptionalExpandedChange = { scope.launch { taskPrefs.setOptionalExpanded(it) } },
                isOtherDaysExpanded = isOtherDaysExpanded,
                onOtherDaysExpandedChange = { scope.launch { taskPrefs.setOtherDaysExpanded(it) } },
                modifier = Modifier.padding(top = 16.dp),
                isLoading = allTasksOpt == null,
                listState = listState,
                highlightedTaskId = highlightedTaskId,
                animatingIds = animatingIds
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TasksScreenPreview() {
    RewardWithoutGuiltTheme {
        val tasks = listOf(
            Task("1", "Morning Workout", 50, true),
            Task("2", "Read 20 pages", 20, false),
            Task("3", "Buy groceries", 10, false)
        )
        Column {
            ExpandableFilterSection(filters = TaskFilters(), onFiltersChange = {})
            TaskList(tasks = tasks, emptyMessage = "No tasks", onToggleTask = {}, onDeleteTasks = {}, onEditTask = {})
        }
    }
}
