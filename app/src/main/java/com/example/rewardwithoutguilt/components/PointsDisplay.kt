package com.example.rewardwithoutguilt.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.rewardwithoutguilt.R
import com.example.rewardwithoutguilt.data.RewardPreferences
import com.example.rewardwithoutguilt.data.TaskPreferences

import androidx.compose.ui.tooling.preview.Preview
import com.example.rewardwithoutguilt.ui.theme.RewardWithoutGuiltTheme

@Composable
fun PointsDisplay(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val taskPrefs = remember { TaskPreferences(context) }
    val totalXp by taskPrefs.totalXp.collectAsState(initial = 0)
    
    val rewardPrefs = remember { RewardPreferences(context) }
    val rewardsOpt by rewardPrefs.rewards.collectAsState(initial = null)
    val totalSpent by rewardPrefs.totalSpent.collectAsState(initial = 0)
    
    val totalPoints = remember(totalXp, rewardsOpt, totalSpent) {
        if (rewardsOpt == null) null else totalXp - totalSpent
    }

    PointsDisplayContent(
        points = totalPoints ?: 0,
        modifier = modifier
    )
}

@Composable
fun PointsDisplayContent(
    points: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.total_points, points),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PointsDisplayPreview() {
    RewardWithoutGuiltTheme {
        PointsDisplayContent(points = 350)
    }
}

