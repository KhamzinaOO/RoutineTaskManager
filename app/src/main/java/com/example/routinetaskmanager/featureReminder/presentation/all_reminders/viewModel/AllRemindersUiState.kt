package com.example.routinetaskmanager.featureReminder.presentation.all_reminders.viewModel

import com.example.routinetaskmanager.featureReminder.domain.model.Reminder

data class AllRemindersUiState(
    val reminders : List<Reminder> = emptyList()
)