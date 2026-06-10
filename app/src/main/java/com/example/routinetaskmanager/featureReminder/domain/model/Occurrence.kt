package com.example.routinetaskmanager.featureReminder.domain.model

import java.time.LocalDateTime

data class ReminderOccurrence(
    val reminderId: Long,
    val reminderName: String,
    val instructionsText: String?,
    val scheduledAt: LocalDateTime,
    val repeatType: ReminderRepeatType,
    val status: ReminderOccurrenceStatus = ReminderOccurrenceStatus.PLANNED
)

enum class ReminderOccurrenceStatus {
    PLANNED,
    COMPLETED,
    SKIPPED,
    MISSED
}