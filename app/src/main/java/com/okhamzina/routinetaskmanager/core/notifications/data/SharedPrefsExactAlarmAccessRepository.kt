package com.okhamzina.routinetaskmanager.core.notifications.data

import android.content.Context
import com.okhamzina.routinetaskmanager.core.notifications.android.AppNotificationRuntimeAccessChecker
import com.okhamzina.routinetaskmanager.core.notifications.domain.ExactAlarmAccessRepository

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

    override fun isWarningDismissedForever(): Boolean {
        return preferences.getBoolean(KEY_WARNING_DISMISSED_FOREVER, false)
    }

    override fun dismissWarningForever() {
        preferences.edit()
            .putBoolean(KEY_WARNING_DISMISSED_FOREVER, true)
            .apply()
    }

    private companion object {
        const val PREFS_NAME = "exact_alarm_access_preferences"
        const val KEY_WARNING_DISMISSED_FOREVER = "warning_dismissed_forever"
    }
}
