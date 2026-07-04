package com.example.routinetaskmanager.featureReminder.application.command

import com.example.routinetaskmanager.core.coroutines.DispatcherProvider
import com.example.routinetaskmanager.featureReminder.application.notifications.RescheduleRemindersUseCase
import com.example.routinetaskmanager.featureReminder.domain.model.NotificationMode
import com.example.routinetaskmanager.featureReminder.domain.model.Reminder
import com.example.routinetaskmanager.featureReminder.domain.model.ReminderDraft
import com.example.routinetaskmanager.featureReminder.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

class ReminderCommandUseCase(
    private val reminderRepository: ReminderRepository,
    private val rescheduleRemindersUseCase: RescheduleRemindersUseCase,
    private val dispatcherProvider: DispatcherProvider
) {

    fun observeReminders() : Flow<List<Reminder>> {
        return reminderRepository.observeReminders()
            .distinctUntilChanged()
            .flowOn(dispatcherProvider.io)
    }
    suspend fun createReminder(
        draft: ReminderDraft
    ): Long {
        return withContext(dispatcherProvider.io) {
            val reminderId = reminderRepository.createReminder(draft)
            rescheduleRemindersUseCase()
            reminderId
        }
    }

    suspend fun updateReminder(
        reminderId: Long,
        draft: ReminderDraft
    ) {
        withContext(dispatcherProvider.io) {
            reminderRepository.updateReminder(
                reminderId = reminderId,
                draft = draft
            )
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

    suspend fun rescheduleReminderNotifications() {
        withContext(dispatcherProvider.io) {
            rescheduleRemindersUseCase()
        }
    }

    suspend fun getReminderById(id : Long) : Reminder?{
        return withContext(dispatcherProvider.io) {
            reminderRepository.getReminderById(id)
        }
    }

    fun observeReminderById(id: Long) : Flow<Reminder?>{
        return reminderRepository.observeReminderById(reminderId = id)
            .distinctUntilChanged()
            .flowOn(dispatcherProvider.io)
    }
}