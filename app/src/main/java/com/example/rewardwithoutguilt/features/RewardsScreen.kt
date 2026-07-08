package com.example.rewardwithoutguilt.features

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.rewardwithoutguilt.R
import com.example.rewardwithoutguilt.components.RedeemConfirmationDialog
import com.example.rewardwithoutguilt.components.RewardItem
import com.example.rewardwithoutguilt.components.SelectionActionBar
import com.example.rewardwithoutguilt.data.Reward
import com.example.rewardwithoutguilt.data.RewardPreferences
import com.example.rewardwithoutguilt.data.TaskPreferences
import com.example.rewardwithoutguilt.ui.theme.RewardWithoutGuiltTheme
import com.example.rewardwithoutguilt.util.Constants
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun RewardsScreen(onEditReward: (String) -> Unit = {}) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val taskPrefs = remember { TaskPreferences(context) }
    val totalXp by taskPrefs.totalXp.collectAsState(initial = 0)

    val rewardPrefs = remember { RewardPreferences(context) }
    val rewardsOpt by rewardPrefs.rewards.collectAsState(initial = null)
    val rewards = rewardsOpt ?: emptyList()
    val totalSpent by rewardPrefs.totalSpent.collectAsState(initial = 0)

    val totalPoints = remember(totalXp, totalSpent) { totalXp - totalSpent }
    var animatingIds by remember { mutableStateOf(setOf<String>()) }
    var rewardToRedeem by remember { mutableStateOf<Reward?>(null) }

    val sortedRewards = remember(rewards, totalPoints, animatingIds) {
        rewards.filter { !it.isSpent || animatingIds.contains(it.id) }.sortedByDescending { totalPoints >= it.cost }
    }

    var selectedRewardIds by remember { mutableStateOf(setOf<String>()) }
    val isSelectionMode = selectedRewardIds.isNotEmpty()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            Spacer(modifier = Modifier.height(4.dp))
            SelectionActionBar(
                selectedCount = selectedRewardIds.size,
                onCloseSelection = { selectedRewardIds = emptySet() },
                onDelete = { scope.launch { rewardPrefs.deleteRewards(selectedRewardIds); selectedRewardIds = emptySet() } },
                onEdit = { selectedRewardIds.firstOrNull()?.let { onEditReward(it) }; selectedRewardIds = emptySet() }
            )

            if (rewardsOpt == null) {
                Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
            } else if (rewards.isEmpty()) {
                Column(Modifier.fillMaxSize(), Arrangement.Center, Alignment.CenterHorizontally) {
                    Text(stringResource(R.string.no_rewards_title))
                    Text(stringResource(R.string.add_some_rewards))
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(sortedRewards, key = { it.id }) { reward ->
                        RewardItem(
                            reward = reward, totalPoints = totalPoints, isSelected = selectedRewardIds.contains(reward.id), isSelectionMode = isSelectionMode, isAnimatingSpent = animatingIds.contains(reward.id),
                            onUnaffordableClick = { r, deficit ->
                                scope.launch {
                                    snackbarHostState.currentSnackbarData?.dismiss()
                                    snackbarHostState.showSnackbar(context.getString(R.string.points_missing_message, deficit, r.title))
                                }
                            },
                            onClick = {
                                if (isSelectionMode) {
                                    if (!reward.isSpent) selectedRewardIds = if (selectedRewardIds.contains(reward.id)) selectedRewardIds - reward.id else selectedRewardIds + reward.id
                                } else if (!reward.isSpent && !animatingIds.contains(reward.id) && totalPoints >= reward.cost) {
                                    rewardToRedeem = reward
                                }
                            },
                            onLongClick = { if (!isSelectionMode && !reward.isSpent) selectedRewardIds = setOf(reward.id) },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }
        
        rewardToRedeem?.let { reward ->
            RedeemConfirmationDialog(
                reward = reward,
                onConfirm = {
                    val rId = reward.id
                    rewardToRedeem = null
                    if (!animatingIds.contains(rId)) {
                        animatingIds = animatingIds + rId
                        scope.launch {
                            rewardPrefs.spendReward(rId)
                            taskPrefs.logRewardSpent(rId, reward.title, reward.cost, reward.iconName, reward.isReusable)
                            delay(Constants.UNDO_ANIMATION_DELAY)
                            animatingIds = animatingIds - rId
                        }
                    }
                },
                onDismiss = { rewardToRedeem = null }
            )
        }

        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)) { data ->
            Snackbar(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer, shape = RoundedCornerShape(24.dp), modifier = Modifier.padding(horizontal = 32.dp)) {
                Text(data.visuals.message, Modifier.fillMaxWidth(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onErrorContainer, textAlign = TextAlign.Center)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RewardsScreenPreview() {
    RewardWithoutGuiltTheme {
        RewardsScreen()
    }
}
