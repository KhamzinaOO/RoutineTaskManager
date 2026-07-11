package com.okhamzina.routinetaskmanager.core.notifications.api

interface AppAlarmScheduler {

    fun schedule(
        targetType: NotificationTargetType,
        targetId: Long,
        scheduledAtMillis: Long,
        requestCode: Int,
        precision: AlarmPrecision = AlarmPrecision.INEXACT,
        occurrenceKind: NotificationOccurrenceKind = NotificationOccurrenceKind.REGULAR
    ): AppAlarmScheduleResult

    fun cancel(
        requestCode: Int
    )
}
