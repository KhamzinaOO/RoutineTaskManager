package com.example.routinetaskmanager.core.notifications

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query


@Dao
interface ScheduledNotificationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(
        entity: ScheduledNotificationEntity
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(
        entities: List<ScheduledNotificationEntity>
    )

    @Query("SELECT * FROM scheduled_notifications")
    suspend fun getAll(): List<ScheduledNotificationEntity>

    @Query(
        """
        SELECT * FROM scheduled_notifications
        WHERE targetType = :targetType
        """
    )
    suspend fun getByTargetType(
        targetType: String
    ): List<ScheduledNotificationEntity>

    @Query(
        """
        SELECT * FROM scheduled_notifications
        WHERE targetType = :targetType
        AND occurrenceKind = :occurrenceKind
        """
    )
    suspend fun getByTargetTypeAndOccurrenceKind(
        targetType: String,
        occurrenceKind: String
    ): List<ScheduledNotificationEntity>

    @Query(
        """
        SELECT * FROM scheduled_notifications
        WHERE targetType = :targetType
        AND occurrenceKey LIKE :occurrenceKeyPrefix || '%'
        """
    )
    suspend fun getByTargetTypeAndOccurrenceKeyPrefix(
        targetType: String,
        occurrenceKeyPrefix: String
    ): List<ScheduledNotificationEntity>

    @Query(
        """
        SELECT * FROM scheduled_notifications
        WHERE targetType = :targetType
        AND targetId = :targetId
        """
    )
    suspend fun getByTarget(
        targetType: String,
        targetId: Long
    ): List<ScheduledNotificationEntity>

    @Query(
        """
        SELECT * FROM scheduled_notifications
        WHERE requestCode = :requestCode
        """
    )
    suspend fun getByRequestCode(
        requestCode: Int
    ): ScheduledNotificationEntity?

    @Query(
        """
        DELETE FROM scheduled_notifications
        WHERE requestCode = :requestCode
        """
    )
    suspend fun deleteByRequestCode(
        requestCode: Int
    )

    @Query(
        """
        DELETE FROM scheduled_notifications
        WHERE targetType = :targetType
        """
    )
    suspend fun deleteByTargetType(
        targetType: String
    )

    @Query(
        """
        DELETE FROM scheduled_notifications
        WHERE targetType = :targetType
        AND occurrenceKind = :occurrenceKind
        """
    )
    suspend fun deleteByTargetTypeAndOccurrenceKind(
        targetType: String,
        occurrenceKind: String
    )

    @Query(
        """
        DELETE FROM scheduled_notifications
        WHERE targetType = :targetType
        AND occurrenceKey LIKE :occurrenceKeyPrefix || '%'
        """
    )
    suspend fun deleteByTargetTypeAndOccurrenceKeyPrefix(
        targetType: String,
        occurrenceKeyPrefix: String
    )

    @Query(
        """
        DELETE FROM scheduled_notifications
        WHERE targetType = :targetType
        AND occurrenceKey NOT LIKE :occurrenceKeyPrefix || '%'
        """
    )
    suspend fun deleteByTargetTypeExceptOccurrenceKeyPrefix(
        targetType: String,
        occurrenceKeyPrefix: String
    )

    @Query(
        """
        DELETE FROM scheduled_notifications
        WHERE targetType = :targetType
        AND targetId = :targetId
        """
    )
    suspend fun deleteByTarget(
        targetType: String,
        targetId: Long
    )

    @Query("DELETE FROM scheduled_notifications")
    suspend fun deleteAll()
}
