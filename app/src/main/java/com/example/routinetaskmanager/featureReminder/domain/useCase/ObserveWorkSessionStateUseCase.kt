package com.example.routinetaskmanager.featureReminder.domain.useCase

import com.example.routinetaskmanager.featureReminder.application.session.WorkSessionManager
import com.example.routinetaskmanager.featureReminder.application.session.WorkSessionState
import kotlinx.coroutines.flow.Flow

class ObserveWorkSessionStateUseCase(
    private val workSessionManager: WorkSessionManager
) {

    operator fun invoke(): Flow<WorkSessionState> {
        return workSessionManager.state
    }
}