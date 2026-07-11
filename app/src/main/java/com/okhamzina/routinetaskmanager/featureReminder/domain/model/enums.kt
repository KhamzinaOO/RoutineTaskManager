package com.okhamzina.routinetaskmanager.featureReminder.domain.model

import kotlinx.serialization.Serializable

enum class NotificationMode {
    SOUND,
    VIBRATION,
    MUTE
}

enum class RepeatScheduleMode {
    DEFAULT,
    ADVANCED
}

enum class RepeatUnit {
    MINUTES,
    HOURS,
    DAYS
}

@Serializable
enum class ReminderRepeatType {
    ON_SCHEDULE_PERIOD,
    ON_SCHEDULE_CERTAIN,
    DURING_SESSION_PERIOD
}
