package com.okhamzina.routinetaskmanager.core.notifications.api

data class NotificationPayload(
    val targetType : NotificationTargetType,
    val targetId : Long,
    val title : String,
    val text : String?,
    val scheduledAtMillis : Long,
    val channelId: String,
    val occurrenceKey: String? = null,
    val occurrenceKind: NotificationOccurrenceKind = NotificationOccurrenceKind.REGULAR
)
