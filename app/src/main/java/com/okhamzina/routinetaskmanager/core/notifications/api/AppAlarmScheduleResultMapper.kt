package com.okhamzina.routinetaskmanager.core.notifications.api

import com.okhamzina.routinetaskmanager.core.error.AppError

fun AppAlarmScheduleResult.toAppErrorOrNull(): AppError? {
    return when (this) {
        AppAlarmScheduleResult.Scheduled,
        AppAlarmScheduleResult.TimeInPast -> null

        AppAlarmScheduleResult.ExactAlarmAccessDenied -> AppError.ExactAlarmPermissionDenied

        is AppAlarmScheduleResult.Failed -> AppError.AlarmSchedulingFailed(throwable)
    }
}
