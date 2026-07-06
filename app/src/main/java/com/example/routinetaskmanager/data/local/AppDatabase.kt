package com.example.routinetaskmanager.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.routinetaskmanager.data.local.notifications.ScheduledNotificationDao
import com.example.routinetaskmanager.data.local.notifications.ScheduledNotificationEntity
import com.example.routinetaskmanager.featureReminder.data.local.ReminderDao
import com.example.routinetaskmanager.featureReminder.data.local.ReminderEntity
import com.example.routinetaskmanager.featureReminder.data.local.ReminderImageEntity
import com.example.routinetaskmanager.featureReminder.data.local.ReminderOccurrenceDAO
import com.example.routinetaskmanager.featureReminder.data.local.ReminderOccurrenceStateEntity

@Database(
    entities = [
        ReminderEntity::class,
        ReminderImageEntity::class,
        ScheduledNotificationEntity::class,
        ReminderOccurrenceStateEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun reminderDao(): ReminderDao
    abstract fun scheduleDao(): ScheduledNotificationDao
    abstract fun reminderOccurrenceDao(): ReminderOccurrenceDAO
}
