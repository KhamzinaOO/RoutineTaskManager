package com.okhamzina.routinetaskmanager.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.okhamzina.routinetaskmanager.core.notifications.data.local.ScheduledNotificationDao
import com.okhamzina.routinetaskmanager.core.notifications.data.local.ScheduledNotificationEntity
import com.okhamzina.routinetaskmanager.featureReminder.data.local.ReminderDao
import com.okhamzina.routinetaskmanager.featureReminder.data.local.ReminderEntity
import com.okhamzina.routinetaskmanager.featureReminder.data.local.ReminderImageEntity
import com.okhamzina.routinetaskmanager.featureReminder.data.local.ReminderOccurrenceDAO
import com.okhamzina.routinetaskmanager.featureReminder.data.local.ReminderOccurrenceStateEntity

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
