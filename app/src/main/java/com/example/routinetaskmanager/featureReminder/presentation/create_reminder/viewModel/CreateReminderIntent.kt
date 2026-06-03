package com.example.routinetaskmanager.featureReminder.presentation.create_reminder.viewModel

import android.net.Uri
import com.example.routinetaskmanager.featureReminder.domain.model.NotificationMode
import com.example.routinetaskmanager.featureReminder.domain.model.ReminderRepeatType
import com.example.routinetaskmanager.featureReminder.presentation.common.model.AfterAnotherRepeatUi
import com.example.routinetaskmanager.featureReminder.presentation.common.model.DuringSessionPeriodRepeatUi
import com.example.routinetaskmanager.featureReminder.presentation.common.model.OnScheduleCertainRepeatUi
import com.example.routinetaskmanager.featureReminder.presentation.common.model.OnSchedulePeriodRepeatUi

sealed interface CreateReminderIntent {

    data class NameChanged(
        val value: String
    ) : CreateReminderIntent

    data class InstructionsChanged(
        val value: String
    ) : CreateReminderIntent

    data class RepeatTypeChanged(
        val value: ReminderRepeatType
    ) : CreateReminderIntent

    data class AfterAnotherStateChanged(
        val value: AfterAnotherRepeatUi
    ) : CreateReminderIntent

    data class OnSchedulePeriodStateChanged(
        val value: OnSchedulePeriodRepeatUi
    ) : CreateReminderIntent

    data class OnScheduleCertainStateChanged(
        val value: OnScheduleCertainRepeatUi
    ) : CreateReminderIntent

    data class DuringSessionStateChanged(
        val value: DuringSessionPeriodRepeatUi
    ) : CreateReminderIntent

    data class NotificationModeChanged(
        val value: NotificationMode
    ) : CreateReminderIntent

    data class ImageAdded(
        val uri: Uri
    ) : CreateReminderIntent

    data class ImageRemoved(
        val uri: Uri
    ) : CreateReminderIntent

    data object TakePictureClicked : CreateReminderIntent

    data object SaveClicked : CreateReminderIntent

    data object BackClicked : CreateReminderIntent

    data object ErrorShown : CreateReminderIntent
}