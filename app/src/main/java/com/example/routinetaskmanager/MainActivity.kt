package com.example.routinetaskmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.routinetaskmanager.core.ui.WeekCarousel
import com.example.routinetaskmanager.ui.theme.RoutineTaskManagerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RoutineTaskManagerTheme {
//                ButtonTestScreen()
                Box(
                    modifier = Modifier.systemBarsPadding()
                ) {
                    WeekCarousel(modifier = Modifier.padding(horizontal = 15.dp))
                }
            }
        }
    }
}

