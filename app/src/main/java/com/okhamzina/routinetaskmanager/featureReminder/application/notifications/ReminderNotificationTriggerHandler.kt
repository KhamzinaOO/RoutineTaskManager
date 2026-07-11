package com.okhamzina.routinetaskmanager.featureReminder.application.notifications

import com.okhamzina.routinetaskmanager.core.error.AppError
import com.okhamzina.routinetaskmanager.core.error.AppResult
import com.okhamzina.routinetaskmanager.core.error.EmptyAppResult
import com.okhamzina.routinetaskmanager.core.error.ErrorReporter
import com.okhamzina.routinetaskmanager.core.error.runAppResultCatching
import com.okhamzina.routinetaskmanager.core.notifications.api.NotificationPayload
import com.okhamzina.routinetaskmanager.core.notifications.api.NotificationTriggerHandler
import com.okhamzina.routinetaskmanager.core.notifications.api.NotificationOccurrenceKind
import com.okhamzina.routinetaskmanager.core.notifications.api.NotificationTargetType
import com.okhamzina.routinetaskmanager.core.notifications.domain.ScheduledNotificationRepository
import com.okhamzina.routinetaskmanager.core.notifications.toReminderChannelId
import com.okhamzina.routinetaskmanager.featureReminder.domain.repository.ReminderRepository
import com.okhamzina.routinetaskmanager.featureReminder.application.session.WorkSessionManager

class ReminderNotificationTriggerHandler(
    private val reminderRepository: ReminderRepository,
    private val scheduledNotificationRepository: ScheduledNotificationRepository,
    private val rescheduleRemindersUseCase: RescheduleRemindersUseCase,
    private val workSessionManager: WorkSessionManager,
    private val errorReporter: ErrorReporter
) : NotificationTriggerHandler {

    override suspend fun buildPayloadOrNull(
        targetId: Long,
        scheduledAtMillis: Long,
        occurrenceKind: NotificationOccurrenceKind
    ): NotificationPayload? {
        val reminder = reminderRepository.getReminderById(targetId)
            ?: return null

        if (!reminder.isEnabled || !reminder.notificationEnabled) {
            scheduledNotificationRepository.deleteByTarget(
                targetType = NotificationTargetType.REMINDER,
                targetId = targetId
            )
            return null
        }

        return NotificationPayload(
            targetType = NotificationTargetType.REMINDER,
            targetId = reminder.id,
            title = reminder.name,
            text = reminder.instructionsText,
            scheduledAtMillis = scheduledAtMillis,
            channelId = reminder.notificationMode.toReminderChannelId()
        )
    }

    override suspend fun onNotificationShown(
        targetId: Long,
        scheduledAtMillis: Long,
        occurrenceKind: NotificationOccurrenceKind
    ) {
        runAppResultCatching(errorReporter) {
            rescheduleByOccurrenceKind(occurrenceKind)
        }
    }

    private suspend fun rescheduleByOccurrenceKind(
        occurrenceKind: NotificationOccurrenceKind
    ): EmptyAppResult<AppError> {
        return when (occurrenceKind) {
            NotificationOccurrenceKind.REGULAR -> {
                rescheduleRemindersUseCase()
            }

            NotificationOccurrenceKind.SESSION -> {
                when (val result = workSessionManager.rescheduleActiveSessionIfNeeded()) {
                    is AppResult.Error -> AppResult.Error(result.error)
                    is AppResult.Success -> AppResult.Success(Unit)
                }
            }
        }
    }
}
