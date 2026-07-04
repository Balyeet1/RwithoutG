package com.example.rewardwithoutguilt.util

object Constants {
    // Greeting
    const val MAX_GREETING_CHARS = 18
    const val DEFAULT_IMAGE_SIZE = 128f
    const val MIN_IMAGE_SIZE = 100f
    const val MAX_IMAGE_SIZE = 350f
    const val GREETING_TIMEOUT_MS = 3000L
    const val DEFAULT_GREETING = "Welcome!"
    const val GREETING_IMAGE_NAME = "custom_greeting_image.jpg"
    
    // DataStore Names
    const val PREFS_GREETING = "greeting_prefs"
    const val PREFS_NOTE = "note_prefs"
    const val PREFS_REWARD = "reward_prefs"
    const val PREFS_TASK = "task_prefs"
    const val PREFS_NOTIFICATION = "notification_prefs"
    const val PREFS_PUZZLE = "puzzle_prefs"

    // Notifications
    const val NOTIFICATION_CHANNEL_ID = "task_reminders"
    const val NOTIFICATION_ID = 1

    // Task & Reward Logic
    const val UNDO_ANIMATION_DELAY = 700L
    const val WEEKLY_POINTS_GOAL = 1200
    const val DAY_IN_MILLIS = 24 * 60 * 60 * 1000L
    const val HOUR_IN_MILLIS = 60 * 60 * 1000L
    const val REUSABLE_REWARD_FLAG = -1
    const val BONUS_POINTS = 10
    const val DEFAULT_TASK_COLOR = 0xFF3B82F6L
    const val REWARD_COLOR = 0xFF6366F1L
    
    // XP / Progress
    const val POINTS_EASY = 25
    const val POINTS_MODERATE = 75
}
