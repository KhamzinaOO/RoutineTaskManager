package com.example.routinetaskmanager.featureReminder.domain.repository

import android.net.Uri
import com.example.routinetaskmanager.featureReminder.domain.model.NotificationMode
import com.example.routinetaskmanager.featureReminder.domain.model.Reminder
import com.example.routinetaskmanager.featureReminder.domain.model.ReminderRepeatRule
import com.example.routinetaskmanager.featureReminder.domain.model.ReminderSaveData
import kotlinx.coroutines.flow.Flow

interface ReminderRepository {
    suspend fun getAllRemindersSnapshot(): List<Reminder>
    fun observeReminders(): Flow<List<Reminder>>

    fun observeReminderById(
        reminderId: Long
    ): Flow<Reminder?>

    suspend fun getReminderById(
        reminderId: Long
    ): Reminder?

    suspend fun createReminder(data: ReminderSaveData): Long

    suspend fun updateReminder(
        id: Long,
        data: ReminderSaveData
    )
    suspend fun deleteReminder(
        reminderId: Long
    )

    suspend fun addImageToReminder(
        reminderId: Long,
        imageUri: Uri
    )

    suspend fun deleteImage(
        imageId: Long
    )


    suspend fun setReminderEnabled(reminderId: Long, enabled: Boolean)
    suspend fun setNotificationEnabled(reminderId: Long, enabled: Boolean)
    suspend fun updateNotificationMode(reminderId: Long, notificationMode: NotificationMode)
}