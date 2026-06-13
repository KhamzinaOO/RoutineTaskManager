package com.example.routinetaskmanager.di

import androidx.room.Room
import com.example.routinetaskmanager.core.notifications.ScheduledNotificationDao
import com.example.routinetaskmanager.data.local.AppDatabase
import com.example.routinetaskmanager.featureReminder.data.local.ReminderDao
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import java.util.concurrent.Executors

val databaseModule = module {

    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "routine_task_manager.db"
        )
            .setQueryCallback({ sqlQuery, bindArgs ->
                println("SQL Query: $sqlQuery | Args: $bindArgs")
            }, Executors.newSingleThreadExecutor())
            .fallbackToDestructiveMigration(false)
            .build()
    }

    single<ReminderDao> {
        get<AppDatabase>().reminderDao()
    }

    single<ScheduledNotificationDao> {
        get<AppDatabase>().scheduleDao()
    }
}