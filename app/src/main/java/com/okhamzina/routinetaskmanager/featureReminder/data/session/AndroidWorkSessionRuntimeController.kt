package com.okhamzina.routinetaskmanager.featureReminder.data.session

import com.okhamzina.routinetaskmanager.featureReminder.application.session.WorkSessionRuntimeController
import com.okhamzina.routinetaskmanager.featureReminder.application.session.WorkSessionRuntimeStartResult

class AndroidWorkSessionRuntimeController(
    private val foregroundController: WorkSessionForegroundController
) : WorkSessionRuntimeController {

    override suspend fun start(startedAtMillis: Long): WorkSessionRuntimeStartResult {
        return when (val result = foregroundController.start(startedAtMillis)) {
            WorkSessionForegroundStartResult.Started -> {
                WorkSessionRuntimeStartResult.Started
            }

            is WorkSessionForegroundStartResult.Failed -> {
                WorkSessionRuntimeStartResult.Failed(result.throwable)
            }
        }
    }

    override fun stop() {
        foregroundController.stop()
    }
}