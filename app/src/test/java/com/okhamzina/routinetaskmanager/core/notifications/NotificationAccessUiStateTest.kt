package com.okhamzina.routinetaskmanager.core.notifications

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationAccessUiStateTest {

    @Test
    fun warningsAreHiddenWhenThereAreNoScheduledNotifications() {
        val state = state(hasScheduledNotifications = false)

        assertFalse(state.shouldShowExactAlarmWarning)
        assertFalse(state.shouldShowNotificationWarning)
    }

    @Test
    fun warningsAreShownWhenNotificationsAreScheduledAndAccessIsMissing() {
        val state = state(hasScheduledNotifications = true)

        assertTrue(state.shouldShowExactAlarmWarning)
        assertTrue(state.shouldShowNotificationWarning)
    }

    private fun state(hasScheduledNotifications: Boolean) = NotificationAccessUiState(
        hasExactAlarmAccess = false,
        hasNotificationAccess = false,
        hasScheduledNotifications = hasScheduledNotifications,
        isExactWarningDismissedForever = false,
        isNotificationWarningDismissedForever = false
    )
}
