package com.okhamzina.routinetaskmanager.featureReminder.application.schedule

import com.okhamzina.routinetaskmanager.core.coroutines.DispatcherProvider
import com.okhamzina.routinetaskmanager.core.utills.toEpochMillis
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.ReminderOccurrence
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.ReminderOccurrenceStatus
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.schedule.ReminderScheduleCalculator
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.schedule.ScheduleRange
import com.okhamzina.routinetaskmanager.featureReminder.domain.repository.ReminderOccurrenceRepository
import com.okhamzina.routinetaskmanager.featureReminder.domain.repository.ReminderRepository
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