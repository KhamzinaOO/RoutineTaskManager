package com.okhamzina.routinetaskmanager.featureReminder.application.session

interface WorkSessionStateStore {
    fun load(): PersistedWorkSessionState?

    fun save(state: PersistedWorkSessionState)

    fun clear()
}

data class PersistedWorkSessionState(
    val startedAtMillis: Long,
    val expiresAtMillis: Long?
)
