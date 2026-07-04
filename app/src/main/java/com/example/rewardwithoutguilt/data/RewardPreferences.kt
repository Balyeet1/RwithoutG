package com.example.rewardwithoutguilt.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.rewardwithoutguilt.util.Constants
import androidx.compose.runtime.Immutable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.rewardDataStore by preferencesDataStore(name = Constants.PREFS_REWARD)

@Immutable
data class Reward(
    val id: String,
    val title: String,
    val cost: Int,
    val isSpent: Boolean = false,
    val iconName: String? = null,
    val isReusable: Boolean = false,
    val timesSpent: Int = 0
)

class RewardPreferences(private val context: Context) {
    companion object {
        private val REWARDS_KEY = stringPreferencesKey("rewards")
        private val TOTAL_SPENT_KEY = intPreferencesKey("total_spent")
        
        private const val DELIMITER_ITEM = ";"
        private const val DELIMITER_FIELD = "|"
    }

    val totalSpent: Flow<Int> = context.rewardDataStore.data.map { it[TOTAL_SPENT_KEY] ?: 0 }

    val rewards: Flow<List<Reward>> = context.rewardDataStore.data.map { preferences ->
        val rewardsString = preferences[REWARDS_KEY] ?: ""
        if (rewardsString.isBlank()) return@map emptyList()
        
        rewardsString.split(DELIMITER_ITEM).mapNotNull { item ->
            if (item.isBlank()) return@mapNotNull null
            val parts = item.split(DELIMITER_FIELD)
            if (parts.size < 4) return@mapNotNull null
            
            val isSpent = parts[3].trim().toBoolean()
            Reward(
                id = parts[0].trim(),
                title = parts[1].trim(),
                cost = parts[2].trim().toIntOrNull() ?: 0,
                isSpent = isSpent,
                iconName = parts.getOrNull(4)?.trim()?.takeIf { it.isNotEmpty() },
                isReusable = parts.getOrNull(5)?.trim()?.toBoolean() ?: false,
                timesSpent = parts.getOrNull(6)?.trim()?.toIntOrNull() ?: if (isSpent) 1 else 0
            )
        }
    }

    suspend fun addReward(reward: Reward) {
        context.rewardDataStore.edit { preferences ->
            val current = preferences[REWARDS_KEY] ?: ""
            val newEntry = "${reward.id}$DELIMITER_FIELD${reward.title}$DELIMITER_FIELD${reward.cost}$DELIMITER_FIELD${reward.isSpent}$DELIMITER_FIELD${reward.iconName ?: ""}$DELIMITER_FIELD${reward.isReusable}$DELIMITER_FIELD${reward.timesSpent}"
            preferences[REWARDS_KEY] = if (current.isBlank()) newEntry else "${current.trimEnd(DELIMITER_ITEM.single())}$DELIMITER_ITEM$newEntry"
        }
    }

    suspend fun spendReward(rewardId: String) {
        context.rewardDataStore.edit { preferences ->
            val current = preferences[REWARDS_KEY] ?: ""
            var costToAdd = 0
            val updated = current.split(DELIMITER_ITEM).mapNotNull { item ->
                if (item.isBlank()) return@mapNotNull null
                val parts = item.split(DELIMITER_FIELD)
                if (parts.size < 4) return@mapNotNull null
                
                val id = parts[0].trim()
                if (id == rewardId) {
                    val title = parts[1].trim()
                    val costStr = parts[2].trim()
                    val cost = costStr.toIntOrNull() ?: 0
                    val iconName = parts.getOrNull(4)?.trim() ?: ""
                    val isReusable = parts.getOrNull(5)?.trim()?.toBoolean() ?: false
                    val currentTimes = parts.getOrNull(6)?.trim()?.toIntOrNull() ?: if (parts[3].trim().toBoolean()) 1 else 0
                    
                    val newTimes = currentTimes + 1
                    val newIsSpent = !isReusable
                    costToAdd = cost
                    "$id$DELIMITER_FIELD$title$DELIMITER_FIELD$costStr$DELIMITER_FIELD$newIsSpent$DELIMITER_FIELD$iconName$DELIMITER_FIELD$isReusable$DELIMITER_FIELD$newTimes"
                } else item.trim()
            }
            preferences[REWARDS_KEY] = updated.joinToString(DELIMITER_ITEM)
            preferences[TOTAL_SPENT_KEY] = (preferences[TOTAL_SPENT_KEY] ?: 0) + costToAdd
        }
    }

    suspend fun updateReward(rewardId: String, newTitle: String, newCost: Int, newIconName: String?, newIsReusable: Boolean) {
        context.rewardDataStore.edit { preferences ->
            val current = preferences[REWARDS_KEY] ?: ""
            val updated = current.split(DELIMITER_ITEM).mapNotNull { item ->
                if (item.isBlank()) return@mapNotNull null
                val parts = item.split(DELIMITER_FIELD)
                if (parts.size < 4) return@mapNotNull null
                
                val id = parts[0].trim()
                if (id == rewardId) {
                    val isSpent = parts[3].trim()
                    val timesSpent = parts.getOrNull(6)?.trim() ?: if (isSpent.toBoolean()) "1" else "0"
                    "$id$DELIMITER_FIELD${newTitle.trim()}$DELIMITER_FIELD$newCost$DELIMITER_FIELD$isSpent$DELIMITER_FIELD${newIconName ?: ""}$DELIMITER_FIELD$newIsReusable$DELIMITER_FIELD$timesSpent"
                } else item.trim()
            }
            preferences[REWARDS_KEY] = updated.joinToString(DELIMITER_ITEM)
        }
    }

    suspend fun deleteRewards(rewardIds: Set<String>) {
        context.rewardDataStore.edit { preferences ->
            val current = preferences[REWARDS_KEY] ?: ""
            val updated = current.split(DELIMITER_ITEM).filter { item ->
                item.isNotBlank() && !rewardIds.contains(item.split(DELIMITER_FIELD)[0].trim())
            }
            preferences[REWARDS_KEY] = updated.joinToString(DELIMITER_ITEM)
        }
    }

    suspend fun syncSpentPointsIfMissing() {
        context.rewardDataStore.edit { preferences ->
            if (preferences[TOTAL_SPENT_KEY] == null) {
                val rewardsString = preferences[REWARDS_KEY] ?: ""
                val spentPoints = if (rewardsString.isBlank()) 0 else {
                    rewardsString.split(DELIMITER_ITEM).mapNotNull { item ->
                        if (item.isBlank()) return@mapNotNull null
                        val parts = item.split(DELIMITER_FIELD)
                        if (parts.size >= 4) {
                            val cost = parts[2].trim().toIntOrNull() ?: 0
                            val timesSpent = parts.getOrNull(6)?.trim()?.toIntOrNull() ?: if (parts[3].trim().toBoolean()) 1 else 0
                            cost * timesSpent
                        } else null
                    }.sum()
                }
                preferences[TOTAL_SPENT_KEY] = spentPoints
            }
        }
    }
}
