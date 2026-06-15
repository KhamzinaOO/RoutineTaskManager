package com.example.routinetaskmanager.featureReminder.domain.useCase

import android.net.Uri
import com.example.routinetaskmanager.core.coroutines.DispatcherProvider
import com.example.routinetaskmanager.featureReminder.domain.model.NotificationMode
import com.example.routinetaskmanager.featureReminder.domain.model.Reminder
import com.example.routinetaskmanager.featureReminder.domain.model.ReminderRepeatRule
import com.example.routinetaskmanager.featureReminder.domain.model.ReminderSaveData
import com.example.routinetaskmanager.featureReminder.domain.repository.ReminderRepository
import kotlinx.coroutines.withContext

class ReminderCommandUseCase(
    private val reminderRepository: ReminderRepository,
    private val rescheduleRemindersUseCase: RescheduleRemindersUseCase,
    private val workSessionManager: WorkSessionManager,
    private val dispatcherProvider: DispatcherProvider
) {

    suspend fun createReminder(
        data: ReminderSaveData
    ): Long {
        return withContext(dispatcherProvider.io) {
            val reminderId = reminderRepository.createReminder(data)
            rescheduleRemindersUseCase()
            reminderId
        }
    }

    suspend fun updateReminder(
        reminderId: Long,
        data: ReminderSaveData
    ) {
        withContext(dispatcherProvider.io) {
            reminderRepository.updateReminder(
                id = reminderId,
                data = data
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
        return withContext(dispatcherProvider.io){
            reminderRepository.getReminderById(id)
        }
    }

    suspend fun startWorkSession(): WorkSessionState {
        return workSessionManager.startOrRestartSession()
    }

    suspend fun endWorkSession() {
        workSessionManager.endSession()
    }
}
