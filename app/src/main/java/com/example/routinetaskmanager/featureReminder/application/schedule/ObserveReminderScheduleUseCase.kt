package com.example.routinetaskmanager.featureReminder.application.schedule

import com.example.routinetaskmanager.core.coroutines.DispatcherProvider
import com.example.routinetaskmanager.core.utills.toEpochMillis
import com.example.routinetaskmanager.featureReminder.domain.model.ReminderOccurrence
import com.example.routinetaskmanager.featureReminder.domain.model.schedule.ReminderScheduleCalculator
import com.example.routinetaskmanager.featureReminder.domain.model.schedule.ScheduleRange
import com.example.routinetaskmanager.featureReminder.domain.repository.ReminderOccurrenceRepository
import com.example.routinetaskmanager.featureReminder.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class ObserveReminderScheduleUseCase(
    private val dispatcherProvider: DispatcherProvider,
    private val reminderRepository: ReminderRepository,
    private val occurrenceRepository: ReminderOccurrenceRepository,
    private val scheduleCalculator: ReminderScheduleCalculator
) {

    operator fun invoke(
        range: ScheduleRange
    ): Flow<List<ReminderOccurrence>> {
        return combine(
            reminderRepository.observeReminders(),
            occurrenceRepository.observeByRange(
                startMillis = range.start.toEpochMillis(),
                endMillis = range.endExclusive.toEpochMillis()
            )
        ){ reminders, states ->
            val occurrences = scheduleCalculator.buildOccurrences(
                reminders = reminders.filter { it.isEnabled },
                range = range
            )

            val statesByKey = states.associateBy { state ->
                state.occurrenceKey
            }

            occurrences.map { occurrence ->
                val state = statesByKey[occurrence.occurrenceKey]
                if(state == null){
                    occurrence
                }else{
                    occurrence.copy(
                        status = state.status
                    )
                }
            }
        }.flowOn(dispatcherProvider.default)
    }
}