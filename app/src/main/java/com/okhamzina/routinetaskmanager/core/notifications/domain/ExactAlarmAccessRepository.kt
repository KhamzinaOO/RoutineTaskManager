package com.okhamzina.routinetaskmanager.core.notifications.domain

interface ExactAlarmAccessRepository {
    fun hasExactAlarmAccess(): Boolean

    fun isWarningDismissedForever(): Boolean

    fun dismissWarningForever()
}
