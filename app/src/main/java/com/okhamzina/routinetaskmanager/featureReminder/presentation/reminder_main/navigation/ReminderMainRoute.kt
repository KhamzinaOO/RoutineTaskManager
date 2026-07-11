package com.okhamzina.routinetaskmanager.featureReminder.presentation.reminder_main.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.okhamzina.routinetaskmanager.core.presentation.model.asString
import com.okhamzina.routinetaskmanager.featureReminder.presentation.reminder_main.ui.RemindersMainScreen
import com.okhamzina.routinetaskmanager.featureReminder.presentation.reminder_main.model.ReminderMainEffect
import com.okhamzina.routinetaskmanager.featureReminder.presentation.reminder_main.viewModel.ReminderMainViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun ReminderMainRoute(
    viewModel: ReminderMainViewModel = koinViewModel(),
    onTopBarIconClick : () -> Unit,
    onFABClicked : () -> Unit,
    showMessage: (String) -> Unit,
    showActionMessage: (message: String, actionLabel: String, onAction: () -> Unit) -> Unit
){
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit){
        viewModel.effect.collect { effect ->
            when(effect){
                is ReminderMainEffect.OpenDrawer -> {
                    onTopBarIconClick()
                }

                is ReminderMainEffect.FABClicked -> {
                    onFABClicked()
                }

                is ReminderMainEffect.ShowMessage -> {
                    showMessage(effect.message.asString(context))
                }
            }
        }
    }

    RemindersMainScreen(
        uiState = uiState,
        onIntent = viewModel::onIntent,
        showActionMessage = showActionMessage
    )
}
