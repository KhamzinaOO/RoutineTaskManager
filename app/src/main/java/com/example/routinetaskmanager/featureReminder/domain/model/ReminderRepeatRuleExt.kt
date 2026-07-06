package com.example.routinetaskmanager.featureReminder.domain.model

val ReminderRepeatRule.type : ReminderRepeatType
    get() = when (this) {
        is ReminderRepeatRule.DuringSessionPeriod -> ReminderRepeatType.DURING_SESSION_PERIOD
        is ReminderRepeatRule.OnSchedulePeriod -> ReminderRepeatType.ON_SCHEDULE_PERIOD
        is ReminderRepeatRule.OnScheduleCertain -> ReminderRepeatType.ON_SCHEDULE_CERTAIN
    }
