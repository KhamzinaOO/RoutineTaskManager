package com.example.routinetaskmanager.featureReminder.presentation.create_reminder.navigation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.routinetaskmanager.featureReminder.presentation.create_reminder.viewModel.CreateReminderEffect
import com.example.routinetaskmanager.featureReminder.presentation.create_reminder.viewModel.CreateReminderIntent
import com.example.routinetaskmanager.featureReminder.presentation.create_reminder.viewModel.CreateReminderViewModel
import com.example.routinetaskmanager.featureReminder.presentation.create_reminder.ui.CreateReminderScreen
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CreateReminderRoute(
    viewModel : CreateReminderViewModel = koinViewModel(),
    showMessage : (String) -> Unit,
    onBackClick : () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.onIntent(
                CreateReminderIntent.ImageAdded(uri)
            )
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                CreateReminderEffect.NavigateBack -> {
                    onBackClick()
                }

                CreateReminderEffect.OpenImagePicker -> {
                    imagePickerLauncher.launch(
                        PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                }


                is CreateReminderEffect.ShowMessage -> {
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