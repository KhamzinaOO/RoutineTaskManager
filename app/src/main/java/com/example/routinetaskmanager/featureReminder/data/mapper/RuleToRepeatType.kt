package com.example.routinetaskmanager.featureReminder.data.mapper

import com.example.routinetaskmanager.featureReminder.domain.model.ReminderRepeatRule

fun ReminderRepeatRule.toRepeatType(): String {
    return when (this) {
        is ReminderRepeatRule.AfterAnother -> "AFTER_ANOTHER"
        is ReminderRepeatRule.DuringSessionPeriod -> "DURING_SESSION_PERIOD"
        is ReminderRepeatRule.OnSchedulePeriod -> "ON_SCHEDULE_PERIOD"
        is ReminderRepeatRule.OnScheduleCertain -> "ON_SCHEDULE_CERTAIN"
    }
}