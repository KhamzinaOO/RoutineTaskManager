package com.example.routinetaskmanager.featureReminder.presentation.create_edit_reminder.model

import com.example.routinetaskmanager.featureReminder.domain.model.NotificationMode
import com.example.routinetaskmanager.core.presentation.model.UiText
import com.example.routinetaskmanager.featureReminder.domain.model.ReminderRepeatType
import com.example.routinetaskmanager.featureReminder.presentation.common.model.DuringSessionPeriodRepeatUi
import com.example.routinetaskmanager.featureReminder.presentation.common.model.OnScheduleCertainRepeatUi
import com.example.routinetaskmanager.featureReminder.presentation.common.model.OnSchedulePeriodRepeatUi

data class CreateEditReminderUiState(
    val id : Long? = null,
    val screenMode: CreateEditReminderMode = CreateEditReminderMode.Create,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val name: String = "",
    val instructions: String = "",
    val repeatType: ReminderRepeatType = ReminderRepeatType.ON_SCHEDULE_PERIOD,
    val onSchedulePeriodState: OnSchedulePeriodRepeatUi = OnSchedulePeriodRepeatUi(),
    val onScheduleCertainState: OnScheduleCertainRepeatUi = OnScheduleCertainRepeatUi(),
    val duringSessionState: DuringSessionPeriodRepeatUi = DuringSessionPeriodRepeatUi(),
    val notificationMode: NotificationMode = NotificationMode.SOUND,
    val images: List<ReminderImageUi> = emptyList(),
    val errorMessage: UiText? = null
)
