package com.example.rewardwithoutguilt.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.rewardwithoutguilt.util.Constants
import androidx.compose.runtime.Immutable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import java.util.UUID

private val Context.taskDataStore by preferencesDataStore(name = Constants.PREFS_TASK)

enum class TaskFrequency { ONE_TIME, DAILY, WEEKLY }
enum class TaskCategory { HEALTH, HOME, WORK, ANIMALS, PERSONAL }
enum class TaskDifficulty { EASY, MODERATE, DIFFICULT }
enum class ActivityType { TASK, REWARD, SLIP_UP }

@Immutable
data class TaskFilters(
    val categories: Set<TaskCategory> = emptySet(),
    val frequencies: Set<TaskFrequency> = emptySet(),
    val favoriteCategories: Set<TaskCategory> = emptySet(),
    val favoriteFrequencies: Set<TaskFrequency> = emptySet()
) {
    val isEmpty: Boolean get() = categories.isEmpty() && frequencies.isEmpty()
}

@Immutable
data class CompletedTaskRecord(
    val historyId: String,
    val taskId: String,
    val title: String,
    val points: Int,
    val completionDate: Long,
    val iconName: String?,
    val color: Long,
    val category: TaskCategory?,
    val currentProgress: Int = 0,
    val targetProgress: Int = 1,
    val type: ActivityType = ActivityType.TASK
)

@Immutable
data class Task(
    val id: String,
    val title: String,
    val points: Int = 0,
    val isCompleted: Boolean = false,
    val iconName: String? = null,
    val frequency: TaskFrequency = TaskFrequency.ONE_TIME,
    val lastCompletedDate: Long = 0L,
    val accumulatedPoints: Int = 0,
    val color: Long = Constants.DEFAULT_TASK_COLOR,
    val category: TaskCategory? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val currentProgress: Int = 0,
    val targetProgress: Int = 1,
    val isBadHabit: Boolean = false,
    val isOptional: Boolean = false,
    val daysOfWeek: Set<Int> = emptySet()
) {
    fun isExpired(): Boolean {
        if (frequency == TaskFrequency.ONE_TIME) return false
        val timestampToCompare = if (lastCompletedDate != 0L) lastCompletedDate else createdAt
        val now = System.currentTimeMillis()
        
        val calNow = Calendar.getInstance().apply { timeInMillis = now }
        val calLast = Calendar.getInstance().apply { timeInMillis = timestampToCompare }

        return when (frequency) {
            TaskFrequency.DAILY -> {
                !(calNow.get(Calendar.YEAR) == calLast.get(Calendar.YEAR) &&
                  calNow.get(Calendar.DAY_OF_YEAR) == calLast.get(Calendar.DAY_OF_YEAR))
            }
            TaskFrequency.WEEKLY -> {
                val thisMonday = Calendar.getInstance().apply {
                    timeInMillis = now
                    firstDayOfWeek = Calendar.MONDAY
                    set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                }
                if (thisMonday.timeInMillis > now) thisMonday.add(Calendar.DAY_OF_YEAR, -7)
                timestampToCompare < thisMonday.timeInMillis
            }
        }
    }

    val isEffectivelyCompleted: Boolean get() = isCompleted && !isExpired()
    val canUndo: Boolean get() {
        if (lastCompletedDate == 0L) return false
        val now = System.currentTimeMillis()
        val calNow = Calendar.getInstance().apply { timeInMillis = now }
        val calLast = Calendar.getInstance().apply { timeInMillis = lastCompletedDate }
        
        return calNow.get(Calendar.YEAR) == calLast.get(Calendar.YEAR) &&
               calNow.get(Calendar.DAY_OF_YEAR) == calLast.get(Calendar.DAY_OF_YEAR)
    }

    val difficulty: TaskDifficulty get() = when {
        points <= Constants.POINTS_EASY -> TaskDifficulty.EASY
        points <= Constants.POINTS_MODERATE -> TaskDifficulty.MODERATE
        else -> TaskDifficulty.DIFFICULT
    }
}

