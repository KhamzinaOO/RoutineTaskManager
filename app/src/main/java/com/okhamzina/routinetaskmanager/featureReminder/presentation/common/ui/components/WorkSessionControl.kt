package com.okhamzina.routinetaskmanager.featureReminder.presentation.common.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.okhamzina.routinetaskmanager.R
import com.okhamzina.routinetaskmanager.core.presentation.ui.PermissionDeniedAction
import com.okhamzina.routinetaskmanager.core.presentation.ui.openAppNotificationSettings
import com.okhamzina.routinetaskmanager.core.presentation.ui.rememberNotificationPermissionRequest
import com.okhamzina.routinetaskmanager.core.presentation.ui.rememberExactAlarmAccessRequest
import com.okhamzina.routinetaskmanager.core.presentation.ui.LocalExactAlarmPromptConfig
import com.okhamzina.routinetaskmanager.core.utills.formatTime
import kotlinx.coroutines.delay

@Composable
fun WorkSessionControl(
    modifier: Modifier = Modifier,
    remindersCount: Int,
    isActive: Boolean,
    startedAtMillis: Long?,
    isLoading: Boolean,
    showActionMessage: (message: String, actionLabel: String, onAction: () -> Unit) -> Unit,
    onStartSession: () -> Unit,
    onEndSession: () -> Unit,
    onNotificationPermissionDenied: () -> Unit
) {
    val context = LocalContext.current
    val exactAlarmPromptConfig = LocalExactAlarmPromptConfig.current
    val retryActionLabel = stringResource(R.string.action_retry)
    val settingsActionLabel = stringResource(R.string.action_settings)
    val notificationsRequiredMessage = stringResource(R.string.notifications_required_for_session)
    val notificationPermissionRequestRef = remember { arrayOf<(() -> Unit)?>(null) }

    val requestExactAlarmAccess = rememberExactAlarmAccessRequest(
        onGranted = onStartSession,
        onDenied = onStartSession,
        skipExplanation = exactAlarmPromptConfig.skipExplanation,
        onDoNotShowAgain = exactAlarmPromptConfig.onDoNotShowAgain
    )

    val requestNotificationPermission = rememberNotificationPermissionRequest(
        onGranted = requestExactAlarmAccess,
        onDenied = onNotificationPermissionDenied,
        onDeniedWithAction = { action ->
            val actionLabel = when (action) {
                PermissionDeniedAction.RetryRequest -> retryActionLabel
                PermissionDeniedAction.OpenSettings -> settingsActionLabel
            }

            showActionMessage(
                notificationsRequiredMessage,
                actionLabel
            ) {
                when (action) {
                    PermissionDeniedAction.RetryRequest -> notificationPermissionRequestRef[0]?.invoke()
                    PermissionDeniedAction.OpenSettings -> openAppNotificationSettings(context)
                }
            }
        }
    )
    notificationPermissionRequestRef[0] = requestNotificationPermission

    WorkSessionButton(
        modifier = modifier,
        remindersCount = remindersCount,
        timer = rememberWorkSessionTimerText(
            isActive = isActive,
            startedAtMillis = startedAtMillis
        ),
        isActive = isActive,
        isLoading = isLoading,
        onEndClick = onEndSession,
        onStartClick = { requestNotificationPermission() }
    )
}

@Composable
private fun rememberWorkSessionTimerText(
    isActive: Boolean,
    startedAtMillis: Long?
): String {
    var elapsedTimeMillis by remember { mutableLongStateOf(0L) }

    LaunchedEffect(isActive, startedAtMillis) {
        if (!isActive || startedAtMillis == null) {
            elapsedTimeMillis = 0L
            return@LaunchedEffect
        }

        while (true) {
            elapsedTimeMillis = System.currentTimeMillis() - startedAtMillis
            delay(1000L)
        }
    }

    return formatTime(elapsedTimeMillis)
}
