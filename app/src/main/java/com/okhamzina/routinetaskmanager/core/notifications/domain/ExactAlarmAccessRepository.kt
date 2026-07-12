package com.okhamzina.routinetaskmanager.core.notifications.domain

interface ExactAlarmAccessRepository {
    fun hasExactAlarmAccess(): Boolean

    fun isExactWarningDismissedForever(): Boolean

    fun dismissExactWarningForever()

    fun hasNotificationAccess(): Boolean

    fun isNotificationWarningDismissedForever(): Boolean

    fun dismissNotificationWarningForever()
}
