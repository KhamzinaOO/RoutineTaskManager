package com.example.routinetaskmanager.core.notifications

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "scheduled_notifications",
    indices = [
        Index("targetType"),
        Index("targetId"),
        Index("scheduledAtMillis")
    ]
)
data class ScheduledNotificationEntity (
    @PrimaryKey
    val requestCode : Int,
    val targetType: NotificationTargetType,
    val targetId : Long,
    val scheduledAtMillis : Long,
    val title : String,
    val text : String?
)