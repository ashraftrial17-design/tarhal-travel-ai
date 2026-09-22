package com.tirhal.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.tirhal.ai.ui.navigation.MainAppNavigation
import com.tirhal.ai.ui.theme.TirhalAITheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TirhalAITheme {
                MainAppNavigation()
            }
        }
    }
}
