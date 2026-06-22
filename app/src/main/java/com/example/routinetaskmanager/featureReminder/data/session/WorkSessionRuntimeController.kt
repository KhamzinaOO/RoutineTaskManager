package com.example.routinetaskmanager.featureReminder.data.session

interface WorkSessionRuntimeController {
    suspend fun start(startedAtMillis: Long): WorkSessionRuntimeStartResult
    fun stop()
}

sealed interface WorkSessionRuntimeStartResult {
    data object Started : WorkSessionRuntimeStartResult
    data class Failed(val throwable: Throwable) : WorkSessionRuntimeStartResult
}