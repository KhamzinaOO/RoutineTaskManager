package com.example.routinetaskmanager.featureReminder.presentation.reminderInfo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.routinetaskmanager.core.presentation.model.UiText
import com.example.routinetaskmanager.core.presentation.model.asString
import com.example.routinetaskmanager.featureReminder.presentation.reminderInfo.model.ReminderInfoEffect
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ReminderInfoRoute(
    reminderId : Long,
    viewModel: ReminderInfoViewModel = koinViewModel(parameters = { parametersOf(reminderId) }),
    showMessage : (String) -> Unit,
    onEditClick : (Long) -> Unit,
    onBackClick : () -> Unit
){
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when(effect){
                is ReminderInfoEffect.EditReminder -> onEditClick(effect.id)
                ReminderInfoEffect.NavigateBack -> onBackClick()
                is ReminderInfoEffect.ShowMessage -> {
                    showMessage(effect.message.asString(context))
                }
            }
        }
    }

    ReminderInfoScreen(
        uiState = uiState,
        onIntent = viewModel::onIntent
    )
}

