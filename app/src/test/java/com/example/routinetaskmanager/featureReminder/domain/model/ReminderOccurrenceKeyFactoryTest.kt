package com.example.routinetaskmanager.featureReminder.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ReminderOccurrenceKeyFactoryTest {

    @Test
    fun regular_buildsStableKeyFromReminderIdAndTime() {
        val key = ReminderOccurrenceKeyFactory.regular(
            reminderId = 42L,
            scheduledAtMillis = 1_234_567L
        )

        assertEquals("REMINDER-42-1234567", key)
    }

    @Test
    fun session_buildsStableKeyWithSequenceNumber() {
        val key = ReminderOccurrenceKeyFactory.session(
            reminderId = 42L,
            scheduledAtMillis = 1_234_567L,
            sequence = 3
        )

        assertEquals("REMINDER-SESSION-42-1234567-3", key)
    }
}
