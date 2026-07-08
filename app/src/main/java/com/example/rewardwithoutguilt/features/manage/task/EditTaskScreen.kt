package com.example.rewardwithoutguilt.features.manage.task

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.compose.ui.tooling.preview.Preview
import com.example.rewardwithoutguilt.ui.theme.RewardWithoutGuiltTheme


@Composable
fun EditTaskScreen(
    taskId: String,
    onTaskUpdated: () -> Unit,
    onSaveTrigger: ((action: () -> Unit, enabled: Boolean) -> Unit)? = null
) {
    var taskTitle by remember { mutableStateOf("") }
    var taskPoints by remember { mutableIntStateOf(0) }
    var targetProgress by remember { mutableIntStateOf(1) }
    var currentProgress by remember { mutableIntStateOf(0) }
    var selectedIconName by remember { mutableStateOf("default") }
    var selectedFrequency by remember { mutableStateOf(TaskFrequency.ONE_TIME) }
    var selectedDaysOfWeek by remember { mutableStateOf(emptySet<Int>()) }
    var selectedCategory by remember { mutableStateOf<TaskCategory?>(null) }
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
                    taskPrefs.updateTask(
                        taskId,
                        taskTitle,
                        taskPoints,
                        selectedIconName,
                        selectedFrequency,
                        getCategoryColor(selectedCategory),
                        selectedCategory,
                        targetProgress,
                        false,
                        isOptional,
                        selectedDaysOfWeek
                    )
                    onTaskUpdated()
                } catch (e: Exception) {
                    isSaving = false
                }
            }
        } else {
            showErrors = true
        }
    }

    LaunchedEffect(onSaveTrigger, isTitleValid, isPointsValid, isSaving) {
        onSaveTrigger?.invoke(onSave, isTitleValid && isPointsValid && !isSaving)
    }

    LaunchedEffect(taskId) {
        val task = taskPrefs.tasks.first().find { it.id == taskId }
        if (task != null) {
            taskTitle = task.title
            taskPoints = task.points
            selectedIconName = task.iconName ?: "default"
            selectedFrequency = task.frequency
            selectedCategory = task.category
            targetProgress = task.targetProgress
            currentProgress = task.currentProgress
            isOptional = task.isOptional
            selectedDaysOfWeek = task.daysOfWeek
        }
    }

    EditTaskScreenContent(
        taskTitle = taskTitle,
        onTitleChange = { taskTitle = it; if (it.isNotBlank()) showErrors = false },
        taskPoints = taskPoints,
        onPointsChange = { taskPoints = it; if (it > 0) showErrors = false },
        targetProgress = targetProgress,
        onTargetProgressChange = { targetProgress = it },
        currentProgress = currentProgress,
        selectedIconName = selectedIconName,
        onIconSelected = { selectedIconName = it },
        selectedFrequency = selectedFrequency,
        onFrequencySelected = { selectedFrequency = it },
        selectedDaysOfWeek = selectedDaysOfWeek,
        onDaysChanged = { selectedDaysOfWeek = it },
        selectedCategory = selectedCategory,
        onCategorySelected = { selectedCategory = it },
        isOptional = isOptional,
        onOptionalChange = { isOptional = it },
        activeEditor = activeEditor,
        onActiveEditorChange = { activeEditor = it },
        isSaving = isSaving,
        showErrors = showErrors,
        isTitleValid = isTitleValid,
        isPointsValid = isPointsValid,
        taskId = taskId
    )
}

