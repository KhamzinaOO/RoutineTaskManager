package com.okhamzina.routinetaskmanager.core.presentation.ui

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.okhamzina.routinetaskmanager.R
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.core.net.toUri

enum class PermissionDeniedAction {
    RetryRequest,
    OpenSettings
}

data class ExactAlarmPromptConfig(
    val skipExplanation: Boolean = false,
    val onDoNotShowAgain: () -> Unit = {}
)

val LocalExactAlarmPromptConfig = staticCompositionLocalOf {
    ExactAlarmPromptConfig()
}

@Composable
fun rememberNotificationPermissionRequest(
    onGranted: () -> Unit,
    onDenied: () -> Unit,
    onDeniedWithAction: ((PermissionDeniedAction) -> Unit)? = null
): () -> Unit {
    val context = LocalContext.current
    val activity = context.findActivity()
    var showRationale by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted && areNotificationsEnabled(context)) {
            onGranted()
        } else {
            notifyNotificationPermissionDenied(
                context = context,
                activity = activity,
                onDenied = onDenied,
                onDeniedWithAction = onDeniedWithAction
            )
        }
    }

    fun requestPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            if (areNotificationsEnabled(context)) {
                onGranted()
            } else {
                onDeniedWithAction?.invoke(PermissionDeniedAction.OpenSettings) ?: onDenied()
            }
            return
        }

        val permission = Manifest.permission.POST_NOTIFICATIONS
        val isGranted = ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED

        if (isGranted) {
            if (areNotificationsEnabled(context)) {
                onGranted()
            } else {
                onDeniedWithAction?.invoke(PermissionDeniedAction.OpenSettings) ?: onDenied()
            }
            return
        }

        val shouldShowRationale = activity?.let {
            ActivityCompat.shouldShowRequestPermissionRationale(it, permission)
        } ?: false

        if (shouldShowRationale) {
            showRationale = true
        } else {
            launcher.launch(permission)
        }
    }

    if (showRationale) {
        AlertDialog(
            onDismissRequest = {
                showRationale = false
                onDeniedWithAction?.invoke(PermissionDeniedAction.RetryRequest) ?: onDenied()
            },
            title = {
                Text(text = stringResource(R.string.notification_permission_title))
            },
            text = {
                Text(text = stringResource(R.string.notification_permission_rationale))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRationale = false
                        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                ) {
                    Text(
                        text = stringResource(R.string.action_allow),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showRationale = false
                        onDeniedWithAction?.invoke(PermissionDeniedAction.RetryRequest) ?: onDenied()
                    }
                ) {
                    Text(
                        text = stringResource(R.string.action_not_now),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        )
    }

    return ::requestPermission
}

@Composable
fun rememberExactAlarmAccessRequest(
    onGranted: () -> Unit,
    onDenied: () -> Unit,
    onDeniedWithAction: ((PermissionDeniedAction) -> Unit)? = null,
    skipExplanation: Boolean = false,
    onDoNotShowAgain: () -> Unit = {}
): () -> Unit {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var showExplanation by remember { mutableStateOf(false) }
    var awaitingSettingsResult by remember { mutableStateOf(false) }
    var doNotShowAgain by remember { mutableStateOf(false) }

    fun persistDialogPreferenceIfNeeded() {
        if (doNotShowAgain) {
            onDoNotShowAgain()
        }
    }

    fun hasExactAlarmAccess(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return true
        }

        val alarmManager = context.getSystemService(AlarmManager::class.java)
        return alarmManager.canScheduleExactAlarms()
    }

    fun openExactAlarmSettings() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            onGranted()
            return
        }

        awaitingSettingsResult = true

        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
            data = "package:${context.packageName}".toUri()
        }

        runCatching {
            context.startActivity(intent)
        }.onFailure {
            awaitingSettingsResult = false
            onDeniedWithAction?.invoke(PermissionDeniedAction.OpenSettings) ?: onDenied()
        }
    }

    DisposableEffect(lifecycleOwner, awaitingSettingsResult) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && awaitingSettingsResult) {
                awaitingSettingsResult = false

                if (hasExactAlarmAccess()) {
                    onGranted()
                } else {
                    onDeniedWithAction?.invoke(PermissionDeniedAction.OpenSettings) ?: onDenied()
                }
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    fun requestAccess() {
        if (hasExactAlarmAccess()) {
            onGranted()
        } else if (skipExplanation) {
            onDenied()
        } else {
            doNotShowAgain = false
            showExplanation = true
        }
    }

    if (showExplanation) {
        AlertDialog(
            onDismissRequest = {
                showExplanation = false
                persistDialogPreferenceIfNeeded()
                onDeniedWithAction?.invoke(PermissionDeniedAction.OpenSettings) ?: onDenied()
            },
            title = {
                Text(text = stringResource(R.string.exact_alarm_permission_title))
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = stringResource(R.string.exact_alarm_permission_rationale))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = doNotShowAgain,
                            onCheckedChange = { checked -> doNotShowAgain = checked }
                        )
                        Text(text = stringResource(R.string.action_do_not_show_again))
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExplanation = false
                        persistDialogPreferenceIfNeeded()
                        openExactAlarmSettings()
                    }
                ) {
                    Text(
                        text = stringResource(R.string.action_open_settings),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showExplanation = false
                        persistDialogPreferenceIfNeeded()
                        onDeniedWithAction?.invoke(PermissionDeniedAction.OpenSettings) ?: onDenied()
                    }
                ) {
                    Text(
                        text = stringResource(R.string.action_not_now),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        )
    }

    return ::requestAccess
}

fun openAppNotificationSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        }

    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching {
        context.startActivity(intent)
    }
}

fun openExactAlarmSettings(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
        return
    }

    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
        data = "package:${context.packageName}".toUri()
    }

    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    runCatching {
        context.startActivity(intent)
    }
}

private fun notifyNotificationPermissionDenied(
    context: Context,
    activity: Activity?,
    onDenied: () -> Unit,
    onDeniedWithAction: ((PermissionDeniedAction) -> Unit)?
) {
    val action = if (canRequestNotificationPermissionAgain(activity)) {
        PermissionDeniedAction.RetryRequest
    } else {
        PermissionDeniedAction.OpenSettings
    }

    onDeniedWithAction?.invoke(action) ?: onDenied()
}

private fun canRequestNotificationPermissionAgain(
    activity: Activity?
): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        return false
    }

    return activity?.let {
        ActivityCompat.shouldShowRequestPermissionRationale(
            it,
            Manifest.permission.POST_NOTIFICATIONS
        )
    } ?: false
}

private fun areNotificationsEnabled(context: Context): Boolean {
    return NotificationManagerCompat.from(context).areNotificationsEnabled()
}

private tailrec fun Context.findActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}
