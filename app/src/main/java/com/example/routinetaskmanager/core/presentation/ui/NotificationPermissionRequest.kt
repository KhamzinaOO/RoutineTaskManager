package com.example.routinetaskmanager.core.presentation.ui

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

enum class PermissionDeniedAction {
    RetryRequest,
    OpenSettings
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
        if (isGranted) {
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
            onGranted()
            return
        }

        val permission = Manifest.permission.POST_NOTIFICATIONS
        val isGranted = ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED

        if (isGranted) {
            onGranted()
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
                Text(text = "Allow notifications?")
            },
            text = {
                Text(text = "Work sessions use notifications to remind you at the selected times.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRationale = false
                        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                ) {
                    Text(text = "Allow")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showRationale = false
                        onDeniedWithAction?.invoke(PermissionDeniedAction.RetryRequest) ?: onDenied()
                    }
                ) {
                    Text(text = "Not now")
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
    onDeniedWithAction: ((PermissionDeniedAction) -> Unit)? = null
): () -> Unit {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var showExplanation by remember { mutableStateOf(false) }
    var awaitingSettingsResult by remember { mutableStateOf(false) }

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
            data = Uri.parse("package:${context.packageName}")
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
        } else {
            showExplanation = true
        }
    }

    if (showExplanation) {
        AlertDialog(
            onDismissRequest = {
                showExplanation = false
                onDeniedWithAction?.invoke(PermissionDeniedAction.OpenSettings) ?: onDenied()
            },
            title = {
                Text(text = "Allow exact reminders?")
            },
            text = {
                Text(text = "Exact alarm access helps work-session reminders arrive at the selected minute.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExplanation = false
                        openExactAlarmSettings()
                    }
                ) {
                    Text(text = "Open settings")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showExplanation = false
                        onDeniedWithAction?.invoke(PermissionDeniedAction.OpenSettings) ?: onDenied()
                    }
                ) {
                    Text(text = "Not now")
                }
            }
        )
    }

    return ::requestAccess
}

fun openAppNotificationSettings(context: Context) {
    val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        }
    } else {
        appDetailsSettingsIntent(context)
    }

    context.startActivity(intent)
}

fun openExactAlarmSettings(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
        return
    }

    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
        data = Uri.parse("package:${context.packageName}")
    }

    context.startActivity(intent)
}

private fun notifyNotificationPermissionDenied(
    context: Context,
    activity: Activity?,
    onDenied: () -> Unit,
    onDeniedWithAction: ((PermissionDeniedAction) -> Unit)?
) {
    val action = if (canRequestNotificationPermissionAgain(context, activity)) {
        PermissionDeniedAction.RetryRequest
    } else {
        PermissionDeniedAction.OpenSettings
    }

    onDeniedWithAction?.invoke(action) ?: onDenied()
}

private fun canRequestNotificationPermissionAgain(
    context: Context,
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

private fun appDetailsSettingsIntent(context: Context): Intent {
    return Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.parse("package:${context.packageName}")
    }
}

private tailrec fun Context.findActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}
