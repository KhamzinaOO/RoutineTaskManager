package com.example.routinetaskmanager.di

import com.example.routinetaskmanager.core.notifications.android.AndroidAppAlarmScheduler
import com.example.routinetaskmanager.core.notifications.api.AppAlarmScheduler
import com.example.routinetaskmanager.core.notifications.android.AppNotificationChannels
import com.example.routinetaskmanager.core.notifications.android.AppNotificationFactory
import com.example.routinetaskmanager.core.notifications.android.AppNotificationManager
import com.example.routinetaskmanager.core.notifications.android.AppNotificationRuntimeAccessChecker
import com.example.routinetaskmanager.core.notifications.NotificationTriggerRouter
import com.example.routinetaskmanager.featureReminder.application.notifications.ReminderNotificationTriggerHandler
import com.example.routinetaskmanager.core.notifications.RescheduleAllNotificationsUseCase
import com.example.routinetaskmanager.featureTask.TaskNotificationTriggerHandler
import com.example.routinetaskmanager.featureReminder.data.session.WorkSessionForegroundController
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

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
            scheduledNotificationDao = get(),
            rescheduleRemindersUseCase = get(),
            workSessionManager = get()
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
}
