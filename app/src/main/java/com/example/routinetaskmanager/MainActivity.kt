package com.example.routinetaskmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.routinetaskmanager.navigation.ui.AppNavigation
import com.example.routinetaskmanager.ui.theme.RoutineTaskManagerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RoutineTaskManagerTheme {
                AppNavigation()
            }
        }
    }
}

