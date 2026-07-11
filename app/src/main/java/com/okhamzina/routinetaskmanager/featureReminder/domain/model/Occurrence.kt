package com.okhamzina.routinetaskmanager.featureReminder.domain.model

import com.okhamzina.routinetaskmanager.core.notifications.api.NotificationOccurrenceKind
import java.time.LocalDateTime

data class ReminderOccurrence(
    val reminderId: Long,
    val reminderName: String,
    val instructionsText: String?,
    val scheduledAt: LocalDateTime,
    val repeatType: ReminderRepeatType,
    val status: ReminderOccurrenceStatus = ReminderOccurrenceStatus.PLANNED,
    val occurrenceKey: String,
    val scheduledAtMillis: Long,
    val occurrenceKind: NotificationOccurrenceKind = NotificationOccurrenceKind.REGULAR
)

enum class ReminderOccurrenceStatus {
    PLANNED,
    COMPLETED,
    SKIPPED,
    MISSED
}