package com.example.routinetaskmanager.featureReminder.domain.repository

import android.net.Uri
import com.example.routinetaskmanager.featureReminder.domain.model.NotificationMode
import com.example.routinetaskmanager.featureReminder.domain.model.Reminder
import com.example.routinetaskmanager.featureReminder.domain.model.ReminderRepeatRule
import kotlinx.coroutines.flow.Flow

interface ReminderRepository {

    fun observeReminders(): Flow<List<Reminder>>

    fun observeReminderById(
        reminderId: Long
    ): Flow<Reminder?>

    suspend fun getReminderById(
        reminderId: Long
    ): Reminder?

    suspend fun createReminder(
        name: String,
        instructionsText: String?,
        repeatRule: ReminderRepeatRule,
        notificationMode: NotificationMode,
        imageUris: List<Uri>
    ): Long

    suspend fun updateReminder(
        reminderId: Long,
        name: String,
        instructionsText: String?,
        repeatRule: ReminderRepeatRule,
        notificationMode: NotificationMode
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
}