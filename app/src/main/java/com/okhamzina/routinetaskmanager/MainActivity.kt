package com.okhamzina.routinetaskmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.okhamzina.routinetaskmanager.navigation.ui.AppNavigation
import com.okhamzina.routinetaskmanager.ui.theme.RoutineTaskManagerTheme

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

