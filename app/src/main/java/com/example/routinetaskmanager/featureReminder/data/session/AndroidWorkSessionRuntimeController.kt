package com.example.routinetaskmanager.featureReminder.data.session

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