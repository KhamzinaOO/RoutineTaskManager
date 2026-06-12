package com.example.routinetaskmanager.core.notifications

object AppNotificationConstants {


    const val ACTION_SHOW_NOTIFICATION =
        "com.example.routinetaskmanager.action.SHOW_NOTIFICATION"

    const val EXTRA_TARGET_TYPE = "extra_target_type"
    const val EXTRA_TARGET_ID = "extra_target_id"
    const val EXTRA_SCHEDULED_AT = "extra_scheduled_at"
    const val EXTRA_REQUEST_CODE = "extra_request_code"

    const val REMINDER_GROUP_KEY = "routine_reminders_group"

    const val CHANNEL_NOTIFICATION_SOUND_ID = "notification_sound_channel"
    const val CHANNEL_NOTIFICATION_VIBRATION_ID = "notification_vibration_channel"
    const val CHANNEL_NOTIFICATION_SILENT_ID = "notification_silent_channel"
}