package com.example.routinetaskmanager.core.notifications.api

import com.example.routinetaskmanager.core.notifications.api.NotificationTargetType

//TODO : add adequate failure result instead of Boolean false
interface AppAlarmScheduler {

    fun schedule(
        targetType: NotificationTargetType,
        targetId: Long,
        scheduledAtMillis: Long,
        requestCode: Int,
        precision: AlarmPrecision = AlarmPrecision.INEXACT
    ): Boolean

    fun cancel(
        requestCode: Int
    )
}

/**
sealed interface AlarmScheduleResult {
    data object Scheduled : AlarmScheduleResult
    data object TimeInPast : AlarmScheduleResult
    data object NotificationsBlocked : AlarmScheduleResult
    data object ExactAlarmAccessDenied : AlarmScheduleResult
    data class Failed(val throwable: Throwable) : AlarmScheduleResult
}*/