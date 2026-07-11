package com.okhamzina.routinetaskmanager.featureReminder.domain.repository

import com.okhamzina.routinetaskmanager.featureReminder.domain.model.NotificationMode
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.Reminder
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.ReminderDraft
import kotlinx.coroutines.flow.Flow

interface ReminderRepository {
    suspend fun getAllRemindersSnapshot(): List<Reminder>

    fun observeReminders(): Flow<List<Reminder>>

    fun observeReminderById(reminderId: Long): Flow<Reminder?>

    suspend fun getReminderById(reminderId: Long): Reminder?

    suspend fun createReminder(draft: ReminderDraft): Long

    suspend fun updateReminder(
        reminderId: Long,
        draft: ReminderDraft
    )

    suspend fun deleteReminder(reminderId: Long)

    suspend fun setReminderEnabled(reminderId: Long, enabled: Boolean)

    suspend fun setNotificationEnabled(reminderId: Long, enabled: Boolean)

    suspend fun updateNotificationMode(
        reminderId: Long,
        notificationMode: NotificationMode
    )
}
