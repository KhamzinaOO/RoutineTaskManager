package com.okhamzina.routinetaskmanager.featureReminder.presentation.common.mappers

import com.okhamzina.routinetaskmanager.R
import com.okhamzina.routinetaskmanager.core.error.toUiText
import com.okhamzina.routinetaskmanager.core.presentation.model.UiText
import com.okhamzina.routinetaskmanager.featureReminder.application.session.RestoreWorkSessionRuntimeResult
import com.okhamzina.routinetaskmanager.featureReminder.application.session.ToggleWorkSessionResult

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
            error.toUiText(
                defaultMessage = UiText.StringResource(R.string.error_failed_start_work_session)
            )
        }

        is ToggleWorkSessionResult.EndFailed -> {
            error.toUiText(
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
            error.toUiText(
                defaultMessage = UiText.StringResource(R.string.error_failed_restore_work_session_service)
            )
        }
    }
}
