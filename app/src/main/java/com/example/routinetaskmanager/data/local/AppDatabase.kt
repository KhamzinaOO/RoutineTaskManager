package com.example.routinetaskmanager.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 4,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun reminderDao(): ReminderDao
    abstract fun scheduleDao(): ScheduledNotificationDao
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            ALTER TABLE scheduled_notifications
            ADD COLUMN occurrenceKind TEXT NOT NULL DEFAULT 'REGULAR'
            """.trimIndent()
        )

        db.execSQL(
            """
            UPDATE scheduled_notifications
            SET occurrenceKind = 'SESSION'
            WHERE occurrenceKey LIKE 'REMINDER-SESSION-%'
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE INDEX IF NOT EXISTS index_scheduled_notifications_occurrenceKind
            ON scheduled_notifications(occurrenceKind)
            """.trimIndent()
        )
    }
}
