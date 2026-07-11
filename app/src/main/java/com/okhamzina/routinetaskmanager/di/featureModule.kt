package com.okhamzina.routinetaskmanager.di

import com.okhamzina.routinetaskmanager.data.storage.ImageStorage
import com.okhamzina.routinetaskmanager.featureHome.HomeViewModel
import com.okhamzina.routinetaskmanager.featureReminder.application.session.RestoreActiveWorkSessionRuntimeUseCase
import com.okhamzina.routinetaskmanager.featureReminder.application.session.ToggleWorkSessionUseCase
import com.okhamzina.routinetaskmanager.featureReminder.application.session.WorkSessionRuntimeController
import com.okhamzina.routinetaskmanager.featureReminder.application.session.WorkSessionStateStore
import com.okhamzina.routinetaskmanager.featureReminder.data.repository.ReminderOccurrenceRepositoryImpl
import com.okhamzina.routinetaskmanager.featureReminder.data.repository.ReminderRepositoryImpl
import com.okhamzina.routinetaskmanager.featureReminder.data.session.AndroidWorkSessionRuntimeController
import com.okhamzina.routinetaskmanager.featureReminder.data.session.SharedPrefsWorkSessionStateStore
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.schedule.ReminderScheduleCalculator
import com.okhamzina.routinetaskmanager.featureReminder.domain.repository.ReminderOccurrenceRepository
import com.okhamzina.routinetaskmanager.featureReminder.domain.repository.ReminderRepository
import com.okhamzina.routinetaskmanager.featureReminder.application.schedule.ObserveDayReminderOccurrencesUseCase
import com.okhamzina.routinetaskmanager.featureReminder.application.schedule.ObserveReminderScheduleUseCase
import com.okhamzina.routinetaskmanager.featureReminder.application.session.ObserveWorkSessionStateUseCase
import com.okhamzina.routinetaskmanager.featureReminder.application.command.ReminderCommandUseCase
import com.okhamzina.routinetaskmanager.featureReminder.application.notifications.ReminderSessionNotificationUseCase
import com.okhamzina.routinetaskmanager.featureReminder.application.notifications.RescheduleRemindersUseCase
import com.okhamzina.routinetaskmanager.featureReminder.application.schedule.ObserveNextReminderOccurrenceUseCase
import com.okhamzina.routinetaskmanager.featureReminder.application.schedule.ObserveNextReminderOccurrenceByIdUseCase
import com.okhamzina.routinetaskmanager.featureReminder.application.schedule.ObserveReminderOccurrenceUseCase
import com.okhamzina.routinetaskmanager.featureReminder.application.session.WorkSessionManager
import com.okhamzina.routinetaskmanager.featureReminder.presentation.all_reminders.viewModel.AllRemindersViewModel
import com.okhamzina.routinetaskmanager.featureReminder.presentation.create_edit_reminder.viewModel.CreateEditReminderViewModel
import com.okhamzina.routinetaskmanager.featureReminder.presentation.reminderInfo.ReminderInfoViewModel
import com.okhamzina.routinetaskmanager.featureReminder.presentation.reminder_main.viewModel.ReminderMainViewModel
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
            id = id,
            errorReporter = get()
        )
    }

    viewModel{ (id: Long) ->
        ReminderInfoViewModel(
            reminderId = id,
            commandUseCase = get(),
            observeNextReminderOccurrenceById = get(),
            dateTimeTicker = get(),
            errorReporter = get()
        )
    }

    single {
        ReminderScheduleCalculator()
    }

    factory {
        ObserveNextReminderOccurrenceUseCase(
            dispatcherProvider = get(),
            workSessionManager = get(),
            observeReminderScheduleUseCase = get()
        )
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
            scheduleCalculator = get(),
            occurrenceRepository = get()
        )
    }

    factory {
        ObserveReminderScheduleUseCase(
            dispatcherProvider = get(),
            reminderRepository = get(),
            scheduleCalculator = get(),
            occurrenceRepository = get()
        )
    }

    factory {
        RescheduleRemindersUseCase(
            reminderRepository = get(),
            scheduleCalculator = get(),
            alarmScheduler = get(),
            scheduledNotificationRepository = get(),
            reminderOccurrenceRepository = get(),
            dispatcherProvider = get()
        )
    }

    factory {
        ReminderSessionNotificationUseCase(
            reminderRepository = get(),
            alarmScheduler = get(),
            scheduledNotificationRepository = get(),
            dispatcherProvider = get()
        )
    }

    single {
        SharedPrefsWorkSessionStateStore(
            context = androidContext()
        )
    }

    single<WorkSessionStateStore> {
        get<SharedPrefsWorkSessionStateStore>()
    }

    single {
        WorkSessionManager(
            stateStore = get(),
            reminderSessionNotificationUseCase = get()
        )
    }

    factory {
        ReminderCommandUseCase(
            reminderRepository = get(),
            rescheduleRemindersUseCase = get(),
            dispatcherProvider = get(),
            scheduleCalculator = get(),
            occurrenceRepository = get(),
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

    single<ReminderOccurrenceRepository> {
        ReminderOccurrenceRepositoryImpl(
            occurrenceDao = get()
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
            workSessionRuntimeController = get(),
            errorReporter = get()
        )
    }

    factory {
        RestoreActiveWorkSessionRuntimeUseCase(
            workSessionManager = get(),
            runtimeController = get(),
            errorReporter = get()
        )
    }
}
