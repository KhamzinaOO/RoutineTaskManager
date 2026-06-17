package com.example.routinetaskmanager.core.notifications

import android.content.BroadcastReceiver
import android.util.Log
import com.example.routinetaskmanager.core.coroutines.DefaultDispatcherProvider
import com.example.routinetaskmanager.core.coroutines.DispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.milliseconds

private const val RECEIVER_TIMEOUT_MILLIS = 9_000L
private const val TAG = "BroadcastReceiverExt"

fun BroadcastReceiver.goAsync(
    dispatcher: DispatcherProvider,
    timeoutMillis: Long = RECEIVER_TIMEOUT_MILLIS,
    block: suspend () -> Unit
) {
    val pendingResult = goAsync()

    CoroutineScope(SupervisorJob() + dispatcher.io).launch {
        try {
            withTimeout(timeoutMillis.milliseconds) {
                block()
            }
        } catch (throwable: Throwable) {
            Log.e(TAG, "Failed to handle async broadcast", throwable)
        } finally {
            pendingResult.finish()
        }
    }
}