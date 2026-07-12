package com.okhamzina.routinetaskmanager.core.notifications.data

import android.content.Context
import com.okhamzina.routinetaskmanager.core.notifications.android.AppNotificationRuntimeAccessChecker
import com.okhamzina.routinetaskmanager.core.notifications.domain.ExactAlarmAccessRepository
import androidx.core.content.edit

class SharedPrefsExactAlarmAccessRepository(
    context: Context,
    private val accessChecker: AppNotificationRuntimeAccessChecker
) : ExactAlarmAccessRepository {

    private val preferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    override fun hasExactAlarmAccess(): Boolean {
        return accessChecker.canScheduleExactAlarms()
    }

    override fun isExactWarningDismissedForever(): Boolean {
        return preferences.getBoolean(KEY_WARNING_DISMISSED_FOREVER, false)
    }

    override fun dismissExactWarningForever() {
        preferences.edit {
            putBoolean(KEY_WARNING_DISMISSED_FOREVER, true)
        }
    }

    override fun hasNotificationAccess(): Boolean {
        return accessChecker.canPostNotifications()
    }

    override fun isNotificationWarningDismissedForever(): Boolean {
        return preferences.getBoolean(KEY_NOTIFICATIONS_DISABLED_WARNING_DISMISSED, false)
    }

    override fun dismissNotificationWarningForever() {
        preferences.edit{
            putBoolean(KEY_NOTIFICATIONS_DISABLED_WARNING_DISMISSED, true)
        }
    }

    private companion object {
        const val PREFS_NAME = "notification_warning_preferences"
        const val KEY_WARNING_DISMISSED_FOREVER = "warning_dismissed_forever"

        const val KEY_NOTIFICATIONS_DISABLED_WARNING_DISMISSED = "notifications_disabled_warning_dismissed"
    }
}
