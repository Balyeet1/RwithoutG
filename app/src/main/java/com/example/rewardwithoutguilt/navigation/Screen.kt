package com.example.rewardwithoutguilt.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.StickyNote2
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.rewardwithoutguilt.R

sealed class Screen(
    val route: String,
    val titleResId: Int? = null,
    val icon: ImageVector? = null
) {
    object Greeting : Screen("greeting")
    object Tasks : Screen("tasks", R.string.nav_tasks, Icons.Default.Assignment)
    object Rewards : Screen("rewards", R.string.nav_rewards, Icons.Default.Star)
    object CompletedTask : Screen("completed_task", R.string.nav_completed, Icons.Default.CheckCircle)
    object Notes : Screen("notes", R.string.nav_notes, Icons.AutoMirrored.Filled.StickyNote2)
    object EditGreeting : Screen("edit_greeting", R.string.nav_edit_greeting)
    object NotificationSettings : Screen("notification_settings", R.string.menu_notification_settings)
    object Focus : Screen("focus", R.string.nav_focus, Icons.Default.CenterFocusStrong)
    object Puzzle : Screen("puzzle", R.string.nav_puzzle)
    object CreateNote : Screen("create_note?noteId={noteId}&category={category}") {
        fun createRoute(noteId: String? = null, category: String? = null): String {
            val builder = StringBuilder("create_note")
            val params = mutableListOf<String>()
            noteId?.let { params.add("noteId=$it") }
            category?.let { params.add("category=$it") }
            if (params.isNotEmpty()) {
                builder.append("?")
                builder.append(params.joinToString("&"))
            }
            return builder.toString()
        }
    }
    object CreateTask : Screen("create_task?title={title}&points={points}&icon={icon}&frequency={frequency}&category={category}&targetProgress={targetProgress}") {
        fun createRoute(
            title: String? = null,
            points: Int? = null,
            icon: String? = null,
            frequency: String? = null,
            category: String? = null,
            targetProgress: Int? = null
        ): String {
            val builder = StringBuilder("create_task")
            val params = mutableListOf<String>()
            title?.let { params.add("title=$it") }
            points?.let { params.add("points=$it") }
            icon?.let { params.add("icon=$it") }
            frequency?.let { params.add("frequency=$it") }
            category?.let { params.add("category=$it") }
            targetProgress?.let { params.add("targetProgress=$it") }
            
            if (params.isNotEmpty()) {
                builder.append("?")
                builder.append(params.joinToString("&"))
            }
            return builder.toString()
        }
    }
    object AddTaskSelection : Screen("add_task_selection")
    object TaskTemplateList : Screen("task_template_list")
    object AddRewardSelection : Screen("add_reward_selection")
    object RewardTemplateList : Screen("reward_template_list")
    object CreateSlipUp : Screen("create_slip_up?title={title}&points={points}&icon={icon}") {
        fun createRoute(
            title: String? = null,
            points: Int? = null,
            icon: String? = null
        ): String {
            val builder = StringBuilder("create_slip_up")
            val params = mutableListOf<String>()
            title?.let { params.add("title=$it") }
            points?.let { params.add("points=$it") }
            icon?.let { params.add("icon=$it") }
            
            if (params.isNotEmpty()) {
                builder.append("?")
                builder.append(params.joinToString("&"))
            }
            return builder.toString()
        }
    }
    object CreateReward : Screen("create_reward?title={title}&cost={cost}&icon={icon}&isReusable={isReusable}") {
        fun createRoute(
            title: String? = null,
            cost: Int? = null,
            icon: String? = null,
            isReusable: Boolean? = null
        ): String {
            val builder = StringBuilder("create_reward")
            val params = mutableListOf<String>()
            title?.let { params.add("title=$it") }
            cost?.let { params.add("cost=$it") }
            icon?.let { params.add("icon=$it") }
            isReusable?.let { params.add("isReusable=$it") }
            
            if (params.isNotEmpty()) {
                builder.append("?")
                builder.append(params.joinToString("&"))
            }
            return builder.toString()
        }
    }
    object EditTask : Screen("edit_task/{taskId}") {
        fun createRoute(taskId: String) = "edit_task/$taskId"
    }
    object EditReward : Screen("edit_reward/{rewardId}") {
        fun createRoute(rewardId: String) = "edit_reward/$rewardId"
    }
}
