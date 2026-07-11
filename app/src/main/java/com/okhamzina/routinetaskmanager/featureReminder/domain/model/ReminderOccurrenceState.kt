package com.okhamzina.routinetaskmanager.featureReminder.domain.model

import com.okhamzina.routinetaskmanager.core.notifications.api.NotificationOccurrenceKind

data class ReminderOccurrenceState(
    val occurrenceKey: String,
    val reminderId: Long,
    val scheduledAtMillis: Long,
    val status: ReminderOccurrenceStatus,
    val actedAtMillis: Long,
    val occurrenceKind: NotificationOccurrenceKind = NotificationOccurrenceKind.REGULAR
)
