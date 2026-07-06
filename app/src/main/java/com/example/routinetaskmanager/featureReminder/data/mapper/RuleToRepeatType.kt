package com.example.routinetaskmanager.featureReminder.data.mapper

import com.example.routinetaskmanager.featureReminder.domain.model.ReminderRepeatRule
import com.example.routinetaskmanager.featureReminder.domain.model.ReminderRepeatType

fun ReminderRepeatRule.toRepeatType(): String {
    return when (this) {
        is ReminderRepeatRule.DuringSessionPeriod -> "DURING_SESSION_PERIOD"
        is ReminderRepeatRule.OnSchedulePeriod -> "ON_SCHEDULE_PERIOD"
        is ReminderRepeatRule.OnScheduleCertain -> "ON_SCHEDULE_CERTAIN"
    }
}

fun ReminderRepeatRule.toRepeatTypeDomain(): ReminderRepeatType {
    return when (this) {
        is ReminderRepeatRule.DuringSessionPeriod -> ReminderRepeatType.DURING_SESSION_PERIOD
        is ReminderRepeatRule.OnSchedulePeriod -> ReminderRepeatType.ON_SCHEDULE_PERIOD
        is ReminderRepeatRule.OnScheduleCertain -> ReminderRepeatType.ON_SCHEDULE_CERTAIN
    }
}
