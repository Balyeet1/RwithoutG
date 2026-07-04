package com.example.rewardwithoutguilt

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.example.rewardwithoutguilt.data.FocusPreferences
import com.example.rewardwithoutguilt.features.focus.FocusService
import com.example.rewardwithoutguilt.navigation.SetupNavGraph
import com.example.rewardwithoutguilt.ui.theme.RewardWithoutGuiltTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Start focus service if enabled
        lifecycleScope.launch {
            val focusPrefs = FocusPreferences(this@MainActivity)
            if (focusPrefs.isFocusEnabled.first()) {
                val intent = Intent(this@MainActivity, FocusService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(intent)
                } else {
                    startService(intent)
                }
            }
        }

        setContent {
            RewardWithoutGuiltTheme {
                val navController = rememberNavController()
                SetupNavGraph(navController = navController)
            }
        }
    }
}
