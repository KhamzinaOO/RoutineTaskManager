package com.okhamzina.routinetaskmanager.featureReminder.presentation.all_reminders

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.okhamzina.routinetaskmanager.core.presentation.model.asString
import com.okhamzina.routinetaskmanager.featureReminder.presentation.all_reminders.model.AllRemindersEffect
import com.okhamzina.routinetaskmanager.featureReminder.presentation.all_reminders.viewModel.AllRemindersViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun AllRemindersRoute(
    viewModel: AllRemindersViewModel = koinViewModel(),
    onMenuClick : () -> Unit,
    onAddReminderClick : () -> Unit,
    onSearchClick: () -> Unit = {},
    onReminderClick : (Long) -> Unit,
    onEditClick : (Long) -> Unit,
    showSnackBar : (String) -> Unit
){
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when(effect){
                AllRemindersEffect.NavigateToCreateReminder -> {
                    onAddReminderClick()
                }
                is AllRemindersEffect.NavigateToReminder -> {
                    onReminderClick(effect.id)
                }
                AllRemindersEffect.OpenDrawer -> {
                    onMenuClick()
                }

                AllRemindersEffect.OpenSearch -> onSearchClick()

                is AllRemindersEffect.NavigateToEditReminder -> {
                    onEditClick(effect.id)
                }
                is AllRemindersEffect.ShowMessage -> {
                    showSnackBar(effect.message.asString(context))
                }
            }
        }
    }

    AllRemindersScreen(
        uiState = uiState,
        onIntent = viewModel::onIntent
    )
}
