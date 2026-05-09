package com.example.routinetaskmanager.navigation.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun CommonFloatingButton(
    onClick :() -> Unit
){
    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.primaryContainer
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "add",
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}
