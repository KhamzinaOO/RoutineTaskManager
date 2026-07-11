package com.okhamzina.routinetaskmanager.featureReminder.presentation.create_edit_reminder

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.okhamzina.routinetaskmanager.core.presentation.model.asString
import com.okhamzina.routinetaskmanager.core.presentation.ui.rememberExactAlarmAccessRequest
import com.okhamzina.routinetaskmanager.core.presentation.ui.rememberNotificationPermissionRequest
import com.okhamzina.routinetaskmanager.featureReminder.presentation.create_edit_reminder.model.CreateEditReminderEffect
import com.okhamzina.routinetaskmanager.featureReminder.presentation.create_edit_reminder.model.CreateEditReminderIntent
import com.okhamzina.routinetaskmanager.featureReminder.presentation.create_edit_reminder.ui.CreateReminderScreen
import com.okhamzina.routinetaskmanager.featureReminder.presentation.create_edit_reminder.viewModel.CreateEditReminderViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun CreateEditReminderRoute(
    id : Long?,
    viewModel : CreateEditReminderViewModel = koinViewModel(parameters = { parametersOf(id) }),
    showMessage : (String) -> Unit,
    onBackClick : () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val requestExactAlarmAccess = rememberExactAlarmAccessRequest(
        onGranted = {
            viewModel.onIntent(CreateEditReminderIntent.NotificationPermissionGranted)
        },
        onDenied = {
            viewModel.onIntent(CreateEditReminderIntent.ExactAlarmPermissionDenied)
        }
    )

    val requestNotificationPermission = rememberNotificationPermissionRequest(
        onGranted = {
            requestExactAlarmAccess()
        },
        onDenied = {
            viewModel.onIntent(CreateEditReminderIntent.NotificationPermissionDenied)
        }
    )

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        uris.forEach { uri ->
            viewModel.onIntent(
                CreateEditReminderIntent.ImageAdded(uri.toString())
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

                CreateEditReminderEffect.RequestNotificationPermission -> {
                    requestNotificationPermission()
                }

                is CreateEditReminderEffect.ShowMessage -> {
                    showMessage(effect.message.asString(context))
                }
            }
        }
    }

    CreateReminderScreen(
        uiState = uiState,
        onIntent = viewModel::onIntent
    )
}
