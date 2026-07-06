package com.example.routinetaskmanager.featureReminder.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderOccurrenceDAO {

    @Upsert
    suspend fun upsertState(state: ReminderOccurrenceStateEntity)

    @Query("""
        SELECT * FROM reminder_occurrence_states
        WHERE occurrenceKey = :key 
    """)
    suspend fun getStateByKey(key: String): ReminderOccurrenceStateEntity?

    @Query("""
        SELECT * FROM reminder_occurrence_states
        WHERE reminder_id = :reminderId
        AND scheduledAtMillis >= :startMillis
        AND scheduledAtMillis < :endMillis
    """)
    fun observeByReminderAndRange(
        reminderId: Long,
        startMillis: Long,
        endMillis: Long
    ): Flow<List<ReminderOccurrenceStateEntity>>

    @Query("""
        SELECT * FROM reminder_occurrence_states
        WHERE reminder_id = :reminderId
        AND scheduledAtMillis >= :startMillis
        AND scheduledAtMillis < :endMillis
    """)
    suspend fun getByReminderAndRange(
        reminderId: Long,
        startMillis: Long,
        endMillis: Long
    ): List<ReminderOccurrenceStateEntity>


    @Query("""
        SELECT * FROM reminder_occurrence_states
        WHERE scheduledAtMillis >= :startMillis
        AND scheduledAtMillis < :endMillis
    """)
    fun observeByRange(
        startMillis: Long,
        endMillis: Long
    ): Flow<List<ReminderOccurrenceStateEntity>>

    @Query("""
        SELECT * FROM reminder_occurrence_states
        WHERE scheduledAtMillis >= :startMillis
        AND scheduledAtMillis < :endMillis
    """)
    suspend fun getByRange(
        startMillis: Long,
        endMillis: Long
    ): List<ReminderOccurrenceStateEntity>

    @Query("""
        DELETE FROM reminder_occurrence_states
        WHERE reminder_id = :reminderId
    """)
    suspend fun deleteByReminderId(reminderId: Long)
}
