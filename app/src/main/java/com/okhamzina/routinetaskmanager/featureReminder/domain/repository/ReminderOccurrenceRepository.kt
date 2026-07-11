package com.okhamzina.routinetaskmanager.featureReminder.domain.repository

import com.okhamzina.routinetaskmanager.featureReminder.domain.model.ReminderOccurrence
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.ReminderOccurrenceState
import kotlinx.coroutines.flow.Flow

interface ReminderOccurrenceRepository {
    suspend fun upsertState(state: ReminderOccurrence)

    suspend fun getStateByKey(key: String): ReminderOccurrenceState?

    fun observeByReminderAndRange(
        reminderId: Long,
        startMillis: Long,
        endMillis: Long
    ): Flow<List<ReminderOccurrenceState>>

    suspend fun getByReminderAndRange(
        reminderId: Long,
        startMillis: Long,
        endMillis: Long
    ): List<ReminderOccurrenceState>

    fun observeByRange(
        startMillis: Long,
        endMillis: Long
    ): Flow<List<ReminderOccurrenceState>>

    suspend fun getByRange(
        startMillis: Long,
        endMillis: Long
    ): List<ReminderOccurrenceState>

    suspend fun deleteByReminderId(reminderId: Long)
}