class TaskPreferences(private val context: Context) {
    companion object {
        private val TASKS_KEY = stringPreferencesKey("tasks")
        private val COMPLETED_HISTORY_KEY = stringPreferencesKey("completed_history")
        private val TOTAL_XP_KEY = intPreferencesKey("total_xp")
        private val FILTER_CATEGORIES_KEY = stringPreferencesKey("filter_categories")
        private val FILTER_FREQUENCIES_KEY = stringPreferencesKey("filter_frequencies")
        private val FILTER_FAVORITE_CATEGORIES_KEY = stringPreferencesKey("filter_favorite_categories")
        private val FILTER_FAVORITE_FREQUENCIES_KEY = stringPreferencesKey("filter_favorite_frequencies")
        private val OPTIONAL_TASKS_EXPANDED_KEY = booleanPreferencesKey("optional_tasks_expanded")
        private val OTHER_DAYS_TASKS_EXPANDED_KEY = booleanPreferencesKey("other_days_tasks_expanded")
        
        private const val DELIMITER_ITEM = ";"
        private const val DELIMITER_FIELD = "|"
    }

    val filters: Flow<TaskFilters> = context.taskDataStore.data.map { preferences ->
        TaskFilters(
            categories = preferences[FILTER_CATEGORIES_KEY]?.toCategorySet() ?: emptySet(),
            frequencies = preferences[FILTER_FREQUENCIES_KEY]?.toFrequencySet() ?: emptySet(),
            favoriteCategories = preferences[FILTER_FAVORITE_CATEGORIES_KEY]?.toCategorySet() ?: emptySet(),
            favoriteFrequencies = preferences[FILTER_FAVORITE_FREQUENCIES_KEY]?.toFrequencySet() ?: emptySet()
        )
    }

    private fun String.toCategorySet() = split(",").filter { it.isNotEmpty() }.mapNotNull { runCatching { TaskCategory.valueOf(it) }.getOrNull() }.toSet()
    private fun String.toFrequencySet() = split(",").filter { it.isNotEmpty() }.mapNotNull { runCatching { TaskFrequency.valueOf(it) }.getOrNull() }.toSet()

    suspend fun updateFilters(newFilters: TaskFilters) {
        context.taskDataStore.edit { preferences ->
            preferences[FILTER_CATEGORIES_KEY] = newFilters.categories.joinToString(",") { it.name }
            preferences[FILTER_FREQUENCIES_KEY] = newFilters.frequencies.joinToString(",") { it.name }
            preferences[FILTER_FAVORITE_CATEGORIES_KEY] = newFilters.favoriteCategories.joinToString(",") { it.name }
            preferences[FILTER_FAVORITE_FREQUENCIES_KEY] = newFilters.favoriteFrequencies.joinToString(",") { it.name }
        }
    }

    val isOptionalExpanded: Flow<Boolean> = context.taskDataStore.data.map { it[OPTIONAL_TASKS_EXPANDED_KEY] ?: false }

    suspend fun setOptionalExpanded(expanded: Boolean) {
        context.taskDataStore.edit { it[OPTIONAL_TASKS_EXPANDED_KEY] = expanded }
    }

    val isOtherDaysExpanded: Flow<Boolean> = context.taskDataStore.data.map { it[OTHER_DAYS_TASKS_EXPANDED_KEY] ?: false }

    suspend fun setOtherDaysExpanded(expanded: Boolean) {
        context.taskDataStore.edit { it[OTHER_DAYS_TASKS_EXPANDED_KEY] = expanded }
    }

    val totalXp: Flow<Int> = context.taskDataStore.data.map { it[TOTAL_XP_KEY] ?: 0 }

    suspend fun syncXpIfMissing() {
        context.taskDataStore.edit { preferences ->
            if (preferences[TOTAL_XP_KEY] == null) {
                val tasks = preferences[TASKS_KEY]?.split(DELIMITER_ITEM)?.mapNotNull { item ->
                    val parts = item.split(DELIMITER_FIELD)
                    if (parts.size >= 8) parts[7].toIntOrNull() else if (parts.size >= 4 && parts[3].toBoolean()) parts[2].toIntOrNull() else null
                }?.sum() ?: 0

                val history = preferences[COMPLETED_HISTORY_KEY]?.split(DELIMITER_ITEM)?.mapNotNull { item ->
                    val parts = item.split(DELIMITER_FIELD)
                    if (parts.size >= 11 && ActivityType.valueOf(parts[10]) == ActivityType.TASK) parts[3].toIntOrNull() else null
                }?.sum() ?: 0

                preferences[TOTAL_XP_KEY] = maxOf(tasks, history)
            }
        }
    }

