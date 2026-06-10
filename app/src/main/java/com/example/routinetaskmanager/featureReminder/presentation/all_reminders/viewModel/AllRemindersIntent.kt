package com.example.routinetaskmanager.featureReminder.presentation.all_reminders.viewModel

import com.example.routinetaskmanager.featureReminder.presentation.all_reminders.model.ReminderFilter

sealed interface AllRemindersIntent {
    data class ShowMessage (val message : String) : AllRemindersIntent
    data object OnMenuButtonClick : AllRemindersIntent
    data class OnItemClick (val id : Long) : AllRemindersIntent
    data object OnAddFABClick : AllRemindersIntent

    data class OnEditClick (val id: Long) : AllRemindersIntent

    data class OnDeleteClick (val id : Long) : AllRemindersIntent

    data class OnOpenClick (val id : Long) : AllRemindersIntent

    data class FilterReminder (val filter : ReminderFilter) : AllRemindersIntent

    data class OnTypeSelected (val typeIndex : Int) : AllRemindersIntent
}