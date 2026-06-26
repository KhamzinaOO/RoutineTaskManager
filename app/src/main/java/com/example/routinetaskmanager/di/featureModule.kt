package com.example.routinetaskmanager.di

import com.example.routinetaskmanager.data.storage.ImageStorage
import com.example.routinetaskmanager.featureHome.HomeViewModel
import com.example.routinetaskmanager.featureReminder.application.session.RestoreActiveWorkSessionRuntimeUseCase
import com.example.routinetaskmanager.featureReminder.application.session.ToggleWorkSessionUseCase
import com.example.routinetaskmanager.featureReminder.application.session.WorkSessionRuntimeController
import com.example.routinetaskmanager.featureReminder.data.repository.ReminderRepositoryImpl
import com.example.routinetaskmanager.featureReminder.data.session.AndroidWorkSessionRuntimeController
import com.example.routinetaskmanager.featureReminder.domain.model.schedule.ReminderScheduleCalculator
import com.example.routinetaskmanager.featureReminder.domain.repository.ReminderRepository
import com.example.routinetaskmanager.featureReminder.application.schedule.ObserveDayReminderOccurrencesUseCase
import com.example.routinetaskmanager.featureReminder.application.schedule.ObserveReminderScheduleUseCase
import com.example.routinetaskmanager.featureReminder.application.session.ObserveWorkSessionStateUseCase
import com.example.routinetaskmanager.featureReminder.application.command.ReminderCommandUseCase
import com.example.routinetaskmanager.featureReminder.application.notifications.ReminderSessionNotificationUseCase
import com.example.routinetaskmanager.featureReminder.application.notifications.RescheduleRemindersUseCase
import com.example.routinetaskmanager.featureReminder.application.schedule.ObserveNextReminderOccurrenceByIdUseCase
import com.example.routinetaskmanager.featureReminder.application.schedule.ObserveReminderOccurrenceUseCase
import com.example.routinetaskmanager.featureReminder.application.session.WorkSessionManager
import com.example.routinetaskmanager.featureReminder.presentation.all_reminders.viewModel.AllRemindersViewModel
import com.example.routinetaskmanager.featureReminder.presentation.create_edit_reminder.viewModel.CreateEditReminderViewModel
import com.example.routinetaskmanager.featureReminder.presentation.reminderInfo.ReminderInfoViewModel
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

    viewModel{ (id: Long) ->
        ReminderInfoViewModel(
            reminderId = id,
            commandUseCase = get(),
            observeNextReminderOccurrenceById = get()
        )
    }

    single {
        ReminderScheduleCalculator()
    }

    factory {
        ObserveNextReminderOccurrenceByIdUseCase(
            dispatcherProvider = get(),
            workSessionManager = get(),
            observeReminderOccurrenceUseCase = get()
        )
    }

    factory {
        ObserveReminderOccurrenceUseCase(
            dispatcherProvider = get(),
            reminderRepository = get(),
            scheduleCalculator = get()
        )
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
            scheduledNotificationDao = get(),
            dispatcherProvider = get()
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
            workSessionManager = get(),
            dispatcherProvider = get()
        )
    }

    single<WorkSessionRuntimeController> {
        AndroidWorkSessionRuntimeController(
            foregroundController = get()
        )
    }

    factory {
        ToggleWorkSessionUseCase(
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
