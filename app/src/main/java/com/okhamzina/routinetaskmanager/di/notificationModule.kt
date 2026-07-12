package com.okhamzina.routinetaskmanager.di

import com.okhamzina.routinetaskmanager.core.notifications.android.AndroidAppAlarmScheduler
import com.okhamzina.routinetaskmanager.core.notifications.api.AppAlarmScheduler
import com.okhamzina.routinetaskmanager.core.notifications.android.AppNotificationChannels
import com.okhamzina.routinetaskmanager.core.notifications.android.AppNotificationFactory
import com.okhamzina.routinetaskmanager.core.notifications.android.AppNotificationManager
import com.okhamzina.routinetaskmanager.core.notifications.android.AppNotificationRuntimeAccessChecker
import com.okhamzina.routinetaskmanager.core.notifications.NotificationTriggerRouter
import com.okhamzina.routinetaskmanager.featureReminder.application.notifications.ReminderNotificationTriggerHandler
import com.okhamzina.routinetaskmanager.core.notifications.RescheduleAllNotificationsUseCase
import com.okhamzina.routinetaskmanager.core.notifications.ExactAlarmAccessViewModel
import com.okhamzina.routinetaskmanager.core.notifications.data.SharedPrefsExactAlarmAccessRepository
import com.okhamzina.routinetaskmanager.core.notifications.domain.ExactAlarmAccessRepository
import com.okhamzina.routinetaskmanager.featureTask.TaskNotificationTriggerHandler
import com.okhamzina.routinetaskmanager.featureReminder.data.session.WorkSessionForegroundController
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import org.koin.core.module.dsl.viewModelOf

val notificationModule = module {

    single {
        AppNotificationChannels(
            context = androidContext()
        )
    }

    single {
        AppNotificationFactory(
            context = androidContext()
        )
    }

    single {
        AppNotificationRuntimeAccessChecker(
            context = androidContext()
        )
    }

    single<ExactAlarmAccessRepository> {
        SharedPrefsExactAlarmAccessRepository(
            context = androidContext(),
            accessChecker = get()
        )
    }

    single {
        AppNotificationManager(
            context = androidContext(),
            notificationFactory = get(),
            permissionChecker = get()
        )
    }

    single<AppAlarmScheduler> {
        AndroidAppAlarmScheduler(
            context = androidContext(),
            permissionChecker = get()
        )
    }

    single {
        WorkSessionForegroundController(
            context = androidContext()
        )
    }

    single {
        ReminderNotificationTriggerHandler(
            reminderRepository = get(),
            reminderOccurrenceRepository = get(),
            scheduledNotificationRepository = get(),
            rescheduleRemindersUseCase = get(),
            workSessionManager = get(),
            errorReporter = get()
        )
    }

    single {
        TaskNotificationTriggerHandler()
    }

    single {
        NotificationTriggerRouter(
            reminderHandler = get(),
            taskHandler = get()
        )
    }

    factory {
        RescheduleAllNotificationsUseCase(
            rescheduleRemindersUseCase = get(),
            workSessionManager = get()
        )
    }

    viewModelOf(::ExactAlarmAccessViewModel)
}
