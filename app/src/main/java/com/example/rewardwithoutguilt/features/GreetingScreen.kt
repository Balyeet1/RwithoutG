package com.example.rewardwithoutguilt.features

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.rewardwithoutguilt.BuildConfig
import com.example.rewardwithoutguilt.components.GreetingImageDisplay
import com.example.rewardwithoutguilt.data.GreetingImage
import com.example.rewardwithoutguilt.data.GreetingPreferences
import com.example.rewardwithoutguilt.ui.theme.RewardWithoutGuiltTheme
import com.example.rewardwithoutguilt.util.Constants
import kotlinx.coroutines.delay

@Composable
fun GreetingScreen(
    modifier: Modifier = Modifier,
    overrideText: String? = null,
    overrideImage: GreetingImage? = null,
    overrideImageVersion: Long = 0L,
    onTimeout: () -> Unit = {}
) {
    val context = LocalContext.current
    val prefs = remember { GreetingPreferences(context) }
    val savedText by prefs.greetingText.collectAsState(initial = null)
    val savedImage by prefs.greetingImage.collectAsState(initial = GreetingImage())

    val greetingText = overrideText ?: savedText
    val greetingImage = overrideImage ?: savedImage

    val isDebug = BuildConfig.FLAVOR == "dev" && BuildConfig.BUILD_TYPE == "debug"

    LaunchedEffect(Unit) {
        delay(if(isDebug) 1000 else Constants.GREETING_TIMEOUT_MS)
        onTimeout()
    }

    if (greetingText == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = greetingText,
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(25.dp))
            GreetingImageDisplay(
                image = greetingImage,
                version = overrideImageVersion
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingScreenPreview() {
    RewardWithoutGuiltTheme {
        GreetingScreen()
    }
}
