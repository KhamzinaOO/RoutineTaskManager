package com.example.routinetaskmanager.featureReminder.presentation.all_reminders.model

import com.example.routinetaskmanager.featureReminder.domain.model.Reminder
import com.example.routinetaskmanager.featureReminder.domain.model.ReminderRepeatType

data class AllRemindersUiState(
    val reminders: List<Reminder> = emptyList(),
    val remindersToShow: List<ReminderMiniCardUi> = emptyList(),
    val reminderFilter: ReminderFilter = ReminderFilter(),
    val isLoading: Boolean = false,
    val repeatTypeFilterList  : List<ReminderRepeatTypeUi> = ReminderRepeatType.entries.map {
        it.toRepeatTypeUi()
    } + ReminderRepeatTypeUi(
        id = -1,
        repeatType = null,
        repeatTypeName = "All"
    )
)