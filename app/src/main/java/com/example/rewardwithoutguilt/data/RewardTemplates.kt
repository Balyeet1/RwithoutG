package com.example.rewardwithoutguilt.data

import com.example.rewardwithoutguilt.components.RewardIcons

data class RewardTemplate(
    val title: String,
    val cost: Int,
    val iconName: String,
    val isReusable: Boolean = false
)

object RewardTemplates {
    val templates = listOf(
        RewardTemplate("Watch a Movie", 150, RewardIcons.MOVIE),
        RewardTemplate("Play Video Games", 100, RewardIcons.GAME, isReusable = true),
        RewardTemplate("Eat Pizza", 200, RewardIcons.FOOD),
        RewardTemplate("Drink Coffee", 50, RewardIcons.COFFEE, isReusable = true),
        RewardTemplate("Buy Something", 300, RewardIcons.SHOPPING),
        RewardTemplate("Travel", 1000, RewardIcons.TRAVEL),
        RewardTemplate("Listen to Music", 30, RewardIcons.MUSIC, isReusable = true),
        RewardTemplate("Relax / Spa Day", 400, RewardIcons.RELAX),
        RewardTemplate("Go to an Event", 250, RewardIcons.EVENT),
        RewardTemplate("Eat Cake", 80, RewardIcons.CAKE),
        RewardTemplate("Practice Hobby", 60, RewardIcons.HOBBY, isReusable = true),
        RewardTemplate("Take Photos", 70, RewardIcons.CAMERA, isReusable = true),
        RewardTemplate("Go to Theater", 350, RewardIcons.THEATER)
    )
}
