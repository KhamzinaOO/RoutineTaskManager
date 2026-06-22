package com.example.routinetaskmanager.featureReminder.domain.useCase

import com.example.routinetaskmanager.featureReminder.application.session.WorkSessionManager
import com.example.routinetaskmanager.featureReminder.domain.model.ReminderOccurrence
import com.example.routinetaskmanager.featureReminder.domain.model.schedule.dayRange
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import java.time.LocalDate

class ObserveDayReminderOccurrencesUseCase(
    private val observeReminderScheduleUseCase: ObserveReminderScheduleUseCase,
    private val workSessionManager: WorkSessionManager
) {

    suspend operator fun invoke(
        date: LocalDate
    ): Flow<List<ReminderOccurrence>> {
        return combine(
            observeReminderScheduleUseCase(range = dayRange(date)),
            workSessionManager.observeActiveSessionOccurrences()
        ) { scheduledReminders, sessionReminders ->
            mergeOccurrences(
                scheduledReminders = scheduledReminders,
                sessionReminders = sessionReminders,
                date = date
            )
        }.distinctUntilChanged()
    }

    private fun mergeOccurrences(
        scheduledReminders: List<ReminderOccurrence>,
        sessionReminders: List<ReminderOccurrence>,
        date: LocalDate
    ): List<ReminderOccurrence> {
        return (scheduledReminders + sessionReminders.filter { occurrence ->
            occurrence.scheduledAt.toLocalDate() == date
        })
            .distinctBy { occurrence ->
                "${occurrence.reminderId}-${occurrence.scheduledAt}-${occurrence.repeatType}"
            }
            .sortedBy { occurrence ->
                occurrence.scheduledAt
            }
    }
}