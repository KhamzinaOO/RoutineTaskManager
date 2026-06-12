package com.example.routinetaskmanager.core.notifications

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ScheduledNotificationDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(
        entity: ScheduledNotificationEntity
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(
        entities : List<ScheduledNotificationEntity>
    )

    @Query("SELECT * FROM scheduled_notifications")
    suspend fun getAll() : List<ScheduledNotificationEntity>

    @Query("""SELECT * FROM scheduled_notifications
            WHERE targetType = :targetType""")
    suspend fun getByTargetType(
        targetType : NotificationTargetType
    ): List<ScheduledNotificationEntity>

//    @Query("""
//        SELECT * FROM scheduled_notifications
//        WHERE targetType = :targetType
//        AND targetId = :targetId
//    """)
//    suspend fun getByTarget(
//        targetType : NotificationTargetType,
//        targetId : Long
//    )

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