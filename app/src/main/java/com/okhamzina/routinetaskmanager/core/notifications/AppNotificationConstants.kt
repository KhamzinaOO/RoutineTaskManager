package com.okhamzina.routinetaskmanager.core.notifications

object AppNotificationConstants {


    const val ACTION_SHOW_NOTIFICATION =
        "com.okhamzina.routinetaskmanager.action.SHOW_NOTIFICATION"
    const val ACTION_COMPLETE_REMINDER =
        "com.okhamzina.routinetaskmanager.action.COMPLETE_REMINDER"
    const val ACTION_SKIP_REMINDER =
        "com.okhamzina.routinetaskmanager.action.SKIP_REMINDER"

    const val EXTRA_TARGET_TYPE = "extra_target_type"
    const val EXTRA_TARGET_ID = "extra_target_id"
    const val EXTRA_SCHEDULED_AT = "extra_scheduled_at"
    const val EXTRA_REQUEST_CODE = "extra_request_code"
    const val EXTRA_OCCURRENCE_KIND = "extra_occurrence_kind"
    const val EXTRA_OCCURRENCE_KEY = "extra_occurrence_key"

    const val REMINDER_GROUP_KEY = "routine_reminders_group"

    const val CHANNEL_NOTIFICATION_SOUND_ID = "notification_sound_channel"
    const val CHANNEL_NOTIFICATION_VIBRATION_ID = "notification_vibration_channel"
    const val CHANNEL_NOTIFICATION_SILENT_ID = "notification_silent_channel"
    const val CHANNEL_WORK_SESSION_ID = "work_session_channel"

    const val ACTION_START_WORK_SESSION_SERVICE =
        "com.okhamzina.routinetaskmanager.action.START_WORK_SESSION_SERVICE"
    const val ACTION_STOP_WORK_SESSION_SERVICE =
        "com.okhamzina.routinetaskmanager.action.STOP_WORK_SESSION_SERVICE"
    const val EXTRA_WORK_SESSION_STARTED_AT = "extra_work_session_started_at"
}
