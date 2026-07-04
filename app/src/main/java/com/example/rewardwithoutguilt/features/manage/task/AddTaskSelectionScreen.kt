package com.example.rewardwithoutguilt.features.manage.task

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterNone
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rewardwithoutguilt.R
import com.example.rewardwithoutguilt.data.Task
import com.example.rewardwithoutguilt.data.TaskPreferences
import kotlinx.coroutines.launch
import java.util.UUID
import androidx.compose.ui.tooling.preview.Preview
import com.example.rewardwithoutguilt.ui.theme.RewardWithoutGuiltTheme


@Composable
fun AddTaskSelectionScreen(
    onManualClick: () -> Unit,
    onTemplateClick: () -> Unit,
    onSlipUpClick: () -> Unit,
    onQuickTaskAdded: (String) -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val taskPrefs = remember { TaskPreferences(context) }

    AddTaskSelectionScreenContent(
        onManualClick = onManualClick,
        onTemplateClick = onTemplateClick,
        onSlipUpClick = onSlipUpClick,
        onSendQuickTask = { title ->
            val newTask = Task(
                id = UUID.randomUUID().toString(),
                title = title,
                points = 25,
                category = null
            )
            scope.launch {
                taskPrefs.addTask(newTask)
                onQuickTaskAdded(newTask.id)
            }
        }
    )
}

@Composable
fun AddTaskSelectionScreenContent(
    onManualClick: () -> Unit,
    onTemplateClick: () -> Unit,
    onSlipUpClick: () -> Unit,
    onSendQuickTask: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var quickTaskName by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // Quick Task Input
        OutlinedTextField(
            value = quickTaskName,
            onValueChange = { quickTaskName = it },
            placeholder = { Text(stringResource(R.string.add_quick_task_hint), fontSize = 14.sp) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            textStyle = MaterialTheme.typography.bodyMedium,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
            ),
            trailingIcon = {
                IconButton(
                    onClick = {
                        if (quickTaskName.isNotBlank()) {
                            onSendQuickTask(quickTaskName)
                            quickTaskName = ""
                        }
                    },
                    enabled = quickTaskName.isNotBlank()
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Add Quick Task",
                        tint = if (quickTaskName.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                    )
                }
            },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        SelectionCard(
            title = stringResource(R.string.add_task_manual),
            description = stringResource(R.string.add_task_manual_desc),
            icon = Icons.Default.Add,
            iconColor = Color(0xFF818CF8), // Lighter Indigo for dark mode
            iconBackground = Color(0xFF818CF8).copy(alpha = 0.15f),
            onClick = onManualClick
        )

        SelectionCard(
            title = stringResource(R.string.add_slip_up_manual),
            description = stringResource(R.string.add_slip_up_manual_desc),
            icon = Icons.Default.Warning,
            iconColor = Color(0xFFE57373), // Salmon red
            iconBackground = Color(0xFF1E1010), // Dark red bg
            onClick = onSlipUpClick
        )

        SelectionCard(
            title = stringResource(R.string.add_task_template),
            description = stringResource(R.string.add_task_template_desc),
            icon = Icons.Default.FilterNone,
            iconColor = Color(0xFF34D399), // Lighter Emerald for dark mode
            iconBackground = Color(0xFF34D399).copy(alpha = 0.15f),
            onClick = onTemplateClick
        )
    }
}


@Composable
private fun SelectionCard(
    title: String,
    description: String,
    icon: ImageVector,
    iconColor: Color,
    iconBackground: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Box
            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(16.dp),
                color = iconBackground
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = iconColor
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Text content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            }

            // Arrow
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.outlineVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddTaskSelectionScreenPreview() {
    RewardWithoutGuiltTheme {
        AddTaskSelectionScreenContent(
            onManualClick = {},
            onTemplateClick = {},
            onSlipUpClick = {},
            onSendQuickTask = {}
        )
    }
}

