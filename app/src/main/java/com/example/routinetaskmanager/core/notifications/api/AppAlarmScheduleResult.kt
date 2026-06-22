package com.example.routinetaskmanager.core.notifications.api

/**
sealed interface AlarmScheduleResult {
    data object Scheduled : AlarmScheduleResult
    data object TimeInPast : AlarmScheduleResult
    data object NotificationsBlocked : AlarmScheduleResult
    data object ExactAlarmAccessDenied : AlarmScheduleResult
    data class Failed(val throwable: Throwable) : AlarmScheduleResult
}*/