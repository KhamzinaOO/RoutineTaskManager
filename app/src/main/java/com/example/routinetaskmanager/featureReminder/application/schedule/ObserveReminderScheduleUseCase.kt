package com.example.routinetaskmanager.featureReminder.application.schedule

import com.example.routinetaskmanager.core.coroutines.DispatcherProvider
import com.example.routinetaskmanager.featureReminder.domain.model.ReminderOccurrence
import com.example.routinetaskmanager.featureReminder.domain.model.schedule.ReminderScheduleCalculator
import com.example.routinetaskmanager.featureReminder.domain.model.schedule.ScheduleRange
import com.example.routinetaskmanager.featureReminder.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class ObserveReminderScheduleUseCase(
    private val dispatcherProvider: DispatcherProvider,
    private val reminderRepository: ReminderRepository,
    private val scheduleCalculator: ReminderScheduleCalculator
) {

    operator fun invoke(
        range: ScheduleRange
    ): Flow<List<ReminderOccurrence>> {
        return reminderRepository.observeReminders()
            .map { reminders ->
                scheduleCalculator.buildOccurrences(
                    reminders = reminders.filter { it.isEnabled },
                    range = range
                )
            }
            .flowOn(dispatcherProvider.default)
    }
}