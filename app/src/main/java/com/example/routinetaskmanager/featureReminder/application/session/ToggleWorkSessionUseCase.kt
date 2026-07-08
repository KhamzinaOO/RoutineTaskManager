package com.example.routinetaskmanager.featureReminder.application.session

import com.example.routinetaskmanager.core.error.runSuspendCatching

class ToggleWorkSessionUseCase(
    private val workSessionManager: WorkSessionManager,
    private val workSessionRuntimeController: WorkSessionRuntimeController
) {

    suspend operator fun invoke(): ToggleWorkSessionResult {
        return if (workSessionManager.state.value.isActive) {
            runSuspendCatching {
                workSessionManager.endSession()
                workSessionRuntimeController.stop()
                ToggleWorkSessionResult.Ended
            }.getOrElse { throwable ->
                ToggleWorkSessionResult.EndFailed(throwable)
            }
        } else {
            runSuspendCatching {
                val sessionState = workSessionManager.startSession()

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
                        runSuspendCatching {
                            workSessionManager.endSession()
                        }
                        ToggleWorkSessionResult.ForegroundStartBlocked
                    }
                }
            }.getOrElse { throwable ->
                ToggleWorkSessionResult.StartFailed(throwable)
            }
        }
    }
}