    suspend fun syncTasks() {
        context.taskDataStore.edit { preferences ->
            val current = preferences[TASKS_KEY] ?: return@edit
            val updated = current.split(DELIMITER_ITEM).mapNotNull { item ->
                if (item.isBlank()) return@mapNotNull null
                val task = item.toTask() ?: return@mapNotNull item
                if (task.frequency != TaskFrequency.ONE_TIME && task.isExpired()) {
                    val parts = item.split(DELIMITER_FIELD).toMutableList()
                    parts[3] = "false" // isCompleted
                    parts[11] = "0" // currentProgress
                    parts.joinToString(DELIMITER_FIELD)
                } else item
            }
            preferences[TASKS_KEY] = updated.joinToString(DELIMITER_ITEM)
        }
    }

    val tasks: Flow<List<Task>> = context.taskDataStore.data.map { it[TASKS_KEY]?.split(DELIMITER_ITEM)?.mapNotNull { item -> item.toTask() } ?: emptyList() }

    val completedHistory: Flow<List<CompletedTaskRecord>> = context.taskDataStore.data.map { 
        it[COMPLETED_HISTORY_KEY]?.split(DELIMITER_ITEM)?.mapNotNull { item -> item.toHistoryRecord() } ?: emptyList() 
    }

    suspend fun addTask(task: Task) {
        context.taskDataStore.edit { preferences ->
            val current = preferences[TASKS_KEY] ?: ""
            val entry = task.toEntry()
            preferences[TASKS_KEY] = if (current.isBlank()) entry else "${current.trimEnd(DELIMITER_ITEM.single())}$DELIMITER_ITEM$entry"
        }
    }

    suspend fun toggleTaskCompletion(taskId: String, forceUndo: Boolean = false) {
        context.taskDataStore.edit { preferences ->
            val current = preferences[TASKS_KEY] ?: ""
            var recordedTask: Task? = null
            
            val updated = current.split(DELIMITER_ITEM).mapNotNull { item ->
                val task = item.toTask() ?: return@mapNotNull null
                if (task.id != taskId) return@mapNotNull item
                
                if (task.isBadHabit) {
                    val points = task.points
                    val currentXp = preferences[TOTAL_XP_KEY] ?: 0
                    preferences[TOTAL_XP_KEY] = (currentXp - points).coerceAtLeast(0)
                    recordedTask = task.copy(lastCompletedDate = System.currentTimeMillis())
                    return@mapNotNull task.copy(accumulatedPoints = task.accumulatedPoints - points).toEntry()
                } else {
                    val expired = task.isExpired()
                    val shouldUndo = forceUndo || (task.isCompleted && !expired && task.canUndo)
                    
                    if (shouldUndo) {
                        val pointsToSubtract = if (task.isCompleted && task.targetProgress > 1) task.points + Constants.BONUS_POINTS else task.points
                        val currentXp = preferences[TOTAL_XP_KEY] ?: 0
                        preferences[TOTAL_XP_KEY] = (currentXp - pointsToSubtract).coerceAtLeast(0)
                        
                        val history = (preferences[COMPLETED_HISTORY_KEY] ?: "").split(DELIMITER_ITEM).toMutableList()
                        val idx = history.indexOfLast { it.contains("$DELIMITER_FIELD$taskId$DELIMITER_FIELD") }
                        if (idx != -1) {
                            history.removeAt(idx)
                            preferences[COMPLETED_HISTORY_KEY] = history.joinToString(DELIMITER_ITEM)
                        }
                        return@mapNotNull task.copy(isCompleted = false, currentProgress = (task.currentProgress - 1).coerceAtLeast(0), accumulatedPoints = task.accumulatedPoints - pointsToSubtract).toEntry()
                    } else {
                        val baseProgress = if (expired) 0 else task.currentProgress
                        var pointsEarned = task.points
                        var newProgress = baseProgress
                        var completed = false
                        
                        if (task.targetProgress > 1 && baseProgress < task.targetProgress) {
                            newProgress++
                            if (newProgress == task.targetProgress) {
                                completed = true
                                pointsEarned += Constants.BONUS_POINTS
                            }
                        } else {
                            completed = true
                            newProgress = task.targetProgress
                        }
                        
                        val currentXp = preferences[TOTAL_XP_KEY] ?: 0
                        preferences[TOTAL_XP_KEY] = currentXp + pointsEarned
                        recordedTask = task.copy(points = pointsEarned, isCompleted = completed, currentProgress = newProgress, lastCompletedDate = System.currentTimeMillis())
                        return@mapNotNull recordedTask.toEntry()
                    }
                }
            }
            preferences[TASKS_KEY] = updated.joinToString(DELIMITER_ITEM)
            recordedTask?.let { task ->
                val history = preferences[COMPLETED_HISTORY_KEY] ?: ""
                val record = "${UUID.randomUUID()}$DELIMITER_FIELD${task.id}$DELIMITER_FIELD${task.title}$DELIMITER_FIELD${task.points}$DELIMITER_FIELD${task.lastCompletedDate}$DELIMITER_FIELD${task.iconName ?: ""}$DELIMITER_FIELD${task.color}$DELIMITER_FIELD${task.category?.name ?: ""}$DELIMITER_FIELD${task.currentProgress}$DELIMITER_FIELD${task.targetProgress}$DELIMITER_FIELD${if (task.isBadHabit) ActivityType.SLIP_UP else ActivityType.TASK}"
                preferences[COMPLETED_HISTORY_KEY] = if (history.isBlank()) record else "$history$DELIMITER_ITEM$record"
            }
        }
    }

