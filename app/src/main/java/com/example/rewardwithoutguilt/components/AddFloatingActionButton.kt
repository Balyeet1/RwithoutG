package com.example.rewardwithoutguilt.components

import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.rewardwithoutguilt.R

import androidx.compose.ui.tooling.preview.Preview
import com.example.rewardwithoutguilt.ui.theme.RewardWithoutGuiltTheme

@Composable
fun AddFloatingActionButton(
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier.offset(y = 40.dp).clip(CircleShape)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_add),
            contentDescription = contentDescription
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AddFloatingActionButtonPreview() {
    RewardWithoutGuiltTheme {
        AddFloatingActionButton(
            onClick = {},
            contentDescription = "Add"
        )
    }
}

