package com.example.rewardwithoutguilt.features.manage.task

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.rewardwithoutguilt.R
import com.example.rewardwithoutguilt.components.TaskIcons
import com.example.rewardwithoutguilt.data.TaskCategory
import com.example.rewardwithoutguilt.data.TaskTemplate
import com.example.rewardwithoutguilt.data.TaskTemplates
import com.example.rewardwithoutguilt.features.manage.task.getCategoryColor
import androidx.compose.ui.tooling.preview.Preview
import com.example.rewardwithoutguilt.ui.theme.RewardWithoutGuiltTheme


@Composable
fun TaskTemplateListScreen(
    onTemplateSelected: (TaskTemplate) -> Unit
) {
    val groupedTemplates = remember {
        TaskTemplates.templates.groupBy { it.category }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        // Standard Templates by Category
        groupedTemplates.forEach { (category, templates) ->
            item {
                CategoryHeader(category)
            }
            items(
                items = templates,
                key = { it.title }
            ) { template ->
                TemplateItem(
                    template = template,
                    onClick = { onTemplateSelected(template) }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                )
            }
        }
    }
}

@Composable
private fun CategoryHeader(category: TaskCategory?) {
    val categoryName = when (category) {
        TaskCategory.HEALTH -> stringResource(R.string.category_health)
        TaskCategory.HOME -> stringResource(R.string.category_home)
        TaskCategory.WORK -> stringResource(R.string.category_work)
        TaskCategory.ANIMALS -> stringResource(R.string.category_animals)
        TaskCategory.PERSONAL -> stringResource(R.string.category_personal)
        null -> stringResource(R.string.category_none)
    }

    Text(
        text = categoryName.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
    )
}

@Composable
private fun TemplateItem(
    template: TaskTemplate,
    onClick: () -> Unit
) {
    val categoryColor = Color(getCategoryColor(template.category))
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = TaskIcons.getIcon(template.iconName),
            contentDescription = null,
            tint = categoryColor,
            modifier = Modifier.padding(end = 16.dp)
        )
        Column {
            Text(
                text = template.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            val typeText = if (template.isBadHabit) stringResource(R.string.bad_habit_label_short) else template.frequency.name.lowercase().replace('_', ' ')
            Text(
                text = "${template.points} pts • $typeText",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TaskTemplateListScreenPreview() {
    RewardWithoutGuiltTheme {
        TaskTemplateListScreen(
            onTemplateSelected = {}
        )
    }
}