    suspend fun updateTask(taskId: String, newTitle: String, newPoints: Int, newIconName: String?, newFrequency: TaskFrequency, newColor: Long? = null, newCategory: TaskCategory? = null, newTargetProgress: Int = 1, isBadHabit: Boolean = false, isOptional: Boolean = false, newDaysOfWeek: Set<Int> = emptySet()) {
        context.taskDataStore.edit { preferences ->
            val current = preferences[TASKS_KEY] ?: ""
            val updated = current.split(DELIMITER_ITEM).mapNotNull { item ->
                val task = item.toTask() ?: return@mapNotNull null
                if (task.id == taskId) {
                    task.copy(title = newTitle.trim(), points = newPoints, iconName = newIconName ?: "", frequency = newFrequency, color = newColor ?: task.color, category = newCategory, targetProgress = newTargetProgress, isBadHabit = isBadHabit, isOptional = isOptional, daysOfWeek = newDaysOfWeek).toEntry()
                } else item
            }
            preferences[TASKS_KEY] = updated.joinToString(DELIMITER_ITEM)
        }
    }

    suspend fun deleteTasks(taskIds: Set<String>) {
        context.taskDataStore.edit { preferences ->
            val current = preferences[TASKS_KEY] ?: ""
            val updated = current.split(DELIMITER_ITEM).filter { item -> item.isNotBlank() && !taskIds.contains(item.split(DELIMITER_FIELD)[0].trim()) }
            preferences[TASKS_KEY] = updated.joinToString(DELIMITER_ITEM)
        }
    }

    suspend fun logRewardSpent(rewardId: String, title: String, points: Int, iconName: String?, isReusable: Boolean) {
        context.taskDataStore.edit { preferences ->
            val history = preferences[COMPLETED_HISTORY_KEY] ?: ""
            val record = "${UUID.randomUUID()}$DELIMITER_FIELD$rewardId$DELIMITER_FIELD$title$DELIMITER_FIELD$points$DELIMITER_FIELD${System.currentTimeMillis()}$DELIMITER_FIELD${iconName ?: ""}$DELIMITER_FIELD${Constants.REWARD_COLOR}$DELIMITER_FIELD$DELIMITER_FIELD 1$DELIMITER_FIELD${if (isReusable) Constants.REUSABLE_REWARD_FLAG else 1}$DELIMITER_FIELD${ActivityType.REWARD.name}"
            preferences[COMPLETED_HISTORY_KEY] = if (history.isBlank()) record else "$history$DELIMITER_ITEM$record"
        }
    }

    private fun Task.toEntry() = "$id$DELIMITER_FIELD$title$DELIMITER_FIELD$points$DELIMITER_FIELD$isCompleted$DELIMITER_FIELD${iconName ?: ""}$DELIMITER_FIELD$frequency$DELIMITER_FIELD$lastCompletedDate$DELIMITER_FIELD$accumulatedPoints$DELIMITER_FIELD$color$DELIMITER_FIELD${category?.name ?: ""}$DELIMITER_FIELD$createdAt$DELIMITER_FIELD$currentProgress$DELIMITER_FIELD$targetProgress$DELIMITER_FIELD$isBadHabit$DELIMITER_FIELD$isOptional$DELIMITER_FIELD${daysOfWeek.joinToString(",")}"

