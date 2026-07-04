package com.example.rewardwithoutguilt.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.rewardwithoutguilt.R

object TaskIcons {
    const val DEFAULT = "default"
    const val WORK = "work"
    const val FITNESS = "fitness"
    const val BOOK = "book"
    const val CODE = "code"
    const val MEDITATION = "meditation"
    const val CLEANING = "cleaning"
    const val STUDY = "study"
    const val HEALTH = "health"
    const val SHOPPING = "shopping"
    const val HOME = "home"
    const val PETS = "pets"
    const val WATER = "water"
    const val GARDEN = "garden"
    const val CHICKEN = "chicken"
    const val DANCE = "dance"
    const val COW = "cow"
    const val RUN = "run"
    const val BIKE = "bike"
    const val SLEEP = "sleep"
    const val SOCIAL = "social"

    val icons = mapOf(
        DEFAULT to Icons.Default.TaskAlt,
        WORK to Icons.Default.Work,
        FITNESS to Icons.Default.FitnessCenter,
        BOOK to Icons.Default.Book,
        CODE to Icons.Default.Code,
        MEDITATION to Icons.Default.SelfImprovement,
        CLEANING to Icons.Default.CleaningServices,
        STUDY to Icons.Default.School,
        HEALTH to Icons.Default.Favorite,
        SHOPPING to Icons.Default.ShoppingCart,
        HOME to Icons.Default.Home,
        PETS to Icons.Default.Pets,
        WATER to Icons.Default.WaterDrop,
        GARDEN to Icons.Default.Park,
        CHICKEN to Icons.Default.Egg,
        DANCE to Icons.Default.MusicNote,
        COW to Icons.Default.Agriculture,
        RUN to Icons.AutoMirrored.Filled.DirectionsRun,
        BIKE to Icons.AutoMirrored.Filled.DirectionsBike,
        SLEEP to Icons.Default.Bed,
        SOCIAL to Icons.Default.Groups,
    )

    private val labels = mapOf(
        DEFAULT to R.string.icon_default,
        WORK to R.string.icon_work,
        FITNESS to R.string.icon_fitness,
        BOOK to R.string.icon_book,
        CODE to R.string.icon_code,
        MEDITATION to R.string.icon_meditation,
        CLEANING to R.string.icon_cleaning,
        STUDY to R.string.icon_study,
        HEALTH to R.string.icon_health,
        SHOPPING to R.string.icon_shopping,
        HOME to R.string.icon_home,
        PETS to R.string.icon_pets,
        WATER to R.string.icon_water,
        GARDEN to R.string.icon_garden,
        CHICKEN to R.string.icon_chicken,
        DANCE to R.string.icon_dance,
        COW to R.string.icon_cow,
        RUN to R.string.icon_run,
        BIKE to R.string.icon_bike,
        SLEEP to R.string.icon_sleep,
        SOCIAL to R.string.icon_social
    )

    fun getIcon(name: String?, default: ImageVector = Icons.Default.TaskAlt): ImageVector {
        return icons[name] ?: RewardIcons.icons[name] ?: default
    }

    fun getLabel(name: String?): Int {
        return labels[name] ?: RewardIcons.getLabel(name)
    }

    fun getCategoryLabel(category: com.example.rewardwithoutguilt.data.TaskCategory): Int {
        return when (category) {
            com.example.rewardwithoutguilt.data.TaskCategory.HEALTH -> R.string.category_health
            com.example.rewardwithoutguilt.data.TaskCategory.HOME -> R.string.category_home
            com.example.rewardwithoutguilt.data.TaskCategory.WORK -> R.string.category_work
            com.example.rewardwithoutguilt.data.TaskCategory.ANIMALS -> R.string.category_animals
            com.example.rewardwithoutguilt.data.TaskCategory.PERSONAL -> R.string.category_personal
        }
    }
}

object RewardIcons {
    const val DEFAULT = "default"
    const val FOOD = "food"
    const val COFFEE = "coffee"
    const val GAME = "game"
    const val MOVIE = "movie"
    const val TRAVEL = "travel"
    const val SHOPPING = "shopping"
    const val MUSIC = "music"
    const val RELAX = "relax"
    const val EVENT = "event"
    const val CAKE = "cake"
    const val HOBBY = "hobby"
    const val CAMERA = "camera"
    const val THEATER = "theater"

    val icons = mapOf(
        DEFAULT to Icons.Default.Star,
        FOOD to Icons.Default.Restaurant,
        COFFEE to Icons.Default.LocalCafe,
        GAME to Icons.Default.Gamepad,
        MOVIE to Icons.Default.Movie,
        TRAVEL to Icons.Default.Flight,
        SHOPPING to Icons.Default.LocalMall,
        MUSIC to Icons.Default.MusicNote,
        RELAX to Icons.Default.Spa,
        EVENT to Icons.Default.Celebration,
        CAKE to Icons.Default.Cake,
        HOBBY to Icons.Default.Palette,
        CAMERA to Icons.Default.PhotoCamera,
        THEATER to Icons.Default.TheaterComedy
    )

    private val labels = mapOf(
        DEFAULT to R.string.icon_default,
        FOOD to R.string.icon_food,
        COFFEE to R.string.icon_coffee,
        GAME to R.string.icon_game,
        MOVIE to R.string.icon_movie,
        TRAVEL to R.string.icon_travel,
        SHOPPING to R.string.icon_shopping,
        MUSIC to R.string.icon_music,
        RELAX to R.string.icon_relax,
        EVENT to R.string.icon_event,
        CAKE to R.string.icon_cake,
        HOBBY to R.string.icon_hobby,
        CAMERA to R.string.icon_camera,
        THEATER to R.string.icon_theater
    )

    fun getIcon(name: String?, default: ImageVector = Icons.Default.Star): ImageVector {
        return icons[name] ?: default
    }

    fun getLabel(name: String?): Int {
        return labels[name] ?: R.string.icon_default
    }
}
