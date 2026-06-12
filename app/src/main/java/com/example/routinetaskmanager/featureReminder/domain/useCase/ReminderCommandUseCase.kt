package com.example.routinetaskmanager.featureReminder.domain.useCase

import android.net.Uri
import com.example.routinetaskmanager.core.coroutines.DispatcherProvider
import com.example.routinetaskmanager.featureReminder.domain.model.NotificationMode
import com.example.routinetaskmanager.featureReminder.domain.model.Reminder
import com.example.routinetaskmanager.featureReminder.domain.model.ReminderRepeatRule
import com.example.routinetaskmanager.featureReminder.domain.repository.ReminderRepository
import kotlinx.coroutines.withContext

class ReminderCommandUseCase(
    private val reminderRepository: ReminderRepository,
    private val rescheduleRemindersUseCase: RescheduleRemindersUseCase,
    private val dispatcherProvider: DispatcherProvider
) {

    suspend fun createReminder(
        name: String,
        instructionsText: String?,
        repeatRule: ReminderRepeatRule,
        notificationMode: NotificationMode,
        imageUris: List<Uri>
    ): Long {
        return withContext(dispatcherProvider.io) {
            val reminderId = reminderRepository.createReminder(
                name = name,
                instructionsText = instructionsText,
                repeatRule = repeatRule,
                notificationMode = notificationMode,
                imageUris = imageUris
            )

            rescheduleRemindersUseCase()

            reminderId
        }
    }

    suspend fun updateReminder(
        reminder: Reminder
    ) {
        withContext(dispatcherProvider.io) {
            reminderRepository.updateReminder(reminder)

            rescheduleRemindersUseCase()
        }
    }

    suspend fun deleteReminder(
        reminderId: Long
    ) {
        withContext(dispatcherProvider.io) {
            reminderRepository.deleteReminder(reminderId)

            rescheduleRemindersUseCase()
        }
    }

    suspend fun setReminderEnabled(
        reminderId: Long,
        enabled: Boolean
    ) {
        withContext(dispatcherProvider.io) {
            reminderRepository.setReminderEnabled(
                reminderId = reminderId,
                enabled = enabled
            )

            rescheduleRemindersUseCase()
        }
    }

    suspend fun setNotificationEnabled(
        reminderId: Long,
        enabled: Boolean
    ) {
        withContext(dispatcherProvider.io) {
            reminderRepository.setNotificationEnabled(
                reminderId = reminderId,
                enabled = enabled
            )

            rescheduleRemindersUseCase()
        }
    }

    suspend fun updateNotificationMode(
        reminderId: Long,
        notificationMode: NotificationMode
    ) {
        withContext(dispatcherProvider.io) {
            reminderRepository.updateNotificationMode(
                reminderId = reminderId,
                notificationMode = notificationMode
            )

            rescheduleRemindersUseCase()
        }
    }
}