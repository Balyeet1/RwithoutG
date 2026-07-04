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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.rewardwithoutguilt.R
import com.example.rewardwithoutguilt.components.RewardIcons
import com.example.rewardwithoutguilt.components.TaskEditItem
import com.example.rewardwithoutguilt.components.TaskIcons
import com.example.rewardwithoutguilt.data.Task
import com.example.rewardwithoutguilt.data.TaskCategory
import com.example.rewardwithoutguilt.data.TaskFrequency
import com.example.rewardwithoutguilt.data.TaskPreferences
import kotlinx.coroutines.launch
import java.util.UUID
import androidx.compose.ui.tooling.preview.Preview
import com.example.rewardwithoutguilt.ui.theme.RewardWithoutGuiltTheme


@Composable
fun CreateSlipUpScreen(
    onSlipUpCreated: (String) -> Unit,
    onSaveTrigger: ((action: () -> Unit, enabled: Boolean) -> Unit)? = null,
    initialTitle: String? = null,
    initialPoints: Int? = null,
    initialIconName: String? = null
) {
    var title by remember { mutableStateOf(initialTitle ?: "") }
    var points by remember { mutableIntStateOf(initialPoints ?: 10) }
    var selectedIconName by remember { mutableStateOf(initialIconName ?: "cake") }
    var isSaving by remember { mutableStateOf(false) }
    var showErrors by remember { mutableStateOf(false) }

    // Editor state
    var activeEditor by remember { mutableStateOf(TaskEditorType.ICON) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val taskPrefs = remember { TaskPreferences(context) }

    val isTitleValid = title.isNotBlank()
    val isPointsValid = points > 0

    val onSave: () -> Unit = {
        if (isTitleValid && isPointsValid && !isSaving) {
            isSaving = true
            scope.launch {
                try {
                    val newTask = Task(
                        id = UUID.randomUUID().toString(),
                        title = title.trim(),
                        points = points,
                        isCompleted = false,
                        iconName = selectedIconName,
                        frequency = TaskFrequency.ONE_TIME,
                        color = 0xFFE57373L, // Salmon red
                        category = TaskCategory.HEALTH,
                        targetProgress = 1,
                        isBadHabit = true
                    )
                    taskPrefs.addTask(newTask)
                    onSlipUpCreated(newTask.id)
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

    CreateSlipUpScreenContent(
        title = title,
        onTitleChange = { title = it; if (it.isNotBlank()) showErrors = false },
        points = points,
        onPointsChange = { points = it; if (it > 0) showErrors = false },
        selectedIconName = selectedIconName,
        onIconSelected = { selectedIconName = it },
        activeEditor = activeEditor,
        onActiveEditorChange = { activeEditor = it },
        isSaving = isSaving,
        showErrors = showErrors,
        isTitleValid = isTitleValid,
        isPointsValid = isPointsValid
    )
}

@Composable
fun CreateSlipUpScreenContent(
    title: String,
    onTitleChange: (String) -> Unit,
    points: Int,
    onPointsChange: (Int) -> Unit,
    selectedIconName: String,
    onIconSelected: (String) -> Unit,
    activeEditor: TaskEditorType,
    onActiveEditorChange: (TaskEditorType) -> Unit,
    isSaving: Boolean,
    showErrors: Boolean,
    isTitleValid: Boolean,
    isPointsValid: Boolean,
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
                            title = title,
                            points = points,
                            isCompleted = false,
                            iconName = selectedIconName,
                            frequency = TaskFrequency.ONE_TIME,
                            color = 0xFFEF4444L,
                            category = TaskCategory.HEALTH,
                            targetProgress = 1,
                            isBadHabit = true
                        ),
                        modifier = Modifier.fillMaxWidth(0.85f).scale(1.2f),
                        onIconClick = { onActiveEditorChange(TaskEditorType.ICON) },
                        onTitleChange = onTitleChange,
                        onPointsClick = { onActiveEditorChange(TaskEditorType.POINTS) },
                        titleColor = if (title.isBlank()) Color.White.copy(alpha = 0.4f) else null,
                        isIconSelected = activeEditor == TaskEditorType.ICON,
                        isPointsSelected = activeEditor == TaskEditorType.POINTS
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
                        val badHabitIcons = mapOf(
                            "cake" to RewardIcons.icons["cake"]!!,
                            "food" to RewardIcons.icons["food"]!!,
                            "fitness" to TaskIcons.icons["fitness"]!!,
                            "default" to TaskIcons.icons["default"]!!,
                            "social" to TaskIcons.icons["social"]!!,
                            "sleep" to TaskIcons.icons["sleep"]!!,
                            "game" to RewardIcons.icons["game"]!!,
                            "movie" to RewardIcons.icons["movie"]!!,
                            "shopping" to TaskIcons.icons["shopping"]!!,
                            "relax" to RewardIcons.icons["relax"]!!
                        )
                        IconGridSelector(
                            selectedIconName = selectedIconName,
                            onIconSelected = onIconSelected,
                            enabled = !isSaving,
                            icons = badHabitIcons,
                            getLabel = { name ->
                                TaskIcons.getLabel(name).takeIf { it != R.string.icon_default } 
                                    ?: RewardIcons.getLabel(name)
                            }
                        )
                    }
                    TaskEditorType.POINTS -> {
                        Column {
                            PointsSelector(
                                points = points,
                                onPointsChange = onPointsChange,
                                enabled = !isSaving,
                                isNegative = true
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
                    else -> {}
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CreateSlipUpScreenPreview() {
    RewardWithoutGuiltTheme {
        CreateSlipUpScreenContent(
            title = "Ate sugar after dinner",
            onTitleChange = {},
            points = 25,
            onPointsChange = {},
            selectedIconName = "cake",
            onIconSelected = {},
            activeEditor = TaskEditorType.POINTS,
            onActiveEditorChange = {},
            isSaving = false,
            showErrors = false,
            isTitleValid = true,
            isPointsValid = true
        )
    }
}

