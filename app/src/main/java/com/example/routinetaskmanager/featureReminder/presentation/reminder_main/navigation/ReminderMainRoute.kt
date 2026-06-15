package com.example.routinetaskmanager.featureReminder.presentation.reminder_main.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.routinetaskmanager.featureReminder.presentation.reminder_main.ui.RemindersMainScreen
import com.example.routinetaskmanager.featureReminder.presentation.reminder_main.model.ReminderMainEffect
import com.example.routinetaskmanager.featureReminder.presentation.reminder_main.viewModel.ReminderMainViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun ReminderMainRoute(
    viewModel: ReminderMainViewModel = koinViewModel(),
    onTopBarIconClick : () -> Unit,
    onFABClicked : () -> Unit
){
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit){
        viewModel.effect.collect { effect ->
            when(effect){
                is ReminderMainEffect.OpenDrawer -> {
                    onTopBarIconClick()
                }

                is ReminderMainEffect.FABClicked -> {
                    onFABClicked()
                }
            }
        }
    }

    RemindersMainScreen(
        uiState = uiState,
        onIntent = viewModel::onIntent
    )
}