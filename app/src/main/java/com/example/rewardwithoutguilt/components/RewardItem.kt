package com.example.rewardwithoutguilt.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rewardwithoutguilt.R
import com.example.rewardwithoutguilt.data.Reward
import androidx.compose.ui.tooling.preview.Preview
import com.example.rewardwithoutguilt.ui.theme.RewardWithoutGuiltTheme

import kotlinx.coroutines.async
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RewardItem(
    reward: Reward,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    modifier: Modifier = Modifier,
    totalPoints: Int = 0,
    onUnaffordableClick: (Reward, Int) -> Unit = { _, _ -> },
    isAnimatingSpent: Boolean = false,
) {
    val spendEnabled = totalPoints >= reward.cost
    val deficit = reward.cost - totalPoints

    val pointsOffset = remember { Animatable(0f) }
    val pointsAlpha = remember { Animatable(1f) }
    val itemAlpha = remember { Animatable(1f) }

    LaunchedEffect(isAnimatingSpent) {
        if (isAnimatingSpent) {
            val anim1 = async {
                pointsOffset.animateTo(
                    targetValue = -150f,
                    animationSpec = tween(durationMillis = 700)
                )
            }
            val anim2 = async {
                pointsAlpha.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 700)
                )
            }
            val anim3 = if (!reward.isReusable) {
                async {
                    itemAlpha.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(durationMillis = 700)
                    )
                }
            } else null

            anim1.await()
            anim2.await()
            anim3?.await()

            if (reward.isReusable) {
                pointsOffset.snapTo(0f)
                pointsAlpha.snapTo(1f)
            }
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { alpha = itemAlpha.value }
            .combinedClickable(
                onClick = {
                    if (isAnimatingSpent) return@combinedClickable
                    if (!isSelectionMode && !reward.isSpent) {
                        if (spendEnabled) onClick() else onUnaffordableClick(reward, deficit)
                    } else {
                        onClick()
                    }
                },
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) MaterialTheme.colorScheme.surfaceVariant
        else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            2.dp,
            when {
                isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                reward.isSpent -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                spendEnabled -> Color(0xFF4CAF50).copy(alpha = 0.3f)
                else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
            }
        ),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelectionMode && !reward.isSpent) {
                Box(
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            } else {
                Surface(
                    modifier = Modifier.size(40.dp).clip(CircleShape),
                    color = if (isSelected) Color.Transparent
                    else if (!spendEnabled && !reward.isSpent) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f)
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = RewardIcons.getIcon(reward.iconName, Icons.Default.Star),
                            contentDescription = stringResource(RewardIcons.getLabel(reward.iconName)),
                            tint = when {
                                isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                                reward.isSpent || isAnimatingSpent -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                !spendEnabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                                else -> Color(0xFF4CAF50).copy(alpha = 0.7f)
                            },
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = reward.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = if (reward.isSpent) TextDecoration.LineThrough else null,
                        color = when {
                            reward.isSpent || isAnimatingSpent -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            !spendEnabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                    if (reward.isReusable) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = if (reward.isSpent) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Repeat,
                                    contentDescription = null,
                                    modifier = Modifier.size(10.dp),
                                    tint = if (reward.isSpent) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    else MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = stringResource(R.string.reward_reusable_badge).uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (reward.isSpent) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    else MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
                Text(
                    text = stringResource(R.string.points_label, reward.cost),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = when {
                        reward.isSpent || isAnimatingSpent -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        !spendEnabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        else -> Color(0xFF2E7D32)
                    }
                )
            }

            if (!reward.isSpent && !isSelectionMode && !spendEnabled) {
                Surface(
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = RewardIcons.getIcon(reward.iconName, Icons.Default.Star),
                            contentDescription = stringResource(RewardIcons.getLabel(reward.iconName)),
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.4f)
                        )
                        Text(
                            text = stringResource(R.string.points_short_label, deficit),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                        )
                    }
                }
            } else if (isAnimatingSpent || (reward.isSpent && !isSelectionMode)) {
                Box(
                    modifier = Modifier
                        .offset { IntOffset(0, pointsOffset.value.roundToInt()) }
                        .graphicsLayer { alpha = if (reward.isSpent && !isAnimatingSpent) 0f else pointsAlpha.value }
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.points_minus, -reward.cost),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RewardItemPreview() {
    RewardWithoutGuiltTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Affordable Reward:", style = MaterialTheme.typography.labelMedium)
            RewardItem(
                reward = Reward("1", "Drink Coffee", 100, iconName = "relax", isReusable = true),
                onClick = {},
                onLongClick = {},
                isSelected = false,
                isSelectionMode = false,
                totalPoints = 150
            )
            
            Text("Unaffordable Reward:", style = MaterialTheme.typography.labelMedium)
            RewardItem(
                reward = Reward("2", "Buy Gaming Console", 1000, iconName = "game"),
                onClick = {},
                onLongClick = {},
                isSelected = false,
                isSelectionMode = false,
                totalPoints = 150
            )

            Text("Selected Reward (Selection Mode):", style = MaterialTheme.typography.labelMedium)
            RewardItem(
                reward = Reward("3", "Watch Movie", 300, iconName = "movie", isReusable = true),
                onClick = {},
                onLongClick = {},
                isSelected = true,
                isSelectionMode = true,
                totalPoints = 150
            )

            Text("Spent Reward:", style = MaterialTheme.typography.labelMedium)
            RewardItem(
                reward = Reward("4", "Eat Pizza Slice", 250, isSpent = true, iconName = "food"),
                onClick = {},
                onLongClick = {},
                isSelected = false,
                isSelectionMode = false,
                totalPoints = 150
            )
        }
    }
}

