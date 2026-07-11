package com.okhamzina.routinetaskmanager.featureReminder.presentation.all_reminders.model

import com.okhamzina.routinetaskmanager.R
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.Reminder
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.ReminderRepeatType

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
        repeatTypeNameRes = R.string.filter_all
    )
)
