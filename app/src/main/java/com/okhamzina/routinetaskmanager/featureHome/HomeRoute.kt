package com.okhamzina.routinetaskmanager.featureHome

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.okhamzina.routinetaskmanager.core.presentation.model.asString
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeRoute(
    viewModel: HomeViewModel = koinViewModel(),
    showMessage: (String) -> Unit,
    showActionMessage: (message: String, actionLabel: String, onAction: () -> Unit) -> Unit,
    onAddReminderClick: () -> Unit,
    onTasksClick: () -> Unit,
    onSettingsClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when(effect){
                is HomeEffect.ShowMessage -> {
                    showMessage(effect.message.asString(context))
                }

                HomeEffect.NavigateCreateReminder -> {
                    onAddReminderClick()
                }

                HomeEffect.NavigateTasks -> {
                    onTasksClick()
                }

                HomeEffect.NavigateToSettings -> onSettingsClick()
            }
        }
    }

    HomeScreen(
        uiState = uiState,
        onIntent = viewModel::onIntent,
        showActionMessage = showActionMessage
    )
}
