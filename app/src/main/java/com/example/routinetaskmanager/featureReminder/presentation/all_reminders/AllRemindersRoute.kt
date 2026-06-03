package com.example.routinetaskmanager.featureReminder.presentation.all_reminders

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.routinetaskmanager.featureReminder.presentation.all_reminders.viewModel.AllRemindersViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun AllRemindersRoute(
    viewModel: AllRemindersViewModel = koinViewModel(),
    onReminderClick : (Int) -> Unit
){
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AllRemindersScreen(
        uiState = uiState
    )
}