package com.example.routinetaskmanager.featureReminder.presentation.all_reminders

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.routinetaskmanager.featureReminder.presentation.all_reminders.model.AllRemindersEffect
import com.example.routinetaskmanager.featureReminder.presentation.all_reminders.viewModel.AllRemindersViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun AllRemindersRoute(
    viewModel: AllRemindersViewModel = koinViewModel(),
    onMenuClick : () -> Unit,
    onFABClicked : () -> Unit,
    onReminderClick : (Long) -> Unit,
    onEditClick : (Long) -> Unit,
    showSnackBar : (String) -> Unit
){
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when(effect){
                AllRemindersEffect.FABClicked -> {
                    onFABClicked()
                }
                is AllRemindersEffect.ItemClicked -> {
                    onReminderClick(effect.id)
                }
                AllRemindersEffect.MenuButtonClicked -> {
                    onMenuClick()
                }

                is AllRemindersEffect.EditClicked -> {
                    onEditClick(effect.id)
                }
                is AllRemindersEffect.OpenClicked -> {
                    onReminderClick(effect.id)
                }
                is AllRemindersEffect.ShowMessage -> {
                    showSnackBar(effect.message)
                }
            }
        }
    }

    AllRemindersScreen(
        uiState = uiState,
        onIntent = viewModel::onIntent
    )
}