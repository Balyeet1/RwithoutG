package com.example.rewardwithoutguilt.features.manage.reward

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.rewardwithoutguilt.R
import com.example.rewardwithoutguilt.components.RewardIcons
import com.example.rewardwithoutguilt.components.RewardEditItem
import com.example.rewardwithoutguilt.data.Reward
import com.example.rewardwithoutguilt.data.RewardPreferences
import com.example.rewardwithoutguilt.features.manage.task.IconGridSelector
import com.example.rewardwithoutguilt.features.manage.task.PointsSelector
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.compose.ui.tooling.preview.Preview
import com.example.rewardwithoutguilt.ui.theme.RewardWithoutGuiltTheme


@Composable
fun EditRewardScreen(
    rewardId: String,
    onRewardUpdated: () -> Unit,
    onSaveTrigger: ((action: () -> Unit, enabled: Boolean) -> Unit)? = null
) {
    var rewardTitle by remember { mutableStateOf("") }
    var rewardCost by remember { mutableIntStateOf(100) }
    var selectedIconName by remember { mutableStateOf("default") }
    var isReusable by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var showErrors by remember { mutableStateOf(false) }
    
    // Editor state
    var activeEditor by remember { mutableStateOf(RewardEditorType.ICON) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val rewardPrefs = remember { RewardPreferences(context) }

    val isTitleValid = rewardTitle.isNotBlank()
    val isCostValid = rewardCost > 0

    val onSave: () -> Unit = {
        if (isTitleValid && isCostValid && !isSaving) {
            isSaving = true
            scope.launch {
                try {
                    rewardPrefs.updateReward(
                        rewardId,
                        rewardTitle.trim(),
                        rewardCost,
                        selectedIconName,
                        isReusable
                    )
                    onRewardUpdated()
                } catch (e: Exception) {
                    isSaving = false
                }
            }
        } else {
            showErrors = true
        }
    }

    LaunchedEffect(onSaveTrigger, isTitleValid, isCostValid, isSaving) {
        onSaveTrigger?.invoke(onSave, isTitleValid && isCostValid && !isSaving)
    }

    LaunchedEffect(rewardId) {
        val reward = rewardPrefs.rewards.first().find { it.id == rewardId }
        if (reward != null) {
            rewardTitle = reward.title
            rewardCost = reward.cost
            selectedIconName = reward.iconName ?: "default"
            isReusable = reward.isReusable
        }
    }

    EditRewardScreenContent(
        rewardTitle = rewardTitle,
        onTitleChange = {
            rewardTitle = it
            if (it.isNotBlank()) showErrors = false
        },
        rewardCost = rewardCost,
        onCostChange = {
            rewardCost = it
            if (it > 0) showErrors = false
        },
        selectedIconName = selectedIconName,
        onIconSelected = { selectedIconName = it },
        isReusable = isReusable,
        onReusableChange = { isReusable = it },
        activeEditor = activeEditor,
        onActiveEditorChange = { activeEditor = it },
        isSaving = isSaving,
        showErrors = showErrors,
        isTitleValid = isTitleValid,
        isCostValid = isCostValid,
        rewardId = rewardId
    )
}

@Composable
fun EditRewardScreenContent(
    rewardTitle: String,
    onTitleChange: (String) -> Unit,
    rewardCost: Int,
    onCostChange: (Int) -> Unit,
    selectedIconName: String,
    onIconSelected: (String) -> Unit,
    isReusable: Boolean,
    onReusableChange: (Boolean) -> Unit,
    activeEditor: RewardEditorType,
    onActiveEditorChange: (RewardEditorType) -> Unit,
    isSaving: Boolean,
    showErrors: Boolean,
    isTitleValid: Boolean,
    isCostValid: Boolean,
    rewardId: String,
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        RewardEditItem(
                            reward = Reward(
                                id = rewardId,
                                title = rewardTitle,
                                cost = rewardCost,
                                isSpent = false,
                                iconName = selectedIconName,
                                isReusable = isReusable
                            ),
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .scale(1.2f),
                            onIconClick = { onActiveEditorChange(RewardEditorType.ICON) },
                            onTitleChange = onTitleChange,
                            onPointsClick = { onActiveEditorChange(RewardEditorType.POINTS) },
                            titleColor = if (rewardTitle.isBlank()) {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            } else {
                                Color.White
                            },
                            isIconSelected = activeEditor == RewardEditorType.ICON,
                            isPointsSelected = activeEditor == RewardEditorType.POINTS
                        )
                    }
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
                    RewardEditorType.POINTS -> {
                        Column {
                            PointsSelector(
                                points = rewardCost,
                                onPointsChange = onCostChange,
                                enabled = !isSaving,
                                addingPoints = listOf(100, 250, 500, 1000),
                            )
                            if (showErrors && !isCostValid) {
                                Text(
                                    text = stringResource(R.string.error_points_positive),
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                                )
                            }
                        }
                    }
                    RewardEditorType.ICON -> {
                        IconGridSelector(
                            selectedIconName = selectedIconName,
                            onIconSelected = onIconSelected,
                            enabled = !isSaving,
                            icons = RewardIcons.icons
                        ) { RewardIcons.getLabel(it) }
                    }
                }

                // Reusable Toggle
                com.example.rewardwithoutguilt.components.AdvancedSettingsSection(
                    title = stringResource(R.string.advanced_reward_settings)
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.1f)),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.reward_reusable_label),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = stringResource(R.string.reward_reusable_desc),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = isReusable,
                                onCheckedChange = onReusableChange,
                                enabled = !isSaving
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EditRewardScreenIconEditorPreview() {
    RewardWithoutGuiltTheme {
        EditRewardScreenContent(
            rewardTitle = "Drink delicious coffee",
            onTitleChange = {},
            rewardCost = 150,
            onCostChange = {},
            selectedIconName = "relax",
            onIconSelected = {},
            isReusable = true,
            onReusableChange = {},
            activeEditor = RewardEditorType.ICON,
            onActiveEditorChange = {},
            isSaving = false,
            showErrors = false,
            isTitleValid = true,
            isCostValid = true,
            rewardId = "1"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EditRewardScreenPointsEditorPreview() {
    RewardWithoutGuiltTheme {
        EditRewardScreenContent(
            rewardTitle = "Play PS5",
            onTitleChange = {},
            rewardCost = 500,
            onCostChange = {},
            selectedIconName = "game",
            onIconSelected = {},
            isReusable = false,
            onReusableChange = {},
            activeEditor = RewardEditorType.POINTS,
            onActiveEditorChange = {},
            isSaving = false,
            showErrors = false,
            isTitleValid = true,
            isCostValid = true,
            rewardId = "2"
        )
    }
}