    private fun String.toTask(): Task? {
        val p = split(DELIMITER_FIELD)
        if (p.size < 4) return null
        return Task(
            id = p[0].trim(), title = p[1].trim(), points = p[2].toIntOrNull() ?: 0, isCompleted = p[3].toBoolean(),
            iconName = p.getOrNull(4)?.trim()?.takeIf { it.isNotEmpty() },
            frequency = p.getOrNull(5)?.let { runCatching { TaskFrequency.valueOf(it) }.getOrNull() } ?: TaskFrequency.ONE_TIME,
            lastCompletedDate = p.getOrNull(6)?.toLongOrNull() ?: 0L,
            accumulatedPoints = p.getOrNull(7)?.toIntOrNull() ?: if (p[3].toBoolean()) p[2].toIntOrNull() ?: 0 else 0,
            color = p.getOrNull(8)?.toLongOrNull() ?: Constants.DEFAULT_TASK_COLOR,
            category = p.getOrNull(9)?.let { if (it == "null" || it.isEmpty()) null else runCatching { TaskCategory.valueOf(it) }.getOrNull() },
            createdAt = p.getOrNull(10)?.toLongOrNull() ?: System.currentTimeMillis(),
            currentProgress = p.getOrNull(11)?.toIntOrNull() ?: 0,
            targetProgress = p.getOrNull(12)?.toIntOrNull() ?: 1,
            isBadHabit = p.getOrNull(13)?.toBoolean() ?: false,
            isOptional = p.getOrNull(14)?.toBoolean() ?: false,
            daysOfWeek = p.getOrNull(15)?.split(",")?.filter { it.isNotEmpty() }?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet()
        )
    }

    private fun String.toHistoryRecord(): CompletedTaskRecord? {
        val p = split(DELIMITER_FIELD)
        if (p.size < 5) return null
        return CompletedTaskRecord(
            historyId = p[0], taskId = p[1], title = p[2], points = p[3].toIntOrNull() ?: 0, completionDate = p[4].toLongOrNull() ?: 0L,
            iconName = p.getOrNull(5)?.trim()?.takeIf { it.isNotEmpty() },
            color = p.getOrNull(6)?.toLongOrNull() ?: Constants.DEFAULT_TASK_COLOR,
            category = p.getOrNull(7)?.let { if (it == "null" || it.isEmpty()) null else runCatching { TaskCategory.valueOf(it) }.getOrNull() },
            currentProgress = p.getOrNull(8)?.toIntOrNull() ?: 0,
            targetProgress = p.getOrNull(9)?.toIntOrNull() ?: 1,
            type = p.getOrNull(10)?.let { runCatching { ActivityType.valueOf(it) }.getOrNull() } ?: ActivityType.TASK
        )
    }

    suspend fun addXp(amount: Int) {
        context.taskDataStore.edit { preferences ->
            val currentXp = preferences[TOTAL_XP_KEY] ?: 0
            preferences[TOTAL_XP_KEY] = currentXp + amount
        }
    }

    suspend fun debugFastForward24h() {
        context.taskDataStore.edit { preferences ->
            val tasks = (preferences[TASKS_KEY] ?: "").split(DELIMITER_ITEM).mapNotNull { item ->
                val task = item.toTask() ?: return@mapNotNull null
                val lastDate = if (task.lastCompletedDate != 0L) task.lastCompletedDate - Constants.DAY_IN_MILLIS else 0L
                val createdAt = task.createdAt - Constants.DAY_IN_MILLIS
                val shifted = task.copy(lastCompletedDate = lastDate, createdAt = createdAt)
                if (shifted.isExpired()) shifted.copy(isCompleted = false, currentProgress = 0).toEntry() else shifted.toEntry()
            }
            preferences[TASKS_KEY] = tasks.joinToString(DELIMITER_ITEM)

            val history = (preferences[COMPLETED_HISTORY_KEY] ?: "").split(DELIMITER_ITEM).map { item ->
                if (item.isBlank()) return@map item
                val p = item.split(DELIMITER_FIELD).toMutableList()
                if (p.size < 5) return@map item
                p[4] = ((p[4].toLongOrNull() ?: 0L) - Constants.DAY_IN_MILLIS).toString()
                p.joinToString(DELIMITER_FIELD)
            }
            preferences[COMPLETED_HISTORY_KEY] = history.joinToString(DELIMITER_ITEM)
        }
    }
}
