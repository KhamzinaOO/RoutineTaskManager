package com.example.routinetaskmanager.di

import com.example.routinetaskmanager.core.notifications.AndroidAppAlarmScheduler
import com.example.routinetaskmanager.core.notifications.AppAlarmScheduler
import com.example.routinetaskmanager.core.notifications.AppNotificationChannels
import com.example.routinetaskmanager.core.notifications.AppNotificationFactory
import com.example.routinetaskmanager.core.notifications.AppNotificationManager
import com.example.routinetaskmanager.core.notifications.AppNotificationPermissionChecker
import com.example.routinetaskmanager.core.notifications.NotificationRouter
import com.example.routinetaskmanager.core.notifications.NotificationTriggerRouter
import com.example.routinetaskmanager.core.notifications.ReminderNotificationHandler
import com.example.routinetaskmanager.core.notifications.ReminderNotificationTriggerHandler
import com.example.routinetaskmanager.core.notifications.RescheduleAllNotificationsUseCase
import com.example.routinetaskmanager.core.notifications.TaskNotificationHandler
import com.example.routinetaskmanager.core.notifications.TaskNotificationTriggerHandler
import com.example.routinetaskmanager.featureReminder.notifications.ReminderNotificationHandlerImpl
import com.example.routinetaskmanager.featureTask.TaskNotificationHandlerImpl
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
        AppNotificationPermissionChecker(
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

    single<TaskNotificationHandler> {
        TaskNotificationHandlerImpl(
            scheduledNotificationDao = get()
        )
    }

    single<ReminderNotificationHandler> {
        ReminderNotificationHandlerImpl(
            rescheduleRemindersUseCase = get()
        )
    }

    single {
        NotificationRouter(
            taskNotificationHandler = get(),
            reminderNotificationHandler = get()
        )
    }

    single {
        ReminderNotificationTriggerHandler(
            reminderRepository = get(),
            scheduledNotificationDao = get(),
            rescheduleRemindersUseCase = get()
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
