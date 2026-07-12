package com.okhamzina.routinetaskmanager.featureReminder.application.command

import com.okhamzina.routinetaskmanager.core.coroutines.DispatcherProvider
import com.okhamzina.routinetaskmanager.core.error.AppError
import com.okhamzina.routinetaskmanager.core.error.AppResult
import com.okhamzina.routinetaskmanager.core.error.EmptyAppResult
import com.okhamzina.routinetaskmanager.core.utills.toEpochMillis
import com.okhamzina.routinetaskmanager.featureReminder.application.notifications.RescheduleRemindersUseCase
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.NotificationMode
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.Reminder
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.ReminderDraft
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.ReminderOccurrence
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.ReminderOccurrenceStatus
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.schedule.ReminderScheduleCalculator
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.schedule.ScheduleRange
import com.okhamzina.routinetaskmanager.featureReminder.domain.repository.ReminderOccurrenceRepository
import com.okhamzina.routinetaskmanager.featureReminder.domain.repository.ReminderRepository
import com.okhamzina.routinetaskmanager.featureReminder.application.session.WorkSessionManager
import com.okhamzina.routinetaskmanager.core.notifications.api.NotificationOccurrenceKind
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime

class ReminderCommandUseCase(
    private val scheduleCalculator: ReminderScheduleCalculator,
    private val reminderRepository: ReminderRepository,
    private val occurrenceRepository: ReminderOccurrenceRepository,
    private val rescheduleRemindersUseCase: RescheduleRemindersUseCase,
    private val workSessionManager: WorkSessionManager,
    private val dispatcherProvider: DispatcherProvider
) {

    fun observeReminders() : Flow<List<Reminder>> {
        return reminderRepository.observeReminders()
            .distinctUntilChanged()
            .flowOn(dispatcherProvider.io)
    }
    suspend fun createReminder(
        draft: ReminderDraft
    ): EmptyAppResult<AppError> {
        return withContext(dispatcherProvider.io) {
            reminderRepository.createReminder(draft)
            AppResult.Success(Unit)
        }
    }

    suspend fun updateReminder(
        reminderId: Long,
        draft: ReminderDraft
    ): EmptyAppResult<AppError> {
        return withContext(dispatcherProvider.io) {
            reminderRepository.updateReminder(
                reminderId = reminderId,
                draft = draft
            )
            AppResult.Success(Unit)
        }
    }

    suspend fun deleteReminder(
        reminderId: Long
    ): EmptyAppResult<AppError> {
        return withContext(dispatcherProvider.io) {
            reminderRepository.deleteReminder(reminderId)

            rescheduleRemindersUseCase()
        }
    }

    suspend fun setReminderEnabled(
        reminderId: Long,
        enabled: Boolean
    ): EmptyAppResult<AppError> {
        return withContext(dispatcherProvider.io) {
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
    ): EmptyAppResult<AppError> {
        return withContext(dispatcherProvider.io) {
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
    ): EmptyAppResult<AppError> {
        return withContext(dispatcherProvider.io) {
            reminderRepository.updateNotificationMode(
                reminderId = reminderId,
                notificationMode = notificationMode
            )

            rescheduleRemindersUseCase()
        }
    }

    suspend fun rescheduleReminderNotifications(): EmptyAppResult<AppError> {
        return withContext(dispatcherProvider.io) {
            rescheduleRemindersUseCase()
        }
    }

    private suspend fun rescheduleAfterOccurrenceChange(
        occurrenceKind: NotificationOccurrenceKind
    ): EmptyAppResult<AppError> {
        return when (occurrenceKind) {
            NotificationOccurrenceKind.REGULAR -> rescheduleRemindersUseCase()
            NotificationOccurrenceKind.SESSION -> when (
                val result = workSessionManager.rescheduleActiveSessionIfNeeded()
            ) {
                is AppResult.Error -> AppResult.Error(result.error)
                is AppResult.Success -> AppResult.Success(Unit)
            }
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

    suspend fun completeOccurrence(
        occurrence: ReminderOccurrence
    ): EmptyAppResult<AppError> {
        return withContext(dispatcherProvider.io){
            occurrenceRepository.upsertState(occurrence.copy(status = ReminderOccurrenceStatus.COMPLETED))
            rescheduleAfterOccurrenceChange(occurrence.occurrenceKind)
        }
    }

    suspend fun skipOccurrence(
        occurrence: ReminderOccurrence
    ): EmptyAppResult<AppError> {
        return withContext(dispatcherProvider.io){
            occurrenceRepository.upsertState(occurrence.copy(status = ReminderOccurrenceStatus.SKIPPED))
            rescheduleAfterOccurrenceChange(occurrence.occurrenceKind)
        }
    }

    suspend fun skipRemainingForToday(
        reminderId: Long,
        date: LocalDate
    ): EmptyAppResult<AppError> {
        return withContext(dispatcherProvider.io) {
            val reminder = reminderRepository.getReminderById(reminderId)
                ?.takeIf { it.isEnabled }
                ?: return@withContext AppResult.Success(Unit)

            val now = LocalDateTime.now()
            val startOfDay = date.atStartOfDay()
            val endOfDay = date.plusDays(1).atStartOfDay()

            val rangeStart = if (now.isAfter(startOfDay)) {
                now
            } else {
                startOfDay
            }

            if (!rangeStart.isBefore(endOfDay)) {
                return@withContext AppResult.Success(Unit)
            }

            val range = ScheduleRange(
                start = rangeStart,
                endExclusive = endOfDay
            )

            val existingStatesByKey = occurrenceRepository.getByReminderAndRange(
                reminderId = reminderId,
                startMillis = range.start.toEpochMillis(),
                endMillis = range.endExclusive.toEpochMillis()
            ).associateBy { state ->
                state.occurrenceKey
            }

            scheduleCalculator.buildOccurrencesForReminder(
                reminder = reminder,
                range = range
            )
                .filter { occurrence ->
                    val existingState = existingStatesByKey[occurrence.occurrenceKey]

                    existingState == null ||
                            existingState.status == ReminderOccurrenceStatus.PLANNED
                }
                .forEach { occurrence ->
                    occurrenceRepository.upsertState(
                        occurrence.copy(
                            status = ReminderOccurrenceStatus.SKIPPED
                        )
                    )
                }

            rescheduleRemindersUseCase()
        }
    }
}
