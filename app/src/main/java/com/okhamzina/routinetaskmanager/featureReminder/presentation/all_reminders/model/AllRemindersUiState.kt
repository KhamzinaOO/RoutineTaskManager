package com.okhamzina.routinetaskmanager.featureReminder.presentation.all_reminders.model

import com.okhamzina.routinetaskmanager.R
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.Reminder
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.ReminderRepeatType
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.type

data class AllRemindersUiState(
    val reminders: List<Reminder> = emptyList(),
    val reminderFilter: ReminderFilter = ReminderFilter(),
    val isLoading: Boolean = false,
    val repeatTypeFilterList  : List<ReminderRepeatTypeUi> = ReminderRepeatType.entries.map {
        it.toRepeatTypeUi()
    } + ReminderRepeatTypeUi(
        id = -1,
        repeatType = null,
        repeatTypeNameRes = R.string.filter_all
    )
) {
    val visibleReminders: List<Reminder>
        get() = reminders.filter { reminder ->
            val typeMatches = reminderFilter.repeatType == null ||
                    reminder.repeatRule.type == reminderFilter.repeatType
            val searchMatches = reminderFilter.searchText.isBlank() ||
                    reminder.name.contains(reminderFilter.searchText, ignoreCase = true)

            typeMatches && searchMatches
        }
}
