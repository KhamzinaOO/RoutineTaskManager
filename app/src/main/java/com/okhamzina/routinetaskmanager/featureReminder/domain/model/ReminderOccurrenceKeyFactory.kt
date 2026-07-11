package com.okhamzina.routinetaskmanager.featureReminder.domain.model

object ReminderOccurrenceKeyFactory {
    fun regular(
        reminderId: Long,
        scheduledAtMillis: Long
    ): String {
        return "REMINDER-$reminderId-$scheduledAtMillis"
    }

    fun session(
        reminderId: Long,
        scheduledAtMillis: Long,
        sequence: Int
    ): String {
        return "REMINDER-SESSION-$reminderId-$scheduledAtMillis-$sequence"
    }
}
