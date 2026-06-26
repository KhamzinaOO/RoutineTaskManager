package com.example.routinetaskmanager.featureReminder.application.schedule

import com.example.routinetaskmanager.core.coroutines.DispatcherProvider
import com.example.routinetaskmanager.featureReminder.domain.model.Reminder
import com.example.routinetaskmanager.featureReminder.domain.model.ReminderOccurrence
import com.example.routinetaskmanager.featureReminder.domain.model.ReminderOccurrenceStatus
import com.example.routinetaskmanager.featureReminder.domain.model.schedule.ReminderScheduleCalculator
import com.example.routinetaskmanager.featureReminder.domain.model.schedule.ScheduleRange
import com.example.routinetaskmanager.featureReminder.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime

class ObserveReminderOccurrenceUseCase(
    private val dispatcherProvider: DispatcherProvider,
    private val reminderRepository: ReminderRepository,
    private val scheduleCalculator: ReminderScheduleCalculator
) {
    operator fun invoke(
        reminderId: Long,
        range: ScheduleRange
    ): Flow<ReminderOccurrence?> {
       return reminderRepository.observeReminderById(reminderId)
           .map{ reminder ->
               reminder?.takeIf { it.isEnabled }
                   ?.let{
                       scheduleCalculator.buildOccurrencesForReminder(
                           reminder = reminder,
                           range = range
                       ).firstOrNull { it.status == ReminderOccurrenceStatus.PLANNED }
                   }
           }
           .flowOn(dispatcherProvider.default)
    }
}