package com.example.routinetaskmanager.featureReminder.presentation.all_reminders.viewModel

import com.example.routinetaskmanager.featureReminder.domain.model.Reminder
import com.example.routinetaskmanager.featureReminder.domain.model.ReminderRepeatType
import com.example.routinetaskmanager.featureReminder.presentation.all_reminders.model.ReminderFilter
import com.example.routinetaskmanager.featureReminder.presentation.all_reminders.model.ReminderRepeatTypeUi
import com.example.routinetaskmanager.featureReminder.presentation.all_reminders.model.toRepeatTypeUi

data class AllRemindersUiState(
    val reminders: List<Reminder> = emptyList(),
    val remindersToShow: List<Reminder> = emptyList(),
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