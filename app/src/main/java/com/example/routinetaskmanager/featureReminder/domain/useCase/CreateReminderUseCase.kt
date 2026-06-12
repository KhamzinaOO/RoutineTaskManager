package com.example.routinetaskmanager.featureReminder.domain.useCase

import android.net.Uri
import com.example.routinetaskmanager.featureReminder.domain.model.NotificationMode
import com.example.routinetaskmanager.featureReminder.domain.model.ReminderRepeatRule
import com.example.routinetaskmanager.featureReminder.domain.repository.ReminderRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CreateReminderUseCase(
    private val reminderRepository: ReminderRepository,
    private val rescheduleRemindersUseCase: RescheduleRemindersUseCase
) {

    suspend operator fun invoke(
        name: String,
        instructionsText: String?,
        repeatRule: ReminderRepeatRule,
        notificationMode: NotificationMode,
        imageUris: List<Uri>
    ) {
        withContext(Dispatchers.IO){
            reminderRepository.createReminder(
                name = name,
                instructionsText = instructionsText,
                repeatRule = repeatRule,
                notificationMode = notificationMode,
                imageUris = imageUris
            )

        }
        rescheduleRemindersUseCase()
    }
}