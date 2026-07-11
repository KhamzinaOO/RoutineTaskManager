package com.okhamzina.routinetaskmanager.featureReminder.application.session

import com.okhamzina.routinetaskmanager.core.error.ErrorReporter
import com.okhamzina.routinetaskmanager.core.error.AppResult
import com.okhamzina.routinetaskmanager.core.error.runAppResultCatching
import com.okhamzina.routinetaskmanager.core.error.toAppError

class RestoreActiveWorkSessionRuntimeUseCase(
    private val workSessionManager: WorkSessionManager,
    private val runtimeController: WorkSessionRuntimeController,
    private val errorReporter: ErrorReporter
) {

    suspend operator fun invoke(): RestoreWorkSessionRuntimeResult {
        val state = workSessionManager.state.value

        if (!state.isActive || state.startedAtMillis == null) {
            return RestoreWorkSessionRuntimeResult.NotActive
        }

        val wasRescheduled = when (
            val rescheduleResult = runAppResultCatching(errorReporter) {
                workSessionManager.rescheduleActiveSessionIfNeeded()
            }
        ) {
            is AppResult.Error -> return RestoreWorkSessionRuntimeResult.Failed(
                rescheduleResult.error
            )

            is AppResult.Success -> rescheduleResult.data
        }

        if (!wasRescheduled) {
            return RestoreWorkSessionRuntimeResult.NotActive
        }

        return when (val result = runtimeController.start(state.startedAtMillis)) {
            WorkSessionRuntimeStartResult.Started -> {
                RestoreWorkSessionRuntimeResult.Restored
            }

            is WorkSessionRuntimeStartResult.Failed -> {
                RestoreWorkSessionRuntimeResult.Failed(result.throwable.toAppError())
            }
        }
    }
}
