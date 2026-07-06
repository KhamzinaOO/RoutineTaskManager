package com.example.routinetaskmanager.featureReminder.presentation.create_edit_reminder.model

import android.net.Uri
import com.example.routinetaskmanager.featureReminder.domain.model.NotificationMode
import com.example.routinetaskmanager.featureReminder.domain.model.ReminderRepeatType
import com.example.routinetaskmanager.featureReminder.presentation.common.model.DuringSessionPeriodRepeatUi
import com.example.routinetaskmanager.featureReminder.presentation.common.model.OnScheduleCertainRepeatUi
import com.example.routinetaskmanager.featureReminder.presentation.common.model.OnSchedulePeriodRepeatUi

sealed interface CreateEditReminderIntent {

    data class NameChanged(
        val value: String
    ) : CreateEditReminderIntent

    data class InstructionsChanged(
        val value: String
    ) : CreateEditReminderIntent

    data class RepeatTypeChanged(
        val value: ReminderRepeatType
    ) : CreateEditReminderIntent

    data class OnSchedulePeriodStateChanged(
        val value: OnSchedulePeriodRepeatUi
    ) : CreateEditReminderIntent

    data class OnScheduleCertainStateChanged(
        val value: OnScheduleCertainRepeatUi
    ) : CreateEditReminderIntent

    data class DuringSessionStateChanged(
        val value: DuringSessionPeriodRepeatUi
    ) : CreateEditReminderIntent

    data class NotificationModeChanged(
        val value: NotificationMode
    ) : CreateEditReminderIntent

    data class ImageAdded(
        val path: String
    ) : CreateEditReminderIntent

    data class ImageRemoved(
        val key: String
    ) : CreateEditReminderIntent

    data object TakePictureClicked : CreateEditReminderIntent

    data object SaveClicked : CreateEditReminderIntent

    data object NotificationPermissionGranted : CreateEditReminderIntent

    data object NotificationPermissionDenied : CreateEditReminderIntent

    data object BackClicked : CreateEditReminderIntent

    data object ErrorShown : CreateEditReminderIntent
}
