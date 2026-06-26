package com.example.routinetaskmanager.featureReminder.application.schedule

import com.example.routinetaskmanager.core.coroutines.DispatcherProvider
import com.example.routinetaskmanager.featureReminder.application.session.WorkSessionManager
import com.example.routinetaskmanager.featureReminder.domain.model.ReminderOccurrence
import com.example.routinetaskmanager.featureReminder.domain.model.schedule.dayRange
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import java.time.LocalDate

class ObserveDayReminderOccurrencesUseCase(
    private val dispatcherProvider: DispatcherProvider,
    private val observeReminderScheduleUseCase: ObserveReminderScheduleUseCase,
    private val workSessionManager: WorkSessionManager
) {

    operator fun invoke(
        date: LocalDate
    ): Flow<List<ReminderOccurrence>> {
        return combine(
            observeReminderScheduleUseCase(range = dayRange(date)),
            workSessionManager.observeActiveSessionOccurrences()
        ) { scheduledReminders, sessionReminders ->
            mergeDayOccurrences(
                scheduledReminders = scheduledReminders,
                sessionReminders = sessionReminders,
                date = date
            )
        }
            .flowOn(dispatcherProvider.io)
            .distinctUntilChanged()
    }

    fun mergeDayOccurrences(
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