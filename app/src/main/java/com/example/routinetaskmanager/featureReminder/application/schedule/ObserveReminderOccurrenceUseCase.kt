package com.example.routinetaskmanager.featureReminder.application.schedule

import com.example.routinetaskmanager.core.coroutines.DispatcherProvider
import com.example.routinetaskmanager.core.utills.toEpochMillis
import com.example.routinetaskmanager.featureReminder.domain.model.ReminderOccurrence
import com.example.routinetaskmanager.featureReminder.domain.model.ReminderOccurrenceStatus
import com.example.routinetaskmanager.featureReminder.domain.model.schedule.ReminderScheduleCalculator
import com.example.routinetaskmanager.featureReminder.domain.model.schedule.ScheduleRange
import com.example.routinetaskmanager.featureReminder.domain.repository.ReminderOccurrenceRepository
import com.example.routinetaskmanager.featureReminder.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class ObserveReminderOccurrenceUseCase(
    private val dispatcherProvider: DispatcherProvider,
    private val reminderRepository: ReminderRepository,
    private val occurrenceRepository: ReminderOccurrenceRepository,
    private val scheduleCalculator: ReminderScheduleCalculator
) {
    operator fun invoke(
        reminderId: Long,
        range: ScheduleRange
    ): Flow<ReminderOccurrence?> {
        return combine(
            reminderRepository.observeReminderById(reminderId),
            occurrenceRepository.observeByReminderAndRange(
                reminderId = reminderId,
                startMillis = range.start.toEpochMillis(),
                endMillis = range.endExclusive.toEpochMillis()
            )

        ){ reminder, states ->
            val enabledReminder = reminder?.takeIf { it.isEnabled }
                ?: return@combine null

            val statesByKey = states.associateBy { state ->
                state.occurrenceKey
            }

            scheduleCalculator.buildOccurrencesForReminder(
                reminder = enabledReminder,
                range = range
            )
                .sortedBy { occurrence ->
                    occurrence.scheduledAt
                }
                .map { occurrence ->
                    val state = statesByKey[occurrence.occurrenceKey]

                    if (state == null) {
                        occurrence
                    } else {
                        occurrence.copy(
                            status = state.status
                        )
                    }
                }
                .firstOrNull { occurrence ->
                    occurrence.status == ReminderOccurrenceStatus.PLANNED
                }
        }.flowOn(dispatcherProvider.default)
    }
}