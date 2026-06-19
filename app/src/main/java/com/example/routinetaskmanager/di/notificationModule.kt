package com.example.routinetaskmanager.di

import com.example.routinetaskmanager.core.notifications.AndroidAppAlarmScheduler
import com.example.routinetaskmanager.core.notifications.AppAlarmScheduler
import com.example.routinetaskmanager.core.notifications.AppNotificationChannels
import com.example.routinetaskmanager.core.notifications.AppNotificationFactory
import com.example.routinetaskmanager.core.notifications.AppNotificationManager
import com.example.routinetaskmanager.core.notifications.AppNotificationRuntimeAccessChecker
import com.example.routinetaskmanager.core.notifications.NotificationTriggerRouter
import com.example.routinetaskmanager.core.notifications.ReminderNotificationTriggerHandler
import com.example.routinetaskmanager.core.notifications.RescheduleAllNotificationsUseCase
import com.example.routinetaskmanager.core.notifications.TaskNotificationTriggerHandler
import com.example.routinetaskmanager.core.notifications.WorkSessionForegroundController
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
