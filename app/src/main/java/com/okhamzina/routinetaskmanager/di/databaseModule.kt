package com.okhamzina.routinetaskmanager.di

import androidx.room.Room
import com.okhamzina.routinetaskmanager.core.notifications.data.local.ScheduledNotificationRepositoryImpl
import com.okhamzina.routinetaskmanager.core.notifications.data.local.ScheduledNotificationDao
import com.okhamzina.routinetaskmanager.core.notifications.domain.ScheduledNotificationRepository
import com.okhamzina.routinetaskmanager.data.local.AppDatabase
import com.okhamzina.routinetaskmanager.featureReminder.data.local.ReminderDao
import com.okhamzina.routinetaskmanager.featureReminder.data.local.ReminderOccurrenceDAO
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {

    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "routine_task_manager.db"
        )
            .fallbackToDestructiveMigration(true)
            .build()
    }

    single<ReminderDao> {
        get<AppDatabase>().reminderDao()
    }

    single<ScheduledNotificationDao> {
        get<AppDatabase>().scheduleDao()
    }

    single<ScheduledNotificationRepository> {
        ScheduledNotificationRepositoryImpl(
            scheduledNotificationDao = get()
        )
    }

    single<ReminderOccurrenceDAO> {
        get<AppDatabase>().reminderOccurrenceDao()
    }
}
