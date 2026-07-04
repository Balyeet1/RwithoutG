package com.example.rewardwithoutguilt.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.rewardwithoutguilt.R
import com.example.rewardwithoutguilt.data.Task
import androidx.compose.ui.tooling.preview.Preview
import com.example.rewardwithoutguilt.ui.theme.RewardWithoutGuiltTheme
import com.example.rewardwithoutguilt.data.TaskCategory
import com.example.rewardwithoutguilt.data.TaskFrequency


@Composable
fun TaskList(
    tasks: List<Task>,
    emptyMessage: String,
    onToggleTask: (String) -> Unit,
    modifier: Modifier = Modifier,
    optionalTasks: List<Task> = emptyList(),
    otherDaysTasks: List<Task> = emptyList(),
    onDeleteTasks: ((Set<String>) -> Unit)? = null,
    onEditTask: ((String) -> Unit)? = null,
    isOptionalExpanded: Boolean = false,
    onOptionalExpandedChange: (Boolean) -> Unit = {},
    isOtherDaysExpanded: Boolean = false,
    onOtherDaysExpandedChange: (Boolean) -> Unit = {},
    isSelectionEnabled: Boolean = true,
    isLoading: Boolean = false,
    listState: LazyListState = rememberLazyListState(),
    highlightedTaskId: String? = null,
    animatingIds: Set<String> = emptySet()
) {
    var selectedTaskIds by remember { mutableStateOf(setOf<String>()) }
    val isSelectionMode = selectedTaskIds.isNotEmpty() && isSelectionEnabled

    val onSelectionClick = remember {
        { id: String ->
            selectedTaskIds = if (selectedTaskIds.contains(id)) {
                selectedTaskIds - id
            } else {
                selectedTaskIds + id
            }
        }
    }

    val onSelectionLongClick = remember(isSelectionEnabled) {
        { id: String ->
            if (isSelectionEnabled) {
                selectedTaskIds = setOf(id)
            }
        }
    }

    if (isLoading) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                strokeWidth = 4.dp
            )
        }
    } else if (tasks.isEmpty() && optionalTasks.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = emptyMessage)
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
        ) {
            if (isSelectionEnabled) {
                SelectionActionBar(
                    selectedCount = selectedTaskIds.size,
                    onCloseSelection = { selectedTaskIds = emptySet() },
                    onDelete = {
                        onDeleteTasks?.invoke(selectedTaskIds)
                        selectedTaskIds = emptySet()
                    },
                    onEdit = if (onEditTask != null) {
                        {
                            selectedTaskIds.firstOrNull()?.let { onEditTask(it) }
                            selectedTaskIds = emptySet()
                        }
                    } else null
                )
            }

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = tasks,
                    key = { it.id }
                ) { task ->
                    TaskListItem(
                        task = task,
                        isSelectionMode = isSelectionMode,
                        isSelected = selectedTaskIds.contains(task.id),
                        highlightedTaskId = highlightedTaskId,
                        onToggleTask = onToggleTask,
                        onClick = onSelectionClick,
                        onLongClick = onSelectionLongClick,
                        isAnimating = animatingIds.contains(task.id),
                        modifier = Modifier.animateItem()
                    )
                }

                if (optionalTasks.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOptionalExpandedChange(!isOptionalExpanded) }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = stringResource(R.string.optional_tasks_header),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                ) {
                                    Text(
                                        text = optionalTasks.size.toString(),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Icon(
                                imageVector = if (isOptionalExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    if (isOptionalExpanded) {
                        items(
                            items = optionalTasks,
                            key = { it.id }
                        ) { task ->
                            TaskListItem(
                                task = task,
                                isSelectionMode = isSelectionMode,
                                isSelected = selectedTaskIds.contains(task.id),
                                highlightedTaskId = highlightedTaskId,
                                onToggleTask = onToggleTask,
                                onClick = onSelectionClick,
                                onLongClick = onSelectionLongClick,
                                isAnimating = animatingIds.contains(task.id),
                                modifier = Modifier.animateItem()
                            )
                        }
                    }
                }

                if (otherDaysTasks.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOtherDaysExpandedChange(!isOtherDaysExpanded) }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = stringResource(R.string.other_days_tasks_header),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                ) {
                                    Text(
                                        text = otherDaysTasks.size.toString(),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Icon(
                                imageVector = if (isOtherDaysExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    if (isOtherDaysExpanded) {
                        items(
                            items = otherDaysTasks,
                            key = { it.id }
                        ) { task ->
                            TaskListItem(
                                task = task,
                                isSelectionMode = isSelectionMode,
                                isSelected = selectedTaskIds.contains(task.id),
                                highlightedTaskId = highlightedTaskId,
                                onToggleTask = onToggleTask,
                                onClick = onSelectionClick,
                                onLongClick = onSelectionLongClick,
                                showDayIndicators = true,
                                isAnimating = animatingIds.contains(task.id),
                                modifier = Modifier.animateItem()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskListItem(
    task: Task,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    highlightedTaskId: String?,
    onToggleTask: (String) -> Unit,
    onClick: (String) -> Unit,
    onLongClick: (String) -> Unit,
    isAnimating: Boolean,
    modifier: Modifier = Modifier,
    showDayIndicators: Boolean = false
) {
    val isClickable = !task.isEffectivelyCompleted || task.canUndo
    
    TaskItem(
        task = task,
        onClick = {
            if (isSelectionMode) {
                onClick(task.id)
            } else if (isClickable) {
                onToggleTask(task.id)
            }
        },
        onLongClick = { onLongClick(task.id) },
        isSelected = isSelected,
        isSelectionMode = isSelectionMode,
        enabled = !task.isEffectivelyCompleted || isClickable,
        highlight = task.id == highlightedTaskId,
        showDayIndicators = showDayIndicators,
        isAnimating = isAnimating,
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun TaskListPreview() {
    RewardWithoutGuiltTheme {
        val sampleTasks = listOf(
            Task(id = "1", title = "Drink water", points = 10, category = TaskCategory.HEALTH, iconName = "fitness"),
            Task(id = "2", title = "Read 10 pages", points = 25, category = TaskCategory.PERSONAL, iconName = "default")
        )
        val sampleOptionalTasks = listOf(
            Task(id = "3", title = "Organize desk", points = 25, category = TaskCategory.HOME, isOptional = true, iconName = "default")
        )
        val sampleOtherDaysTasks = listOf(
            Task(id = "4", title = "Go to gym", points = 50, category = TaskCategory.HEALTH, frequency = TaskFrequency.WEEKLY, iconName = "fitness")
        )
        TaskList(
            tasks = sampleTasks,
            emptyMessage = "No tasks today!",
            onToggleTask = {},
            optionalTasks = sampleOptionalTasks,
            otherDaysTasks = sampleOtherDaysTasks,
            isOptionalExpanded = true,
            isOtherDaysExpanded = true
        )
    }
}

