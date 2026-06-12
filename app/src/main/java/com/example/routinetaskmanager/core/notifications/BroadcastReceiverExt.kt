package com.example.routinetaskmanager.core.notifications

import android.content.BroadcastReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun BroadcastReceiver.goAsync(
    block : suspend () -> Unit
) {
    val pendingResult = goAsync()

    CoroutineScope(Dispatchers.IO).launch {
        try {
            block()
        } finally {
            pendingResult.finish()
        }
    }
}