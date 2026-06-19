package com.example.routinetaskmanager.featureReminder.domain.useCase

import kotlinx.coroutines.flow.Flow

class ObserveWorkSessionStateUseCase(
    private val workSessionManager: WorkSessionManager
) {

    operator fun invoke(): Flow<WorkSessionState> {
        return workSessionManager.state
    }
}