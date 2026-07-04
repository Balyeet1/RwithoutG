package com.example.rewardwithoutguilt.data

import com.example.rewardwithoutguilt.components.TaskIcons

data class TaskTemplate(
    val title: String,
    val points: Int,
    val iconName: String,
    val category: TaskCategory?,
    val frequency: TaskFrequency = TaskFrequency.ONE_TIME,
    val targetProgress: Int = 1,
    val isBadHabit: Boolean = false
)

object TaskTemplates {
    val templates = listOf(
        TaskTemplate("Drink Water", 10, TaskIcons.WATER, TaskCategory.HEALTH, TaskFrequency.DAILY, 8),
        TaskTemplate("Read a Book", 30, TaskIcons.BOOK, TaskCategory.HOME, TaskFrequency.DAILY, 1),
        TaskTemplate("Morning Workout", 50, TaskIcons.FITNESS, TaskCategory.HEALTH, TaskFrequency.DAILY, 1),
        TaskTemplate("Meditation", 25, TaskIcons.MEDITATION, TaskCategory.HEALTH, TaskFrequency.DAILY, 1),
        TaskTemplate("Clean Room", 40, TaskIcons.CLEANING, TaskCategory.HOME, TaskFrequency.WEEKLY, 1),
        TaskTemplate("Study", 45, TaskIcons.STUDY, TaskCategory.WORK, TaskFrequency.DAILY, 1),
        TaskTemplate("Walk the Dog", 20, TaskIcons.PETS, TaskCategory.ANIMALS, TaskFrequency.DAILY, 1),
        TaskTemplate("Groceries", 15, TaskIcons.SHOPPING, TaskCategory.HOME, TaskFrequency.ONE_TIME, 1),
        TaskTemplate("Water Plants", 10, TaskIcons.GARDEN, TaskCategory.HOME, TaskFrequency.WEEKLY, 1),
        TaskTemplate("Coding Practice", 60, TaskIcons.CODE, TaskCategory.WORK, TaskFrequency.DAILY, 1),
        TaskTemplate("Evening Run", 55, TaskIcons.RUN, TaskCategory.HEALTH, TaskFrequency.DAILY, 1),
        TaskTemplate("Bicycle Commute", 35, TaskIcons.BIKE, TaskCategory.HEALTH, TaskFrequency.DAILY, 1),
        TaskTemplate("Yoga/Dance", 30, TaskIcons.DANCE, TaskCategory.HEALTH, TaskFrequency.WEEKLY, 1),
        TaskTemplate("Early Sleep", 40, TaskIcons.SLEEP, TaskCategory.HEALTH, TaskFrequency.DAILY, 1),
        TaskTemplate("Feed the Chickens", 15, TaskIcons.CHICKEN, TaskCategory.ANIMALS, TaskFrequency.DAILY, 1),
        TaskTemplate("Farm Maintenance", 70, TaskIcons.COW, TaskCategory.ANIMALS, TaskFrequency.WEEKLY, 1),
        TaskTemplate("Family Dinner", 45, TaskIcons.SOCIAL, TaskCategory.PERSONAL, TaskFrequency.WEEKLY, 1),
        TaskTemplate("Call a Friend", 20, TaskIcons.SOCIAL, TaskCategory.PERSONAL, TaskFrequency.WEEKLY, 1),
        TaskTemplate("Organize Desk", 25, TaskIcons.WORK, TaskCategory.WORK, TaskFrequency.WEEKLY, 1),
        TaskTemplate("Laundry", 30, TaskIcons.CLEANING, TaskCategory.HOME, TaskFrequency.WEEKLY, 1)
    )
}
