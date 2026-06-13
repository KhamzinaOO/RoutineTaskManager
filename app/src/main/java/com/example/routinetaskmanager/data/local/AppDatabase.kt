package com.example.routinetaskmanager.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.routinetaskmanager.core.notifications.ScheduledNotificationDao
import com.example.routinetaskmanager.core.notifications.ScheduledNotificationEntity
import com.example.routinetaskmanager.featureReminder.data.local.ReminderDao
import com.example.routinetaskmanager.featureReminder.data.local.ReminderEntity
import com.example.routinetaskmanager.featureReminder.data.local.ReminderImageEntity

@Database(
    entities = [
        ReminderEntity::class,
        ReminderImageEntity::class,
        ScheduledNotificationEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun reminderDao(): ReminderDao
    abstract fun scheduleDao(): ScheduledNotificationDao
}