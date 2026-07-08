package com.example.routinetaskmanager.featureReminder.presentation.common.mappers

import com.example.routinetaskmanager.R
import com.example.routinetaskmanager.core.error.toAppError
import com.example.routinetaskmanager.core.error.toUiText
import com.example.routinetaskmanager.core.presentation.model.UiText
import com.example.routinetaskmanager.featureReminder.application.session.RestoreWorkSessionRuntimeResult
import com.example.routinetaskmanager.featureReminder.application.session.ToggleWorkSessionResult

fun ToggleWorkSessionResult.toUiMessage(): UiText {
    return when (this) {
        is ToggleWorkSessionResult.Started -> {
            UiText.PluralResource(
                R.plurals.work_session_started_scheduled_count,
                scheduledNotificationCount
            )
        }

        ToggleWorkSessionResult.StartedWithoutReminders -> {
            UiText.StringResource(R.string.work_session_started_no_session_reminders)
        }

        ToggleWorkSessionResult.Ended -> {
            UiText.StringResource(R.string.work_session_ended)
        }

        ToggleWorkSessionResult.ForegroundStartBlocked -> {
            UiText.StringResource(R.string.error_failed_start_work_session_service)
        }

        is ToggleWorkSessionResult.StartFailed -> {
            throwable.toAppError().toUiText(
                defaultMessage = UiText.StringResource(R.string.error_failed_start_work_session)
            )
        }

        is ToggleWorkSessionResult.EndFailed -> {
            throwable.toAppError().toUiText(
                defaultMessage = UiText.StringResource(R.string.error_failed_end_work_session)
            )
        }
    }
}

fun RestoreWorkSessionRuntimeResult.toUiMessageOrNull(): UiText? {
    return when (this) {
        RestoreWorkSessionRuntimeResult.NotActive,
        RestoreWorkSessionRuntimeResult.Restored -> null

        is RestoreWorkSessionRuntimeResult.Failed -> {
            UiText.StringResource(R.string.error_failed_restore_work_session_service)
        }
    }
}
