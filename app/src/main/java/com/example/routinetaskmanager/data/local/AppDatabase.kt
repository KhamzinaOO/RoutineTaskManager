package com.example.routinetaskmanager.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.routinetaskmanager.featureReminder.data.local.ReminderDao
import com.example.routinetaskmanager.featureReminder.data.local.ReminderEntity
import com.example.routinetaskmanager.featureReminder.data.local.ReminderImageEntity

@Database(
    entities = [
        ReminderEntity::class,
        ReminderImageEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun reminderDao(): ReminderDao
}