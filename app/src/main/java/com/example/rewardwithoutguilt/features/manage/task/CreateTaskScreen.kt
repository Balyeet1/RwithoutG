package com.example.rewardwithoutguilt.features.manage.task

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.rewardwithoutguilt.R
import com.example.rewardwithoutguilt.components.TaskEditItem
import com.example.rewardwithoutguilt.data.Task
import com.example.rewardwithoutguilt.data.TaskCategory
import com.example.rewardwithoutguilt.data.TaskFrequency
import com.example.rewardwithoutguilt.data.TaskPreferences
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun CreateTaskScreen(
    onTaskCreated: (String) -> Unit,
    onSaveTrigger: ((action: () -> Unit, enabled: Boolean) -> Unit)? = null,
    initialTitle: String? = null,
    initialPoints: Int? = null,
    initialIconName: String? = null,
    initialFrequency: TaskFrequency? = null,
    initialCategory: TaskCategory? = null,
    initialTargetProgress: Int? = null
) {
    var taskTitle by remember { mutableStateOf(initialTitle ?: "") }
    var taskPoints by remember { mutableIntStateOf(initialPoints ?: 25) }
    var targetProgress by remember { mutableIntStateOf(initialTargetProgress ?: 1) }
    var selectedIconName by remember { mutableStateOf(initialIconName ?: "default") }
    var selectedFrequency by remember { mutableStateOf(initialFrequency ?: TaskFrequency.DAILY) }
    var selectedDaysOfWeek by remember { mutableStateOf(emptySet<Int>()) }
    var selectedCategory by remember { mutableStateOf<TaskCategory?>(initialCategory) }
    var isOptional by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var showErrors by remember { mutableStateOf(false) }

    // Editor state
    var activeEditor by remember { mutableStateOf(TaskEditorType.ICON) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val taskPrefs = remember { TaskPreferences(context) }

    val isTitleValid = taskTitle.isNotBlank()
    val isPointsValid = taskPoints > 0

    val onSave: () -> Unit = {
        if (isTitleValid && isPointsValid && !isSaving) {
            isSaving = true
            scope.launch {
                try {
                    val newTask = Task(
                        id = UUID.randomUUID().toString(),
                        title = taskTitle.trim(),
                        points = taskPoints,
                        isCompleted = false,
                        iconName = selectedIconName,
                        frequency = selectedFrequency,
                        color = getCategoryColor(selectedCategory),
                        category = selectedCategory,
                        targetProgress = targetProgress,
                        isBadHabit = false,
                        isOptional = isOptional,
                        daysOfWeek = selectedDaysOfWeek
                    )
                    taskPrefs.addTask(newTask)
                    onTaskCreated(newTask.id)
                } catch (e: Exception) {
                    isSaving = false
                }
            }
        } else showErrors = true
    }

    LaunchedEffect(onSaveTrigger, isTitleValid, isPointsValid, isSaving) {
        onSaveTrigger?.invoke(onSave, isTitleValid && isPointsValid && !isSaving)
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Live Preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    TaskEditItem(
                        task = Task(
                            id = "preview",
                            title = taskTitle,
                            points = taskPoints,
                            isCompleted = false,
                            iconName = selectedIconName,
                            frequency = selectedFrequency,
                            color = getCategoryColor(selectedCategory),
                            category = selectedCategory,
                            targetProgress = targetProgress,
                            isBadHabit = false,
                            isOptional = isOptional,
                            daysOfWeek = selectedDaysOfWeek
                        ),
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .scale(1.2f),
                        onIconClick = { activeEditor = TaskEditorType.ICON },
                        onTitleChange = { taskTitle = it; if (it.isNotBlank()) showErrors = false },
                        onPointsClick = { activeEditor = TaskEditorType.POINTS },
                        onFrequencyClick = { activeEditor = TaskEditorType.FREQUENCY },
                        onCategoryClick = { activeEditor = TaskEditorType.CATEGORY },
                        onTargetProgressClick = { activeEditor = TaskEditorType.TARGET_PROGRESS },
                        titleColor = if (taskTitle.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant.copy(
                            alpha = 0.6f
                        ) else null,
                        isIconSelected = activeEditor == TaskEditorType.ICON,
                        isPointsSelected = activeEditor == TaskEditorType.POINTS,
                        isFrequencySelected = activeEditor == TaskEditorType.FREQUENCY,
                        isCategorySelected = activeEditor == TaskEditorType.CATEGORY,
                        isTargetProgressSelected = activeEditor == TaskEditorType.TARGET_PROGRESS
                    )
                }

                if (showErrors && !isTitleValid) {
                    Text(
                        text = stringResource(R.string.error_title_required),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                // Conditional Editors
                when (activeEditor) {
                    TaskEditorType.ICON -> {
                        IconGridSelector(
                            selectedIconName = selectedIconName,
                            onIconSelected = { selectedIconName = it },
                            enabled = !isSaving
                        )
                    }

                    TaskEditorType.CATEGORY -> {
                        CategorySelector(
                            selectedCategory = selectedCategory,
                            onCategorySelected = { selectedCategory = it },
                            enabled = !isSaving
                        )
                    }

                    TaskEditorType.POINTS -> {
                        Column {
                            PointsSelector(
                                points = taskPoints,
                                onPointsChange = {
                                    taskPoints = it; if (it > 0) showErrors = false
                                },
                                enabled = !isSaving
                            )
                            if (showErrors && !isPointsValid) {
                                Text(
                                    text = stringResource(R.string.error_points_positive),
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                                )
                            }
                        }
                    }

                    TaskEditorType.FREQUENCY -> {
                        FrequencySelector(
                            selectedFrequency = selectedFrequency,
                            onFrequencySelected = { selectedFrequency = it },
                            enabled = !isSaving,
                            onDaysOfWeekClick = { activeEditor = TaskEditorType.DAYS_OF_WEEK }
                        )
                    }

                    TaskEditorType.DAYS_OF_WEEK -> {
                        DaysOfWeekSelector(
                            selectedDays = selectedDaysOfWeek,
                            onDaysChanged = { selectedDaysOfWeek = it },
                            enabled = !isSaving
                        )
                    }

                    TaskEditorType.TARGET_PROGRESS -> {
                        TargetProgressSelector(
                            targetProgress = targetProgress,
                            onTargetProgressChange = { targetProgress = it },
                            enabled = !isSaving
                        )
                    }

                    else -> {}
                }
                OptionalSelector(
                    isOptional = isOptional,
                    onOptionalChange = { isOptional = it },
                    enabled = !isSaving
                )
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun CreateTaskScreenPreview() {
    com.example.rewardwithoutguilt.ui.theme.RewardWithoutGuiltTheme {
        CreateTaskScreen(onTaskCreated = {})
    }
}
