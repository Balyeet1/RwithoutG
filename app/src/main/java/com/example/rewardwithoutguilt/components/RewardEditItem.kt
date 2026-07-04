package com.example.rewardwithoutguilt.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.example.rewardwithoutguilt.R
import com.example.rewardwithoutguilt.data.Reward

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RewardEditItem(
    reward: Reward,
    onIconClick: () -> Unit,
    onTitleChange: (String) -> Unit,
    onPointsClick: () -> Unit,
    modifier: Modifier = Modifier,
    isIconSelected: Boolean = false,
    isPointsSelected: Boolean = false,
    titleColor: Color? = null,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            2.dp,
            Color(0xFF4CAF50).copy(alpha = 0.3f) // Standard green border for preview
        ),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon section
            Box(
                modifier = Modifier
                    .combinedClickable(onClick = onIconClick)
                    .border(
                        2.dp,
                        if (isIconSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                        CircleShape
                    )
                    .padding(2.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = RewardIcons.getIcon(reward.iconName, Icons.Default.Star),
                            contentDescription = stringResource(RewardIcons.getLabel(reward.iconName)),
                            tint = Color(0xFF4CAF50).copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))

            // Title section
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp)
            ) {
                BasicTextField(
                    value = reward.title,
                    onValueChange = onTitleChange,
                    textStyle = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = titleColor ?: MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { innerTextField ->
                        if (reward.title.isEmpty()) {
                            Text(
                                text = stringResource(R.string.reward_title_label),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = titleColor ?: MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                        innerTextField()
                    }
                )
                if (reward.isReusable) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
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
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = stringResource(R.string.reward_reusable_badge).uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Points section
            Box(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .combinedClickable(onClick = onPointsClick)
                    .border(
                        1.dp,
                        if (isPointsSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                        RoundedCornerShape(8.dp)
                    )
                    .padding(4.dp)
            ) {
                Text(
                    text = stringResource(R.string.points_label, reward.cost),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Default State")
@Composable
fun PreviewRewardEditItemDefault() {
    RewardEditItem(
        reward = Reward(id = "1", title = "Watch a Movie", cost = 150, iconName = RewardIcons.MOVIE),
        onIconClick = {},
        onTitleChange = {},
        onPointsClick = {}
    )
}

@Preview(showBackground = true, name = "Reusable Reward")
@Composable
fun PreviewRewardEditItemReusable() {
    RewardEditItem(
        reward = Reward(id = "2", title = "Play Video Games", cost = 100, iconName = RewardIcons.GAME, isReusable = true),
        onIconClick = {},
        onTitleChange = {},
        onPointsClick = {}
    )
}

@Preview(showBackground = true, name = "Icon Selected")
@Composable
fun PreviewRewardEditItemIconSelected() {
    RewardEditItem(
        reward = Reward(id = "3", title = "Drink Coffee", cost = 50, iconName = RewardIcons.COFFEE),
        onIconClick = {},
        onTitleChange = {},
        onPointsClick = {},
        isIconSelected = true
    )
}

@Preview(showBackground = true, name = "Points Selected")
@Composable
fun PreviewRewardEditItemPointsSelected() {
    RewardEditItem(
        reward = Reward(id = "4", title = "Eat Pizza", cost = 200, iconName = RewardIcons.FOOD),
        onIconClick = {},
        onTitleChange = {},
        onPointsClick = {},
        isPointsSelected = true
    )
}

@Preview(showBackground = true, name = "Empty Title (Hint)")
@Composable
fun PreviewRewardEditItemEmptyTitle() {
    RewardEditItem(
        reward = Reward(id = "5", title = "", cost = 0, iconName = RewardIcons.DEFAULT),
        onIconClick = {},
        onTitleChange = {},
        onPointsClick = {}
    )
}
