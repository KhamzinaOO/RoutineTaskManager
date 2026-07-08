package com.example.routinetaskmanager.featureReminder.data.session

import android.content.Context
import androidx.core.content.edit
import com.example.routinetaskmanager.featureReminder.application.session.PersistedWorkSessionState
import com.example.routinetaskmanager.featureReminder.application.session.WorkSessionStateStore

class SharedPrefsWorkSessionStateStore(
    context: Context
) : WorkSessionStateStore {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun load(): PersistedWorkSessionState? {
        val startedAtMillis = prefs.getLong(KEY_STARTED_AT_MILLIS, 0L)
            .takeIf { it > 0L }
            ?: return null

        val expiresAtMillis = prefs.getLong(KEY_EXPIRES_AT_MILLIS, 0L)
            .takeIf { it > 0L }

        return PersistedWorkSessionState(
            startedAtMillis = startedAtMillis,
            expiresAtMillis = expiresAtMillis
        )
    }

    override fun save(state: PersistedWorkSessionState) {
        prefs.edit {
            putLong(KEY_STARTED_AT_MILLIS, state.startedAtMillis)

            if (state.expiresAtMillis != null) {
                putLong(KEY_EXPIRES_AT_MILLIS, state.expiresAtMillis)
            } else {
                remove(KEY_EXPIRES_AT_MILLIS)
            }
        }
    }

    override fun clear() {
        prefs.edit {
            remove(KEY_STARTED_AT_MILLIS)
            remove(KEY_EXPIRES_AT_MILLIS)
        }
    }

    private companion object {
        const val PREFS_NAME = "work_session"
        const val KEY_STARTED_AT_MILLIS = "started_at_millis"
        const val KEY_EXPIRES_AT_MILLIS = "expires_at_millis"
    }
}
