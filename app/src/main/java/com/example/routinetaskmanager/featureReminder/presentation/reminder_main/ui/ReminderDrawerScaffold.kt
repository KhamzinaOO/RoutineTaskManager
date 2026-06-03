package com.example.routinetaskmanager.featureReminder.presentation.reminder_main.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

enum class RemindersDrawerItem {
    Main,
    AllReminders
}

@Composable
fun RemindersDrawerScaffold(
    drawerState : DrawerState,
    scope : CoroutineScope,
    selectedItem: RemindersDrawerItem,
    onMainClick: () -> Unit,
    onAllRemindersClick: () -> Unit,
    content: @Composable () -> Unit
) {
    fun closeDrawerAndNavigate(action: () -> Unit) {
        scope.launch {
            drawerState.close()
            action()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NavigationDrawerItem(
                    label = { Text("Main") },
                    selected = selectedItem == RemindersDrawerItem.Main,
                    onClick = {
                        closeDrawerAndNavigate(onMainClick)
                    }
                )

                NavigationDrawerItem(
                    label = { Text("All Reminders") },
                    selected = selectedItem == RemindersDrawerItem.AllReminders,
                    onClick = {
                        closeDrawerAndNavigate(onAllRemindersClick)
                    }
                )
            }
        }
    ) {
        content()
    }
}