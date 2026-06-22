package com.example.routinetaskmanager.di

import com.example.routinetaskmanager.data.storage.ImageStorage
import com.example.routinetaskmanager.featureHome.HomeViewModel
import com.example.routinetaskmanager.featureReminder.application.session.RestoreActiveWorkSessionRuntimeUseCase
import com.example.routinetaskmanager.featureReminder.application.session.ToggleWorkSessionUseCase
import com.example.routinetaskmanager.featureReminder.data.session.WorkSessionRuntimeController
import com.example.routinetaskmanager.featureReminder.data.repository.ReminderRepositoryImpl
import com.example.routinetaskmanager.featureReminder.data.session.AndroidWorkSessionRuntimeController
import com.example.routinetaskmanager.featureReminder.domain.model.schedule.ReminderScheduleCalculator
import com.example.routinetaskmanager.featureReminder.domain.repository.ReminderRepository
import com.example.routinetaskmanager.featureReminder.domain.useCase.ObserveDayReminderOccurrencesUseCase
import com.example.routinetaskmanager.featureReminder.domain.useCase.ObserveReminderScheduleUseCase
import com.example.routinetaskmanager.featureReminder.domain.useCase.ObserveWorkSessionStateUseCase
import com.example.routinetaskmanager.featureReminder.domain.useCase.ReminderCommandUseCase
import com.example.routinetaskmanager.featureReminder.application.notifications.ReminderSessionNotificationUseCase
import com.example.routinetaskmanager.featureReminder.application.notifications.RescheduleRemindersUseCase
import com.example.routinetaskmanager.featureReminder.application.session.WorkSessionManager
import com.example.routinetaskmanager.featureReminder.presentation.all_reminders.viewModel.AllRemindersViewModel
import com.example.routinetaskmanager.featureReminder.presentation.create_edit_reminder.viewModel.CreateEditReminderViewModel
import com.example.routinetaskmanager.featureReminder.presentation.reminder_main.viewModel.ReminderMainViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val featureHomeModule = module {
    viewModelOf(::HomeViewModel)
}

val featureReminderModule = module {
    viewModelOf(::ReminderMainViewModel)
    viewModelOf(::AllRemindersViewModel)
    viewModel { (id : Long?)->
        CreateEditReminderViewModel(
            commandUseCase = get(),
            id = id
        )
    }

    single {
        ReminderScheduleCalculator()
    }

    factory {
        ObserveReminderScheduleUseCase(
            dispatcherProvider = get(),
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

    factory {
        ReminderSessionNotificationUseCase(
            reminderRepository = get(),
            alarmScheduler = get(),
            scheduledNotificationDao = get()
        )
    }

    single {
        WorkSessionManager(
            context = androidContext(),
            reminderSessionNotificationUseCase = get()
        )
    }

    factory {
        ReminderCommandUseCase(
            reminderRepository = get(),
            rescheduleRemindersUseCase = get(),
            workSessionManager = get(),
            dispatcherProvider = get()
        )
    }

    single {
        ImageStorage(
            context = androidContext()
        )
    }

    single<ReminderRepository> {
        ReminderRepositoryImpl(
            database = get(),
            reminderDao = get(),
            imageStorage = get()
        )
    }

    factory {
        ObserveWorkSessionStateUseCase(
            workSessionManager = get()
        )
    }

    factory {
        ObserveDayReminderOccurrencesUseCase(
            observeReminderScheduleUseCase = get(),
            workSessionManager = get()
        )
    }

    single<WorkSessionRuntimeController> {
        AndroidWorkSessionRuntimeController(
            foregroundController = get()
        )
    }

    factory {
        ToggleWorkSessionUseCase(
            reminderCommandUseCase = get(),
            workSessionManager = get(),
            workSessionRuntimeController = get()
        )
    }

    factory {
        RestoreActiveWorkSessionRuntimeUseCase(
            workSessionManager = get(),
            runtimeController = get()
        )
    }
}
