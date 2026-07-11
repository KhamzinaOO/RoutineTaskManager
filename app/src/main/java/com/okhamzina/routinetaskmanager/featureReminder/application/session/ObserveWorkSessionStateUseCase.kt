package com.okhamzina.routinetaskmanager.featureReminder.application.session

import kotlinx.coroutines.flow.Flow

class ObserveWorkSessionStateUseCase(
    private val workSessionManager: WorkSessionManager
) {

    operator fun invoke(): Flow<WorkSessionState> {
        return workSessionManager.state
    }
}