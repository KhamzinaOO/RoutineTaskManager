package com.example.routinetaskmanager.featureReminder.domain.useCase

import com.example.routinetaskmanager.featureReminder.domain.model.ReminderOccurrence
import com.example.routinetaskmanager.featureReminder.domain.model.schedule.ReminderScheduleCalculator
import com.example.routinetaskmanager.featureReminder.domain.model.schedule.ScheduleRange
import com.example.routinetaskmanager.featureReminder.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObserveReminderScheduleUseCase(
    private val reminderRepository: ReminderRepository,
    private val scheduleCalculator: ReminderScheduleCalculator
) {

    operator fun invoke(
        range: ScheduleRange
    ): Flow<List<ReminderOccurrence>> {
        return reminderRepository.observeReminders()
            .map { reminders ->
                scheduleCalculator.buildOccurrences(
                    reminders = reminders,
                    range = range
                )
            }
    }
}