package com.example.routinetaskmanager.featureReminder.application.session

import com.example.routinetaskmanager.featureReminder.data.session.WorkSessionRuntimeController
import com.example.routinetaskmanager.featureReminder.data.session.WorkSessionRuntimeStartResult

class RestoreActiveWorkSessionRuntimeUseCase(
    private val workSessionManager: WorkSessionManager,
    private val runtimeController: WorkSessionRuntimeController
) {

    suspend operator fun invoke(): RestoreWorkSessionRuntimeResult {
        val state = workSessionManager.state.value

        if (!state.isActive || state.startedAtMillis == null) {
            return RestoreWorkSessionRuntimeResult.NotActive
        }

        return when (val result = runtimeController.start(state.startedAtMillis)) {
            WorkSessionRuntimeStartResult.Started -> {
                RestoreWorkSessionRuntimeResult.Restored
            }

            is WorkSessionRuntimeStartResult.Failed -> {
                RestoreWorkSessionRuntimeResult.Failed(result.throwable)
            }
        }
    }
}