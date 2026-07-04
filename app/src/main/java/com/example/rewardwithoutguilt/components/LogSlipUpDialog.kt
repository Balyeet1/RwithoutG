package com.example.rewardwithoutguilt.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.rewardwithoutguilt.R
import com.example.rewardwithoutguilt.data.Task
import androidx.compose.ui.tooling.preview.Preview
import com.example.rewardwithoutguilt.ui.theme.RewardWithoutGuiltTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogSlipUpDialog(
    badHabits: List<Task>,
    onDismissRequest: () -> Unit,
    onLogSlipUp: (String) -> Unit,
    onDeleteHabit: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    var habitIdInDeleteMode by remember { mutableStateOf<String?>(null) }

    val backgroundColor = Color(0xFF141419)
    val itemBackgroundColor = Color(0xFF1C1C24)
    val slipUpRed = Color(0xFFE57373)
    val darkRedBg = Color(0xFF2D1616)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        containerColor = backgroundColor,
        tonalElevation = 0.dp,
        dragHandle = null,
        scrimColor = Color.Black.copy(alpha = 0.3f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .padding(bottom = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = slipUpRed,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.log_slip_up_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                
                Surface(
                    onClick = onDismissRequest,
                    modifier = Modifier.size(36.dp),
                    shape = CircleShape,
                    color = Color(0xFF25252E)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.close),
                            tint = Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            
            Text(
                text = stringResource(R.string.log_slip_up_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp, start = 36.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (badHabits.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_bad_habits_yet),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(badHabits, key = { it.id }) { habit ->
                        SlipUpItem(
                            habit = habit,
                            backgroundColor = itemBackgroundColor,
                            redColor = slipUpRed,
                            darkRedBg = darkRedBg,
                            isInDeleteMode = habitIdInDeleteMode == habit.id,
                            onLongClick = {
                                habitIdInDeleteMode = if (habitIdInDeleteMode == habit.id) null else habit.id
                            },
                            onDelete = {
                                onDeleteHabit(habit.id)
                                habitIdInDeleteMode = null
                            },
                            onClick = {
                                if (habitIdInDeleteMode != null) {
                                    habitIdInDeleteMode = null
                                } else {
                                    onLogSlipUp(habit.id)
                                    onDismissRequest()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SlipUpItem(
    habit: Task,
    backgroundColor: Color,
    redColor: Color,
    darkRedBg: Color,
    isInDeleteMode: Boolean,
    onLongClick: () -> Unit,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = darkRedBg
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = TaskIcons.getIcon(habit.iconName),
                        contentDescription = null,
                        tint = redColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = habit.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
            
            if (isInDeleteMode) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = redColor
                    )
                }
            } else {
                Surface(
                    modifier = Modifier.size(width = 48.dp, height = 32.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = darkRedBg
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "-${habit.points}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = redColor
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SlipUpItemPreview() {
    RewardWithoutGuiltTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val sampleHabit = Task(
                id = "1",
                title = "Eat Sweets",
                points = 15,
                isBadHabit = true,
                iconName = "cake"
            )
            Text("Normal State:")
            SlipUpItem(
                habit = sampleHabit,
                backgroundColor = Color(0xFF1C1C24),
                redColor = Color(0xFFE57373),
                darkRedBg = Color(0xFF2D1616),
                isInDeleteMode = false,
                onLongClick = {},
                onClick = {},
                onDelete = {}
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Delete Mode:")
            SlipUpItem(
                habit = sampleHabit,
                backgroundColor = Color(0xFF1C1C24),
                redColor = Color(0xFFE57373),
                darkRedBg = Color(0xFF2D1616),
                isInDeleteMode = true,
                onLongClick = {},
                onClick = {},
                onDelete = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LogSlipUpDialogPreview() {
    RewardWithoutGuiltTheme {
        val sampleHabits = listOf(
            Task(id = "1", title = "Eat Sweets", points = 15, isBadHabit = true, iconName = "cake"),
            Task(id = "2", title = "Bite Nails", points = 10, isBadHabit = true, iconName = "default"),
            Task(id = "3", title = "Procrastinate", points = 25, isBadHabit = true, iconName = "game")
        )
        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            LogSlipUpDialog(
                badHabits = sampleHabits,
                onDismissRequest = {},
                onLogSlipUp = {},
                onDeleteHabit = {}
            )
        }
    }
}

