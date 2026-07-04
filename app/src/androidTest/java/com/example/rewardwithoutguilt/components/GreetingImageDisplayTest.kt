package com.example.rewardwithoutguilt.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import com.example.rewardwithoutguilt.data.GreetingImage
import org.junit.Rule
import org.junit.Test
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GreetingImageDisplayTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun greetingImageDisplay_whenUriIsNull_showsDefaultImage() {
        val image = GreetingImage(uri = null, size = 100f)
        composeTestRule.setContent {
            GreetingImageDisplay(image = image)
        }
        composeTestRule.onNodeWithContentDescription("Default image").assertIsDisplayed()
    }

    @Test
    fun greetingImageDisplay_whenUriIsNotNull_showsGreetingImage() {
        val image = GreetingImage(uri = "content://media/external/images/media/1", size = 100f)
        composeTestRule.setContent {
            GreetingImageDisplay(image = image)
        }
        composeTestRule.onNodeWithContentDescription("Greeting image").assertIsDisplayed()
    }
}
