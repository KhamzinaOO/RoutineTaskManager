package com.example.routinetaskmanager.featureReminder.presentation.create_edit_reminder.navigation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.routinetaskmanager.featureReminder.presentation.create_edit_reminder.ui.CreateReminderScreen
import com.example.routinetaskmanager.featureReminder.presentation.create_edit_reminder.model.CreateEditReminderEffect
import com.example.routinetaskmanager.featureReminder.presentation.create_edit_reminder.model.CreateEditReminderIntent
import com.example.routinetaskmanager.featureReminder.presentation.create_edit_reminder.viewModel.EditReminderViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun EditReminderRoute(
    id : Long,
    viewModel : EditReminderViewModel = koinViewModel(parameters = { parametersOf(id) }),
    showMessage : (String) -> Unit,
    onBackClick : () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.onIntent(
                CreateEditReminderIntent.ImageAdded(uri)
            )
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                CreateEditReminderEffect.NavigateBack -> {
                    onBackClick()
                }

                CreateEditReminderEffect.OpenImagePicker -> {
                    imagePickerLauncher.launch(
                        PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                }


                is CreateEditReminderEffect.ShowMessage -> {
                    showMessage(effect.message)
                }
            }
        }
    }

    CreateReminderScreen(
    uiState = uiState,
    onIntent = viewModel::onIntent
    )
}