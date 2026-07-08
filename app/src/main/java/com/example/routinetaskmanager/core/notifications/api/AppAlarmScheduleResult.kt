package com.example.routinetaskmanager.core.notifications.api

sealed interface AppAlarmScheduleResult {
    data object Scheduled : AppAlarmScheduleResult
    data object TimeInPast : AppAlarmScheduleResult
    data object ExactAlarmAccessDenied : AppAlarmScheduleResult
    data class Failed(val throwable: Throwable? = null) : AppAlarmScheduleResult
}
