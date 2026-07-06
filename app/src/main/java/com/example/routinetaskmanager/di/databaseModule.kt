package com.example.routinetaskmanager.di

import androidx.room.Room
import com.example.routinetaskmanager.data.local.notifications.ScheduledNotificationDao
import com.example.routinetaskmanager.data.local.AppDatabase
import com.example.routinetaskmanager.featureReminder.data.local.ReminderDao
import com.example.routinetaskmanager.featureReminder.data.local.ReminderOccurrenceDAO
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

    single<ReminderOccurrenceDAO> {
        get<AppDatabase>().reminderOccurrenceDao()
    }
}
