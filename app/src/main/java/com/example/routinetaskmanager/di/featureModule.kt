package com.example.routinetaskmanager.di

import com.example.routinetaskmanager.data.storage.ImageStorage
import com.example.routinetaskmanager.featureHome.HomeViewModel
import com.example.routinetaskmanager.featureReminder.data.repository.ReminderRepositoryImpl
import com.example.routinetaskmanager.featureReminder.domain.model.schedule.ReminderScheduleCalculator
import com.example.routinetaskmanager.featureReminder.domain.repository.ReminderRepository
import com.example.routinetaskmanager.featureReminder.domain.useCase.ObserveReminderScheduleUseCase
import com.example.routinetaskmanager.featureReminder.domain.useCase.RescheduleRemindersUseCase
import com.example.routinetaskmanager.featureReminder.presentation.all_reminders.viewModel.AllRemindersViewModel
import com.example.routinetaskmanager.featureReminder.presentation.create_reminder.viewModel.CreateReminderViewModel
import com.example.routinetaskmanager.featureReminder.presentation.reminder_main.viewModel.ReminderMainViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val featureHomeModule = module {
    viewModelOf(::HomeViewModel)
}

val featureReminderModule = module {
    viewModelOf(::CreateReminderViewModel)
    viewModelOf(::ReminderMainViewModel)
    viewModelOf(::AllRemindersViewModel)

    single {
        ReminderScheduleCalculator()
    }

    factory {
        ObserveReminderScheduleUseCase(
            reminderRepository = get(),
            scheduleCalculator = get()
        )
    }

    factory {
        RescheduleRemindersUseCase(
            reminderRepository = get(),
            scheduleCalculator = get(),
            alarmScheduler = get(),
            scheduledNotificationDao = get()
        )
    }

    single {
        ImageStorage(
            context = androidContext()
        )
    }

    single<ReminderRepository> {
        ReminderRepositoryImpl(
            reminderDao = get(),
            imageStorage = get()
        )
    }
}