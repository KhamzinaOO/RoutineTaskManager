package com.okhamzina.routinetaskmanager.featureReminder.application.schedule

import com.okhamzina.routinetaskmanager.core.coroutines.DispatcherProvider
import com.okhamzina.routinetaskmanager.featureReminder.application.session.WorkSessionManager
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.ReminderOccurrence
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.ReminderOccurrenceStatus
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.schedule.ScheduleRange
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import java.time.LocalDateTime

class ObserveNextReminderOccurrenceByIdUseCase(
    private val dispatcherProvider: DispatcherProvider,
    private val workSessionManager: WorkSessionManager,
    private val observeReminderOccurrenceUseCase: ObserveReminderOccurrenceUseCase
) {
    operator fun invoke(
        date: LocalDateTime,
        reminderId: Long
    ): Flow<ReminderOccurrence?> {
        val range = ScheduleRange(
            start = date,
            endExclusive = date.plusDays(7)
        )

        return combine(
            observeReminderOccurrenceUseCase(
                reminderId = reminderId,
                range = range
            ),
            workSessionManager.observeReminderInSessionById(
                reminderId = reminderId
            )
        ) { scheduledOccurrence, sessionOccurrences ->
            val scheduledOccurrences = listOfNotNull(scheduledOccurrence)
            val sessionOccurrencesSafe = sessionOccurrences.orEmpty()

            (scheduledOccurrences + sessionOccurrencesSafe)
                .filter { occurrence ->
                    occurrence.status == ReminderOccurrenceStatus.PLANNED &&
                            !occurrence.scheduledAt.isBefore(date)
                }
                .minByOrNull { occurrence ->
                    occurrence.scheduledAt
                }
        }.flowOn(dispatcherProvider.io)
    }
}