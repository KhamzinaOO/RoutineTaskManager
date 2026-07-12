package com.okhamzina.routinetaskmanager.featureReminder.presentation.all_reminders.model

sealed interface AllRemindersIntent {
    data object MenuButtonClicked : AllRemindersIntent
    data object SearchButtonClicked : AllRemindersIntent
    data class ReminderClicked(val id: Long) : AllRemindersIntent
    data object AddReminderClicked : AllRemindersIntent
    data class EditReminderClicked(val id: Long) : AllRemindersIntent
    data class DeleteReminderClicked(val id: Long) : AllRemindersIntent
    data class FilterChanged(val filter: ReminderFilter) : AllRemindersIntent
    data class TypeFilterSelected(val typeId: Int) : AllRemindersIntent
}
