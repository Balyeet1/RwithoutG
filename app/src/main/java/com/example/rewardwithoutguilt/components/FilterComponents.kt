package com.example.rewardwithoutguilt.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.rewardwithoutguilt.R
import com.example.rewardwithoutguilt.data.TaskCategory
import com.example.rewardwithoutguilt.data.TaskFilters
import com.example.rewardwithoutguilt.data.TaskFrequency
import androidx.compose.ui.tooling.preview.Preview
import com.example.rewardwithoutguilt.ui.theme.RewardWithoutGuiltTheme
import androidx.compose.material3.HorizontalDivider

@Composable
fun ExpandableFilterSection(
    filters: TaskFilters,
    onFiltersChange: (TaskFilters) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by rememberSaveable { mutableStateOf(value = false) }
    val isActive = !filters.isEmpty
    Column(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                onClick = { expanded = !expanded },
                shape = RoundedCornerShape(12.dp),
                color = if (isActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface,
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier.height(40.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(horizontal = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(R.string.filter_label),
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Shortcuts for favorite filters
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                filters.favoriteCategories.forEach { category ->
                    val isSelected = filters.categories.contains(category)
                    val color = when (category) {
                        TaskCategory.HEALTH -> Color(0xFF10B981)
                        TaskCategory.HOME -> Color(0xFFF59E0B)
                        TaskCategory.WORK -> Color(0xFF6366F1)
                        TaskCategory.ANIMALS -> Color(0xFFEC4899)
                        TaskCategory.PERSONAL -> Color(0xFF8B5CF6)
                    }
                    FilterShortcut(
                        label = when (category) {
                            TaskCategory.HEALTH -> stringResource(R.string.category_health)
                            TaskCategory.HOME -> stringResource(R.string.category_home)
                            TaskCategory.WORK -> stringResource(R.string.category_work)
                            TaskCategory.ANIMALS -> stringResource(R.string.category_animals)
                            TaskCategory.PERSONAL -> stringResource(R.string.category_personal)
                        },
                        isSelected = isSelected,
                        color = color,
                        onClick = {
                            val newSet = if (isSelected) filters.categories - category else filters.categories + category
                            onFiltersChange(filters.copy(categories = newSet))
                        }
                    )
                }
                filters.favoriteFrequencies.forEach { freq ->
                    val isSelected = filters.frequencies.contains(freq)
                    val color = when (freq) {
                        TaskFrequency.DAILY -> Color(0xFFE65100)
                        TaskFrequency.WEEKLY -> Color(0xFF1565C0)
                        TaskFrequency.ONE_TIME -> MaterialTheme.colorScheme.primary
                    }
                    FilterShortcut(
                        label = when (freq) {
                            TaskFrequency.DAILY -> stringResource(R.string.frequency_daily)
                            TaskFrequency.WEEKLY -> stringResource(R.string.frequency_weekly)
                            TaskFrequency.ONE_TIME -> stringResource(R.string.frequency_one_time)
                        },
                        isSelected = isSelected,
                        color = color,
                        onClick = {
                            val newSet = if (isSelected) filters.frequencies - freq else filters.frequencies + freq
                            onFiltersChange(filters.copy(frequencies = newSet))
                        }
                    )
                }
            }
        }

        if (expanded) {
            Spacer(modifier = Modifier.height(16.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Category Column
                FilterRow(
                    title = stringResource(R.string.category_label),
                    items = TaskCategory.entries,
                    selectedItems = filters.categories,
                    favoriteItems = filters.favoriteCategories,
                    labelProvider = { category ->
                        when (category) {
                            TaskCategory.HEALTH -> stringResource(R.string.category_health)
                            TaskCategory.HOME -> stringResource(R.string.category_home)
                            TaskCategory.WORK -> stringResource(R.string.category_work)
                            TaskCategory.ANIMALS -> stringResource(R.string.category_animals)
                            TaskCategory.PERSONAL -> stringResource(R.string.category_personal)
                        }
                    },
                    onSelectionChange = { item ->
                        val newSet =
                            if (filters.categories.contains(item)) filters.categories - item else filters.categories + item
                        onFiltersChange(filters.copy(categories = newSet))
                    },
                    onToggleFavorite = { item ->
                        val newFavorites = if (filters.favoriteCategories.contains(item)) {
                            filters.favoriteCategories - item
                        } else {
                            filters.favoriteCategories + item
                        }
                        onFiltersChange(filters.copy(favoriteCategories = newFavorites))
                    },
                    selectedColorProvider = @Composable { category ->
                        when (category) {
                            TaskCategory.HEALTH -> Color(0xFF10B981)
                            TaskCategory.HOME -> Color(0xFFF59E0B)
                            TaskCategory.WORK -> Color(0xFF6366F1)
                            TaskCategory.ANIMALS -> Color(0xFFEC4899)
                            TaskCategory.PERSONAL -> Color(0xFF8B5CF6)
                        }
                    }
                )

                // Frequency Column
                FilterRow(
                    title = stringResource(R.string.frequency_label),
                    items = TaskFrequency.entries,
                    selectedItems = filters.frequencies,
                    favoriteItems = filters.favoriteFrequencies,
                    labelProvider = { freq ->
                        when (freq) {
                            TaskFrequency.DAILY -> stringResource(R.string.frequency_daily)
                            TaskFrequency.WEEKLY -> stringResource(R.string.frequency_weekly)
                            TaskFrequency.ONE_TIME -> stringResource(R.string.frequency_one_time)
                        }
                    },
                    onSelectionChange = { item ->
                        val newSet =
                            if (filters.frequencies.contains(item)) filters.frequencies - item else filters.frequencies + item
                        onFiltersChange(filters.copy(frequencies = newSet))
                    },
                    onToggleFavorite = { item ->
                        val newFavorites = if (filters.favoriteFrequencies.contains(item)) {
                            filters.favoriteFrequencies - item
                        } else {
                            filters.favoriteFrequencies + item
                        }
                        onFiltersChange(filters.copy(favoriteFrequencies = newFavorites))
                    },
                    selectedColorProvider = @Composable { freq ->
                        when (freq) {
                            TaskFrequency.DAILY -> Color(0xFFE65100)
                            TaskFrequency.WEEKLY -> Color(0xFF1565C0)
                            TaskFrequency.ONE_TIME -> MaterialTheme.colorScheme.primary
                        }
                    }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun FilterShortcut(
    label: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) color.copy(alpha = 0.15f) else Color.Transparent,
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) color else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier.height(40.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
private fun <T> FilterRow(
    title: String,
    items: List<T>,
    selectedItems: Set<T>,
    favoriteItems: Set<T>,
    labelProvider: @Composable (T) -> String,
    onSelectionChange: (T) -> Unit,
    onToggleFavorite: (T) -> Unit,
    selectedColorProvider: @Composable (T) -> Color
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items.forEach { item ->
                val isSelected = selectedItems.contains(item)
                val isFavorite = favoriteItems.contains(item)
                val selectedColor = selectedColorProvider(item)
                Box {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) selectedColor.copy(alpha = 0.15f) else Color.Transparent,
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isSelected) selectedColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.combinedClickable(
                            onClick = { onSelectionChange(item) },
                            onLongClick = { onToggleFavorite(item) }
                        )
                    ) {
                        Text(
                            text = labelProvider(item),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isSelected) selectedColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            modifier = Modifier.padding(12.dp)
                        )
                    }

                    if (isFavorite) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFF59E0B), // Amber color for the star
                            modifier = Modifier
                                .size(14.dp)
                                .align(Alignment.TopEnd)
                                .offset(x = 4.dp, y = (-4).dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ExpandableFilterSectionPreview() {
    RewardWithoutGuiltTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Empty Filters (Collapsed):", style = MaterialTheme.typography.titleMedium)
            ExpandableFilterSection(
                filters = TaskFilters(),
                onFiltersChange = {}
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Text("With Selected and Favorite Filters (Collapsed):", style = MaterialTheme.typography.titleMedium)
            ExpandableFilterSection(
                filters = TaskFilters(
                    categories = setOf(TaskCategory.HEALTH, TaskCategory.WORK),
                    frequencies = setOf(TaskFrequency.DAILY),
                    favoriteCategories = setOf(TaskCategory.HEALTH, TaskCategory.PERSONAL),
                    favoriteFrequencies = setOf(TaskFrequency.DAILY, TaskFrequency.WEEKLY)
                ),
                onFiltersChange = {}
            )
        }
    }
}

