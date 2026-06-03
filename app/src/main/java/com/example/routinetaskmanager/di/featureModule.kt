package com.example.routinetaskmanager.di

import com.example.routinetaskmanager.featureHome.HomeViewModel
import com.example.routinetaskmanager.featureReminder.presentation.all_reminders.viewModel.AllRemindersViewModel
import com.example.routinetaskmanager.featureReminder.presentation.create_reminder.viewModel.CreateReminderViewModel
import com.example.routinetaskmanager.featureReminder.presentation.reminder_main.viewModel.ReminderMainViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val featureHomeModule = module {
    viewModelOf(::HomeViewModel)
}

val featureReminderModule = module {
    viewModelOf(::CreateReminderViewModel)
    viewModelOf(::ReminderMainViewModel)
    viewModelOf(::AllRemindersViewModel)
}