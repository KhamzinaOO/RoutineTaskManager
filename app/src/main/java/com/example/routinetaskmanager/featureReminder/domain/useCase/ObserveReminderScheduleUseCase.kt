package com.example.routinetaskmanager.featureReminder.domain.useCase

import com.example.routinetaskmanager.core.coroutines.DispatcherProvider
import com.example.routinetaskmanager.featureReminder.domain.model.ReminderOccurrence
import com.example.routinetaskmanager.featureReminder.domain.model.schedule.ReminderScheduleCalculator
import com.example.routinetaskmanager.featureReminder.domain.model.schedule.ScheduleRange
import com.example.routinetaskmanager.featureReminder.domain.repository.ReminderRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class ObserveReminderScheduleUseCase(
    private val dispatcherProvider: DispatcherProvider,
    private val reminderRepository: ReminderRepository,
    private val scheduleCalculator: ReminderScheduleCalculator
) {

    suspend operator fun invoke(
        range: ScheduleRange
    ): Flow<List<ReminderOccurrence>> {
        return withContext(dispatcherProvider.io) {
            reminderRepository.observeReminders()
                .map { reminders ->
                    scheduleCalculator.buildOccurrences(
                        reminders = reminders,
                        range = range
                    )
                }
        }
    }
}