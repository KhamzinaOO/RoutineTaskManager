package com.example.routinetaskmanager.featureReminder.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {

    @Transaction
    @Query(
        """
        SELECT * FROM reminders
        ORDER BY createdAt DESC
        """
    )
    fun observeRemindersWithImages(): Flow<List<ReminderWithImages>>

    @Transaction
    @Query(
        """
        SELECT * FROM reminders
        WHERE id = :reminderId
        """
    )
    fun observeReminderWithImages(
        reminderId: Long
    ): Flow<ReminderWithImages?>

    @Transaction
    @Query(
        """
        SELECT * FROM reminders
        WHERE id = :reminderId
        """
    )
    suspend fun getReminderWithImagesById(
        reminderId: Long
    ): ReminderWithImages?

    @Query(
        """
        SELECT * FROM reminders
        WHERE id = :reminderId
        """
    )
    suspend fun getReminderById(
        reminderId: Long
    ): ReminderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(
        reminder: ReminderEntity
    ): Long

    @Update
    suspend fun updateReminder(
        reminder: ReminderEntity
    )

    @Query(
        """
        DELETE FROM reminders
        WHERE id = :reminderId
        """
    )
    suspend fun deleteReminderById(
        reminderId: Long
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminderImage(
        image: ReminderImageEntity
    ): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminderImages(
        images: List<ReminderImageEntity>
    )

    @Query(
        """
        SELECT * FROM reminder_images
        WHERE reminder_id = :reminderId
        ORDER BY sortOrder ASC
        """
    )
    suspend fun getImagesByReminderId(
        reminderId: Long
    ): List<ReminderImageEntity>

    @Query(
        """
        SELECT * FROM reminder_images
        WHERE id = :imageId
        """
    )
    suspend fun getImageById(
        imageId: Long
    ): ReminderImageEntity?

    @Query(
        """
        DELETE FROM reminder_images
        WHERE id = :imageId
        """
    )
    suspend fun deleteImageById(
        imageId: Long
    )

    @Query(
        """
        DELETE FROM reminder_images
        WHERE reminder_id = :reminderId
        """
    )
    suspend fun deleteImagesByReminderId(
        reminderId: Long
    )

    @Transaction
    @Query("SELECT * FROM reminders ORDER BY createdAt DESC")
    suspend fun getRemindersWithImagesSnapshot(): List<ReminderWithImages>

    @Query(
        """
    UPDATE reminders
    SET isEnabled = :enabled,
        updatedAt = :updatedAt
    WHERE id = :reminderId
    """
    )
    suspend fun updateReminderEnabled(
        reminderId: Long,
        enabled: Boolean,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query(
        """
    UPDATE reminders
    SET notificationEnabled = :enabled,
        updatedAt = :updatedAt
    WHERE id = :reminderId
    """
    )
    suspend fun updateNotificationEnabled(
        reminderId: Long,
        enabled: Boolean,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query(
        """
    UPDATE reminders
    SET notificationMode = :notificationMode,
        updatedAt = :updatedAt
    WHERE id = :reminderId
    """
    )
    suspend fun updateNotificationMode(
        reminderId: Long,
        notificationMode: String,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Update
    suspend fun updateReminderImages(
        images: List<ReminderImageEntity>
    )
}