@Composable
fun EditTaskScreenContent(
    taskTitle: String,
    onTitleChange: (String) -> Unit,
    taskPoints: Int,
    onPointsChange: (Int) -> Unit,
    targetProgress: Int,
    onTargetProgressChange: (Int) -> Unit,
    currentProgress: Int,
    selectedIconName: String,
    onIconSelected: (String) -> Unit,
    selectedFrequency: TaskFrequency,
    onFrequencySelected: (TaskFrequency) -> Unit,
    selectedDaysOfWeek: Set<Int>,
    onDaysChanged: (Set<Int>) -> Unit,
    selectedCategory: TaskCategory?,
    onCategorySelected: (TaskCategory?) -> Unit,
    isOptional: Boolean,
    onOptionalChange: (Boolean) -> Unit,
    activeEditor: TaskEditorType,
    onActiveEditorChange: (TaskEditorType) -> Unit,
    isSaving: Boolean,
    showErrors: Boolean,
    isTitleValid: Boolean,
    isPointsValid: Boolean,
    taskId: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
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
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
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
                            currentProgress = currentProgress,
                            isBadHabit = false,
                            isOptional = isOptional,
                            daysOfWeek = selectedDaysOfWeek
                        ),
                        modifier = Modifier.fillMaxWidth(0.85f).scale(1.2f),
                        onIconClick = { onActiveEditorChange(TaskEditorType.ICON) },
                        onTitleChange = onTitleChange,
                        onPointsClick = { onActiveEditorChange(TaskEditorType.POINTS) },
                        onFrequencyClick = { onActiveEditorChange(TaskEditorType.FREQUENCY) },
                        onCategoryClick = { onActiveEditorChange(TaskEditorType.CATEGORY) },
                        onTargetProgressClick = { onActiveEditorChange(TaskEditorType.TARGET_PROGRESS) },
                        titleColor = if (taskTitle.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else null,
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
                            onIconSelected = onIconSelected,
                            enabled = !isSaving
                        )
                    }
                    TaskEditorType.CATEGORY -> {
                        CategorySelector(
                            selectedCategory = selectedCategory,
                            onCategorySelected = onCategorySelected,
                            enabled = !isSaving
                        )
                    }
                    TaskEditorType.POINTS -> {
                        Column {
                            PointsSelector(
                                points = taskPoints,
                                onPointsChange = onPointsChange,
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
                            onFrequencySelected = onFrequencySelected,
                            enabled = !isSaving,
                            onDaysOfWeekClick = { onActiveEditorChange(TaskEditorType.DAYS_OF_WEEK) }
                        )
                    }
                    TaskEditorType.DAYS_OF_WEEK -> {
                        DaysOfWeekSelector(
                            selectedDays = selectedDaysOfWeek,
                            onDaysChanged = onDaysChanged,
                            enabled = !isSaving
                        )
                    }
                    TaskEditorType.TARGET_PROGRESS -> {
                        TargetProgressSelector(
                            targetProgress = targetProgress,
                            onTargetProgressChange = onTargetProgressChange,
                            enabled = !isSaving
                        )
                    }
                    else -> {}
                }
                com.example.rewardwithoutguilt.components.AdvancedSettingsSection(
                    title = stringResource(R.string.advanced_task_settings)
                ) {
                    OptionalSelector(
                        isOptional = isOptional,
                        onOptionalChange = onOptionalChange,
                        enabled = !isSaving
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EditTaskScreenPreview() {
    RewardWithoutGuiltTheme {
        EditTaskScreenContent(
            taskTitle = "Practice Coding",
            onTitleChange = {},
            taskPoints = 50,
            onPointsChange = {},
            targetProgress = 1,
            onTargetProgressChange = {},
            currentProgress = 0,
            selectedIconName = "default",
            onIconSelected = {},
            selectedFrequency = TaskFrequency.DAILY,

            onFrequencySelected = {},
            selectedDaysOfWeek = emptySet(),
            onDaysChanged = {},
            selectedCategory = TaskCategory.WORK,
            onCategorySelected = {},
            isOptional = false,
            onOptionalChange = {},
            activeEditor = TaskEditorType.CATEGORY,
            onActiveEditorChange = {},
            isSaving = false,
            showErrors = false,
            isTitleValid = true,
            isPointsValid = true,
            taskId = "1"
        )
    }
}

