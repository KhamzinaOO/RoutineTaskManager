package com.okhamzina.routinetaskmanager.featureReminder.application.session

import com.okhamzina.routinetaskmanager.core.error.ErrorReporter
import com.okhamzina.routinetaskmanager.core.error.AppResult
import com.okhamzina.routinetaskmanager.core.error.runAppResultCatching
import com.okhamzina.routinetaskmanager.core.error.runSuspendCatching
import com.okhamzina.routinetaskmanager.core.error.toAppError

class ToggleWorkSessionUseCase(
    private val workSessionManager: WorkSessionManager,
    private val workSessionRuntimeController: WorkSessionRuntimeController,
    private val errorReporter: ErrorReporter
) {

    suspend operator fun invoke(): ToggleWorkSessionResult {
        return if (workSessionManager.state.value.isActive) {
            runSuspendCatching(errorReporter) {
                workSessionManager.endSession()
                workSessionRuntimeController.stop()
                ToggleWorkSessionResult.Ended
            }.getOrElse { throwable ->
                ToggleWorkSessionResult.EndFailed(throwable.toAppError())
            }
        } else {
            runSuspendCatching(errorReporter) {
                when (
                    val sessionResult = runAppResultCatching(errorReporter) {
                        workSessionManager.startSession()
                    }
                ) {
                    is AppResult.Error -> {
                        ToggleWorkSessionResult.StartFailed(sessionResult.error)
                    }

                    is AppResult.Success -> {
                        val sessionState = sessionResult.data

                        when (workSessionRuntimeController.start(requireNotNull(sessionState.startedAtMillis))) {
                            WorkSessionRuntimeStartResult.Started -> {
                                if (sessionState.scheduledNotificationCount == 0) {
                                    ToggleWorkSessionResult.StartedWithoutReminders
                                } else {
                                    ToggleWorkSessionResult.Started(
                                        scheduledNotificationCount = sessionState.scheduledNotificationCount
                                    )
                                }
                            }

                            is WorkSessionRuntimeStartResult.Failed -> {
                                runSuspendCatching(errorReporter) {
                                    workSessionManager.endSession()
                                }
                                ToggleWorkSessionResult.ForegroundStartBlocked
                            }
                        }
                    }
                }
            }.getOrElse { throwable ->
                ToggleWorkSessionResult.StartFailed(throwable.toAppError())
            }
        }
    }
}
