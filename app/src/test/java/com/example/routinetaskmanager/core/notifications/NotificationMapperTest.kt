package com.example.routinetaskmanager.core.notifications

import com.example.routinetaskmanager.featureReminder.domain.model.NotificationMode
import org.junit.Assert.assertEquals
import org.junit.Test

class NotificationMapperTest {

    @Test
    fun notificationModeToReminderChannelId_mapsEachModeToExpectedChannel() {
        assertEquals(
            AppNotificationConstants.CHANNEL_NOTIFICATION_SOUND_ID,
            NotificationMode.SOUND.toReminderChannelId()
        )
        assertEquals(
            AppNotificationConstants.CHANNEL_NOTIFICATION_VIBRATION_ID,
            NotificationMode.VIBRATION.toReminderChannelId()
        )
        assertEquals(
            AppNotificationConstants.CHANNEL_NOTIFICATION_SILENT_ID,
            NotificationMode.MUTE.toReminderChannelId()
        )
    }
}
