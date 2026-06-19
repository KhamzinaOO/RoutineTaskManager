package com.example.routinetaskmanager.featureReminder.application.session

import com.example.routinetaskmanager.featureReminder.domain.useCase.ReminderCommandUseCase
import com.example.routinetaskmanager.featureReminder.domain.useCase.WorkSessionManager

class ToggleWorkSessionUseCase(
    private val reminderCommandUseCase: ReminderCommandUseCase,
    private val workSessionManager: WorkSessionManager,
    private val workSessionRuntimeController: WorkSessionRuntimeController
) {

    suspend operator fun invoke(): ToggleWorkSessionResult {
        return runCatching {
            if (workSessionManager.state.value.isActive) {
                reminderCommandUseCase.endWorkSession()
                workSessionRuntimeController.stop()
                return ToggleWorkSessionResult.Ended
            }

            val wasActive = workSessionManager.state.value.isActive
            val sessionState = reminderCommandUseCase.startWorkSession()
            val startedAtMillis = sessionState.startedAtMillis

            if (startedAtMillis != null) {
                val foregroundStartResult = workSessionRuntimeController.start(startedAtMillis)

                when (foregroundStartResult) {
                    WorkSessionRuntimeStartResult.Started -> Unit

                    is WorkSessionRuntimeStartResult.Failed -> {
                        runCatching {
                            reminderCommandUseCase.endWorkSession()
                        }

                        return ToggleWorkSessionResult.ForegroundStartBlocked
                    }
                }
            }

            if (sessionState.scheduledNotificationCount == 0) {
                ToggleWorkSessionResult.StartedWithoutReminders
            } else {
                ToggleWorkSessionResult.Started(
                    scheduledNotificationCount = sessionState.scheduledNotificationCount,
                    wasRestart = wasActive
                )
            }
        }.getOrElse { throwable ->
            ToggleWorkSessionResult.Failed(throwable)
        }
    }
}