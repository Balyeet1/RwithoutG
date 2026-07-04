package com.example.rewardwithoutguilt.navigation

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.rewardwithoutguilt.R
import com.example.rewardwithoutguilt.components.LogSlipUpDialog
import com.example.rewardwithoutguilt.components.PointsDisplay
import com.example.rewardwithoutguilt.components.SaveBottomBar
import com.example.rewardwithoutguilt.data.RewardPreferences
import com.example.rewardwithoutguilt.data.Task
import com.example.rewardwithoutguilt.data.TaskCategory
import com.example.rewardwithoutguilt.data.TaskFrequency
import com.example.rewardwithoutguilt.data.TaskPreferences
import com.example.rewardwithoutguilt.features.CompletedTaskScreen
import com.example.rewardwithoutguilt.features.GreetingScreen
import com.example.rewardwithoutguilt.features.NotesScreen
import com.example.rewardwithoutguilt.features.PuzzleScreen
import com.example.rewardwithoutguilt.features.RewardsScreen
import com.example.rewardwithoutguilt.features.TasksScreen
import com.example.rewardwithoutguilt.features.focus.FocusScreen
import com.example.rewardwithoutguilt.features.manage.reward.AddRewardSelectionScreen
import com.example.rewardwithoutguilt.features.manage.reward.CreateRewardScreen
import com.example.rewardwithoutguilt.features.manage.reward.EditRewardScreen
import com.example.rewardwithoutguilt.features.manage.reward.RewardTemplateListScreen
import com.example.rewardwithoutguilt.features.manage.task.AddTaskSelectionScreen
import com.example.rewardwithoutguilt.features.manage.task.CreateSlipUpScreen
import com.example.rewardwithoutguilt.features.manage.task.CreateTaskScreen
import com.example.rewardwithoutguilt.features.manage.task.EditTaskScreen
import com.example.rewardwithoutguilt.features.manage.task.TaskTemplateListScreen
import com.example.rewardwithoutguilt.features.notes.CreateNoteScreen
import com.example.rewardwithoutguilt.more.EditGreetingScreen
import com.example.rewardwithoutguilt.more.MoreMenu
import com.example.rewardwithoutguilt.more.NotificationSettingsScreen
import kotlinx.coroutines.launch
import androidx.compose.runtime.collectAsState
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.rememberCoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupNavGraph(navController: NavHostController) {
    val context = LocalContext.current
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    var showMenu by remember { mutableStateOf(false) }
    var showSlipUpDialog by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    val taskPrefs = remember { TaskPreferences(context) }
    val allTasks by taskPrefs.tasks.collectAsState(initial = emptyList())
    val badHabits = remember(allTasks) { allTasks.filter { it.isBadHabit } }

    // Sync XP and Spent Points on startup
    LaunchedEffect(Unit) {
        TaskPreferences(context).syncXpIfMissing()
        RewardPreferences(context).syncSpentPointsIfMissing()
    }

    var saveTriggerTask by remember { mutableStateOf<Pair<() -> Unit, Boolean>?>(null) }
    var saveTriggerReward by remember { mutableStateOf<Pair<() -> Unit, Boolean>?>(null) }
    var saveTriggerEditTask by remember { mutableStateOf<Pair<() -> Unit, Boolean>?>(null) }
    var saveTriggerEditReward by remember { mutableStateOf<Pair<() -> Unit, Boolean>?>(null) }
    var saveTriggerSlipUp by remember { mutableStateOf<Pair<() -> Unit, Boolean>?>(null) }

    // Use derived state for onSave to ensure it updates when triggers change
    val currentRoute = currentDestination?.route
    val (onSave, isSaveEnabled) = when {
        currentRoute?.startsWith("create_task") == true -> saveTriggerTask?.let { it.first to it.second } ?: (null to false)
        currentRoute?.startsWith("create_reward") == true -> saveTriggerReward?.let { it.first to it.second } ?: (null to false)
        currentRoute?.startsWith("edit_task/") == true -> saveTriggerEditTask?.let { it.first to it.second } ?: (null to false)
        currentRoute?.startsWith("edit_reward/") == true -> saveTriggerEditReward?.let { it.first to it.second } ?: (null to false)
        currentRoute == Screen.CreateSlipUp.route -> saveTriggerSlipUp?.let { it.first to it.second } ?: (null to false)
        else -> null to false
    }

    val bottomBarScreens = listOf(
        Screen.Tasks,
        Screen.CompletedTask,
        Screen.Rewards,
        Screen.Notes
    )
    val showBottomBar = bottomBarScreens.any { it.route == currentRoute }
    val showTopBar = currentRoute != Screen.Greeting.route && currentRoute != Screen.CreateNote.route
    val isChildScreen = currentRoute == Screen.EditGreeting.route ||
            currentRoute == Screen.NotificationSettings.route ||
            currentRoute == Screen.Focus.route ||
            currentRoute == Screen.Puzzle.route ||
            currentRoute?.startsWith("create_task") == true ||
            currentRoute == Screen.AddTaskSelection.route ||
            currentRoute == Screen.TaskTemplateList.route ||
            currentRoute == Screen.AddRewardSelection.route ||
            currentRoute == Screen.RewardTemplateList.route ||
            currentRoute?.startsWith("create_reward") == true ||
            currentRoute?.startsWith("edit_task/") == true ||
            currentRoute?.startsWith("edit_reward/") == true ||
            currentRoute == Screen.CreateNote.route ||
            currentRoute == Screen.CreateSlipUp.route

    val blurRadius by animateDpAsState(
        targetValue = if (showSlipUpDialog) 12.dp else 0.dp,
        label = "blur"
    )

    Scaffold(
        modifier = Modifier.blur(blurRadius),
        topBar = {
            if (showTopBar) {
                TopAppBar(
                    title = {
                        val title = when {
                            currentRoute == Screen.EditGreeting.route -> stringResource(R.string.nav_edit_greeting)
                            currentRoute == Screen.NotificationSettings.route -> stringResource(R.string.menu_notification_settings)
                            currentRoute == Screen.Focus.route -> stringResource(R.string.nav_focus)
                            currentRoute == Screen.Puzzle.route -> stringResource(R.string.nav_puzzle)
                            currentRoute?.startsWith("create_task") == true -> stringResource(R.string.nav_create_task)
                            currentRoute == Screen.AddTaskSelection.route -> stringResource(R.string.nav_add_task_selection)
                            currentRoute == Screen.TaskTemplateList.route -> stringResource(R.string.nav_task_templates)
                            currentRoute == Screen.AddRewardSelection.route -> stringResource(R.string.nav_add_reward_selection)
                            currentRoute == Screen.RewardTemplateList.route -> stringResource(R.string.nav_reward_templates)
                            currentRoute?.startsWith("create_reward") == true -> stringResource(R.string.nav_create_reward)
                            currentRoute?.startsWith("edit_task/") == true -> stringResource(R.string.nav_edit_task)
                            currentRoute?.startsWith("edit_reward/") == true -> stringResource(R.string.nav_edit_reward)
                            currentRoute == Screen.CreateSlipUp.route -> stringResource(R.string.nav_create_slip_up)
                            currentRoute == Screen.Notes.route -> stringResource(R.string.nav_notes)
                            else -> ""
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = title)
                            if (showBottomBar && title.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            if (showBottomBar && currentRoute != Screen.Notes.route) {
                                PointsDisplay()
                            }
                        }
                    },
                    navigationIcon = {
                        if (isChildScreen) {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(R.string.back)
                                )
                            }
                        }
                    },
                    actions = {
                        Row(verticalAlignment = Alignment.CenterVertically,) {
                            if (currentRoute == Screen.Tasks.route) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { showSlipUpDialog = true },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFF1E1010),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f)),
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Outlined.Warning,
                                                contentDescription = stringResource(R.string.log_slip_up_title),
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(4.dp))
                            if (currentRoute == Screen.Tasks.route || currentRoute == Screen.Rewards.route) {
                                IconButton(
                                    onClick = {
                                        if (currentRoute == Screen.Tasks.route) {
                                            navController.navigate(Screen.AddTaskSelection.route)
                                        } else {
                                            navController.navigate(Screen.AddRewardSelection.route)
                                        }
                                    },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = if (currentRoute == Screen.Tasks.route)
                                            stringResource(R.string.task_fab_description)
                                        else
                                            stringResource(R.string.reward_fab_description)
                                    )
                                }
                            }
                            if (!isChildScreen) {
                                Box(contentAlignment = Alignment.Center) {
                                    IconButton(
                                        onClick = { showMenu = true },
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.MoreVert,
                                            contentDescription = stringResource(R.string.more_options)
                                        )
                                    }
                                    if (showMenu) {
                                        MoreMenu(
                                            onDismissRequest = { showMenu = false },
                                            onNavigateToEditGreeting = {
                                                navController.navigate(Screen.EditGreeting.route)
                                            },
                                            onNavigateToNotificationSettings = {
                                                navController.navigate(Screen.NotificationSettings.route)
                                            },
                                            onNavigateToFocus = {
                                                navController.navigate(Screen.Focus.route)
                                            }
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (showBottomBar) {
                BottomBar(navController = navController, screens = bottomBarScreens)
            } else if (onSave != null && currentRoute != Screen.EditGreeting.route) {
                SaveBottomBar(onSave = onSave, enabled = isSaveEnabled)
            }
        }
    ) { innerPadding ->
        if (showSlipUpDialog) {
            LogSlipUpDialog(
                badHabits = badHabits,
                onDismissRequest = { showSlipUpDialog = false },
                onLogSlipUp = { taskId ->
                    scope.launch {
                        taskPrefs.toggleTaskCompletion(taskId)
                    }
                },
                onDeleteHabit = { taskId ->
                    scope.launch {
                        taskPrefs.deleteTasks(setOf(taskId))
                    }
                }
            )
        }
        NavHost(
            navController = navController,
            startDestination = Screen.Greeting.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(route = Screen.Greeting.route) {
                GreetingScreen(
                    onTimeout = {
                        navController.navigate(Screen.Tasks.route) {
                            popUpTo(Screen.Greeting.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(route = Screen.Tasks.route) { backStackEntry ->
                val newTaskCreatedId by backStackEntry.savedStateHandle.getStateFlow<String?>("new_task_id", null).collectAsState()
                TasksScreen(
                    onEditTask = { taskId ->
                        navController.navigate(Screen.EditTask.createRoute(taskId))
                    },
                    onNavigateToPuzzle = {
                        navController.navigate(Screen.Puzzle.route)
                    },
                    highlightedTaskId = newTaskCreatedId,
                    onHighlightDismissed = {
                        backStackEntry.savedStateHandle.remove<String>("new_task_id")
                    }
                )
            }
            composable(
                route = Screen.CreateTask.route,
                arguments = listOf(
                    navArgument("title") { type = NavType.StringType; nullable = true },
                    navArgument("points") { type = NavType.IntType; defaultValue = -1 },
                    navArgument("icon") { type = NavType.StringType; nullable = true },
                    navArgument("frequency") { type = NavType.StringType; nullable = true },
                    navArgument("category") { type = NavType.StringType; nullable = true },
                    navArgument("targetProgress") { type = NavType.IntType; defaultValue = -1 }
                )
            ) { backStackEntry ->
                val title = backStackEntry.arguments?.getString("title")
                val points = backStackEntry.arguments?.getInt("points")?.takeIf { it != -1 }
                val icon = backStackEntry.arguments?.getString("icon")
                val frequencyStr = backStackEntry.arguments?.getString("frequency")
                val categoryStr = backStackEntry.arguments?.getString("category")
                val targetProgress = backStackEntry.arguments?.getInt("targetProgress")?.takeIf { it != -1 }

                val frequency = frequencyStr?.let { runCatching { TaskFrequency.valueOf(it) }.getOrNull() }
                val category = categoryStr?.let { runCatching { TaskCategory.valueOf(it) }.getOrNull() }

                CreateTaskScreen(
                    onTaskCreated = { taskId ->
                        navController.getBackStackEntry(Screen.Tasks.route).savedStateHandle.set("new_task_id", taskId)
                        navController.popBackStack(Screen.Tasks.route, inclusive = false)
                    },
                    onSaveTrigger = { action, enabled -> saveTriggerTask = action to enabled },
                    initialTitle = title,
                    initialPoints = points,
                    initialIconName = icon,
                    initialFrequency = frequency,
                    initialCategory = category,
                    initialTargetProgress = targetProgress
                )
            }
            composable(route = Screen.AddTaskSelection.route) {
                AddTaskSelectionScreen(
                    onManualClick = {
                        navController.navigate(Screen.CreateTask.createRoute())
                    },
                    onTemplateClick = {
                        navController.navigate(Screen.TaskTemplateList.route)
                    },
                    onSlipUpClick = {
                        navController.navigate(Screen.CreateSlipUp.createRoute())
                    },
                    onQuickTaskAdded = { taskId ->
                        navController.getBackStackEntry(Screen.Tasks.route).savedStateHandle.set("new_task_id", taskId)
                        navController.popBackStack(Screen.Tasks.route, inclusive = false)
                    }
                )
            }
            composable(
                route = Screen.CreateSlipUp.route,
                arguments = listOf(
                    navArgument("title") { type = NavType.StringType; nullable = true },
                    navArgument("points") { type = NavType.IntType; defaultValue = -1 },
                    navArgument("icon") { type = NavType.StringType; nullable = true }
                )
            ) { backStackEntry ->
                val title = backStackEntry.arguments?.getString("title")
                val points = backStackEntry.arguments?.getInt("points")?.takeIf { it != -1 }
                val icon = backStackEntry.arguments?.getString("icon")

                CreateSlipUpScreen(
                    onSlipUpCreated = { taskId ->
                        navController.getBackStackEntry(Screen.Tasks.route).savedStateHandle.set("new_task_id", taskId)
                        navController.popBackStack(Screen.Tasks.route, inclusive = false)
                    },
                    onSaveTrigger = { action, enabled -> saveTriggerSlipUp = action to enabled },
                    initialTitle = title,
                    initialPoints = points,
                    initialIconName = icon
                )
            }
            composable(route = Screen.TaskTemplateList.route) {
                TaskTemplateListScreen(
                    onTemplateSelected = { template ->
                        navController.navigate(
                            Screen.CreateTask.createRoute(
                                title = template.title,
                                points = template.points,
                                icon = template.iconName,
                                frequency = template.frequency.name,
                                category = template.category?.name,
                                targetProgress = template.targetProgress
                            )
                        )
                    }
                )
            }
            composable(route = Screen.EditTask.route) { backStackEntry ->
                val taskId = backStackEntry.arguments?.getString("taskId") ?: ""
                EditTaskScreen(
                    taskId = taskId,
                    onTaskUpdated = {
                        navController.popBackStack()
                    },
                    onSaveTrigger = { action, enabled -> saveTriggerEditTask = action to enabled }
                )
            }
            composable(route = Screen.Rewards.route) {
                RewardsScreen(
                    onEditReward = { rewardId ->
                        navController.navigate(Screen.EditReward.createRoute(rewardId))
                    }
                )
            }
            composable(route = Screen.AddRewardSelection.route) {
                AddRewardSelectionScreen(
                    onManualClick = {
                        navController.navigate(Screen.CreateReward.createRoute())
                    },
                    onTemplateClick = {
                        navController.navigate(Screen.RewardTemplateList.route)
                    }
                )
            }
            composable(route = Screen.RewardTemplateList.route) {
                RewardTemplateListScreen(
                    onTemplateSelected = { template ->
                        navController.navigate(
                            Screen.CreateReward.createRoute(
                                title = template.title,
                                cost = template.cost,
                                icon = template.iconName,
                                isReusable = template.isReusable
                            )
                        )
                    }
                )
            }
            composable(
                route = Screen.CreateReward.route,
                arguments = listOf(
                    navArgument("title") { type = NavType.StringType; nullable = true },
                    navArgument("cost") { type = NavType.IntType; defaultValue = -1 },
                    navArgument("icon") { type = NavType.StringType; nullable = true },
                    navArgument("isReusable") { type = NavType.BoolType; defaultValue = false }
                )
            ) { backStackEntry ->
                val title = backStackEntry.arguments?.getString("title")
                val cost = backStackEntry.arguments?.getInt("cost")?.takeIf { it != -1 }
                val icon = backStackEntry.arguments?.getString("icon")
                val isReusable = backStackEntry.arguments?.getBoolean("isReusable") ?: false

                CreateRewardScreen(
                    onRewardCreated = {
                        navController.popBackStack(Screen.Rewards.route, inclusive = false)
                    },
                    onSaveTrigger = { action, enabled -> saveTriggerReward = action to enabled },
                    initialTitle = title,
                    initialCost = cost,
                    initialIconName = icon,
                    initialIsReusable = isReusable
                )
            }
            composable(route = Screen.EditReward.route) { backStackEntry ->
                val rewardId = backStackEntry.arguments?.getString("rewardId") ?: ""
                EditRewardScreen(
                    rewardId = rewardId,
                    onRewardUpdated = {
                        navController.popBackStack()
                    },
                    onSaveTrigger = { action, enabled -> saveTriggerEditReward = action to enabled }
                )
            }
            composable(route = Screen.EditGreeting.route) {
                EditGreetingScreen(onSaveSuccess = {
                    navController.popBackStack()
                })
            }
            composable(route = Screen.NotificationSettings.route) {
                NotificationSettingsScreen()
            }
            composable(route = Screen.Focus.route) {
                FocusScreen(onBack = { navController.popBackStack() })
            }
            composable(route = Screen.Puzzle.route) {
                PuzzleScreen()
            }
            composable(route = Screen.CompletedTask.route) {
                CompletedTaskScreen()
            }
            composable(route = Screen.Notes.route) {
                NotesScreen(
                    onAddNote = { category ->
                        navController.navigate(Screen.CreateNote.createRoute(category = category))
                    },
                    onEditNote = { noteId ->
                        navController.navigate(Screen.CreateNote.createRoute(noteId = noteId))
                    }
                )
            }
            composable(
                route = Screen.CreateNote.route,
                arguments = listOf(
                    navArgument("noteId") { type = NavType.StringType; nullable = true },
                    navArgument("category") { type = NavType.StringType; nullable = true }
                )
            ) { backStackEntry ->
                val noteId = backStackEntry.arguments?.getString("noteId")
                val category = backStackEntry.arguments?.getString("category")
                CreateNoteScreen(
                    noteId = noteId,
                    initialCategory = category,
                    onNoteSaved = {
                        navController.popBackStack()
                    },
                    onClose = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
