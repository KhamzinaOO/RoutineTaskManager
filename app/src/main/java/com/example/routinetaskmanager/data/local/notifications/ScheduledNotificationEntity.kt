package com.example.routinetaskmanager.data.local.notifications

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.routinetaskmanager.core.notifications.api.NotificationOccurrenceKind

@Entity(
    tableName = "scheduled_notifications",
    indices = [
        Index("targetType"),
        Index("targetId"),
        Index("scheduledAtMillis"),
        Index("occurrenceKind"),
        Index(value = ["occurrenceKey"], unique = true)
    ]
)
data class ScheduledNotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val requestCode: Int,
    val targetType: String,
    val targetId: Long,
    val scheduledAtMillis: Long,
    val occurrenceKey: String,
//    val channelId: String,
    val occurrenceKind: String = NotificationOccurrenceKind.REGULAR.name,
    val createdAtMillis: Long = System.currentTimeMillis()
)