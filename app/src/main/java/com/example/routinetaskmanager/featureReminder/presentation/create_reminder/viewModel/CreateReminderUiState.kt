package com.example.routinetaskmanager.featureReminder.presentation.create_reminder.viewModel

import android.net.Uri
import com.example.routinetaskmanager.featureReminder.domain.model.NotificationMode
import com.example.routinetaskmanager.featureReminder.domain.model.ReminderRepeatType
import com.example.routinetaskmanager.featureReminder.presentation.common.model.AfterAnotherRepeatUi
import com.example.routinetaskmanager.featureReminder.presentation.common.model.DuringSessionPeriodRepeatUi
import com.example.routinetaskmanager.featureReminder.presentation.common.model.OnScheduleCertainRepeatUi
import com.example.routinetaskmanager.featureReminder.presentation.common.model.OnSchedulePeriodRepeatUi

data class CreateReminderUiState(
    val name: String = "",
    val instructions: String = "",

    val repeatType: ReminderRepeatType = ReminderRepeatType.ON_SCHEDULE_PERIOD,

    val afterAnotherState: AfterAnotherRepeatUi = AfterAnotherRepeatUi(),
    val onSchedulePeriodState: OnSchedulePeriodRepeatUi = OnSchedulePeriodRepeatUi(),
    val onScheduleCertainState: OnScheduleCertainRepeatUi = OnScheduleCertainRepeatUi(),
    val duringSessionState: DuringSessionPeriodRepeatUi = DuringSessionPeriodRepeatUi(),

    val notificationMode: NotificationMode = NotificationMode.SOUND,

    val imageUris: List<Uri> = emptyList(),

    val isSaving: Boolean = false,
    val errorMessage: String? = null
)