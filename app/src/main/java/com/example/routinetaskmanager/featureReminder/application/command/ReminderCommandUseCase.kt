package com.example.routinetaskmanager.featureReminder.application.command

import com.example.routinetaskmanager.core.coroutines.DispatcherProvider
import com.example.routinetaskmanager.core.utills.toEpochMillis
import com.example.routinetaskmanager.featureReminder.application.notifications.RescheduleRemindersUseCase
import com.example.routinetaskmanager.featureReminder.domain.model.NotificationMode
import com.example.routinetaskmanager.featureReminder.domain.model.Reminder
import com.example.routinetaskmanager.featureReminder.domain.model.ReminderDraft
import com.example.routinetaskmanager.featureReminder.domain.model.ReminderOccurrence
import com.example.routinetaskmanager.featureReminder.domain.model.ReminderOccurrenceStatus
import com.example.routinetaskmanager.featureReminder.domain.model.schedule.ReminderScheduleCalculator
import com.example.routinetaskmanager.featureReminder.domain.model.schedule.ScheduleRange
import com.example.routinetaskmanager.featureReminder.domain.repository.ReminderOccurrenceRepository
import com.example.routinetaskmanager.featureReminder.domain.repository.ReminderRepository
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

    suspend fun completeOccurrence(occurrence: ReminderOccurrence){
        withContext(dispatcherProvider.io){
            occurrenceRepository.upsertState(occurrence.copy(status = ReminderOccurrenceStatus.COMPLETED))
            rescheduleRemindersUseCase()
        }
    }

    suspend fun skipOccurrence(occurrence: ReminderOccurrence){
        withContext(dispatcherProvider.io){
            occurrenceRepository.upsertState(occurrence.copy(status = ReminderOccurrenceStatus.SKIPPED))
            rescheduleRemindersUseCase()
        }
    }

    suspend fun skipRemainingForToday(
        reminderId: Long,
        date: LocalDate
    ) {
        withContext(dispatcherProvider.io) {
            val reminder = reminderRepository.getReminderById(reminderId)
                ?.takeIf { it.isEnabled }
                ?: return@withContext

            val now = LocalDateTime.now()
            val startOfDay = date.atStartOfDay()
            val endOfDay = date.plusDays(1).atStartOfDay()

            val rangeStart = if (now.isAfter(startOfDay)) {
                now
            } else {
                startOfDay
            }

            if (!rangeStart.isBefore(endOfDay)) {
                return@withContext
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