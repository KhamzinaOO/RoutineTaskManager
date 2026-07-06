package com.example.routinetaskmanager.featureReminder.application.schedule

import com.example.routinetaskmanager.core.coroutines.DispatcherProvider
import com.example.routinetaskmanager.featureReminder.domain.model.NotificationMode
import com.example.routinetaskmanager.featureReminder.domain.model.OnScheduleCertainDayRepeat
import com.example.routinetaskmanager.featureReminder.domain.model.Reminder
import com.example.routinetaskmanager.featureReminder.domain.model.ReminderDraft
import com.example.routinetaskmanager.featureReminder.domain.model.ReminderOccurrence
import com.example.routinetaskmanager.featureReminder.domain.model.ReminderOccurrenceState
import com.example.routinetaskmanager.featureReminder.domain.model.ReminderOccurrenceStatus
import com.example.routinetaskmanager.featureReminder.domain.model.ReminderRepeatRule
import com.example.routinetaskmanager.featureReminder.domain.model.RepeatScheduleMode
import com.example.routinetaskmanager.featureReminder.domain.model.WeeklyRepeat
import com.example.routinetaskmanager.featureReminder.domain.model.schedule.ReminderScheduleCalculator
import com.example.routinetaskmanager.featureReminder.domain.model.schedule.dayRange
import com.example.routinetaskmanager.featureReminder.domain.repository.ReminderOccurrenceRepository
import com.example.routinetaskmanager.featureReminder.domain.repository.ReminderRepository
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class ObserveReminderScheduleUseCaseTest {

    private val monday = LocalDate.of(2026, 7, 6)
    private val range = dayRange(monday)
    private val calculator = ReminderScheduleCalculator()

    @Test
    fun invoke_appliesSavedOccurrenceStateAndIgnoresDisabledReminders() = runBlocking {
        val enabledReminder = reminder(
            id = 1L,
            time = LocalTime.of(9, 0),
            isEnabled = true
        )
        val disabledReminder = reminder(
            id = 2L,
            time = LocalTime.of(10, 0),
            isEnabled = false
        )
        val occurrence = calculator
            .buildOccurrencesForReminder(enabledReminder, range)
            .single()

        val useCase = ObserveReminderScheduleUseCase(
            dispatcherProvider = ImmediateDispatcherProvider,
            reminderRepository = FakeReminderRepository(
                initialReminders = listOf(enabledReminder, disabledReminder)
            ),
            occurrenceRepository = FakeOccurrenceRepository(
                initialStates = listOf(
                    occurrence.toState(ReminderOccurrenceStatus.SKIPPED)
                )
            ),
            scheduleCalculator = calculator
        )

        val result = useCase(range).first()

        assertEquals(1, result.size)
        assertEquals(1L, result.single().reminderId)
        assertEquals(ReminderOccurrenceStatus.SKIPPED, result.single().status)
    }

    @Test
    fun invoke_returnsEmptyListWhenEnabledReminderDoesNotOccurInRange() = runBlocking {
        val useCase = ObserveReminderScheduleUseCase(
            dispatcherProvider = ImmediateDispatcherProvider,
            reminderRepository = FakeReminderRepository(
                initialReminders = listOf(
                    reminder(
                        id = 1L,
                        time = LocalTime.of(9, 0),
                        isEnabled = true,
                        selectedDays = setOf(DayOfWeek.TUESDAY)
                    )
                )
            ),
            occurrenceRepository = FakeOccurrenceRepository(initialStates = emptyList()),
            scheduleCalculator = calculator
        )

        val result = useCase(range).first()

        assertEquals(emptyList<ReminderOccurrence>(), result)
    }

    private fun ReminderOccurrence.toState(
        status: ReminderOccurrenceStatus
    ): ReminderOccurrenceState {
        return ReminderOccurrenceState(
            occurrenceKey = occurrenceKey,
            reminderId = reminderId,
            scheduledAtMillis = scheduledAtMillis,
            status = status,
            actedAtMillis = scheduledAtMillis,
            occurrenceKind = occurrenceKind
        )
    }

    private fun reminder(
        id: Long,
        time: LocalTime,
        isEnabled: Boolean,
        selectedDays: Set<DayOfWeek> = setOf(DayOfWeek.MONDAY)
    ): Reminder {
        return Reminder(
            id = id,
            name = "Reminder $id",
            instructionsText = null,
            repeatRule = ReminderRepeatRule.OnScheduleCertain(
                schedule = WeeklyRepeat(
                    mode = RepeatScheduleMode.DEFAULT,
                    selectedDays = selectedDays,
                    defaultValue = OnScheduleCertainDayRepeat(
                        pickedTimes = setOf(time)
                    ),
                    advancedEntries = emptyList()
                )
            ),
            notificationMode = NotificationMode.MUTE,
            createdAt = 1L,
            updatedAt = 1L,
            isEnabled = isEnabled
        )
    }

    private object ImmediateDispatcherProvider : DispatcherProvider {
        override val main: CoroutineDispatcher = Dispatchers.Unconfined
        override val io: CoroutineDispatcher = Dispatchers.Unconfined
        override val default: CoroutineDispatcher = Dispatchers.Unconfined
    }

    private class FakeReminderRepository(
        initialReminders: List<Reminder>
    ) : ReminderRepository {
        private val reminders = MutableStateFlow(initialReminders)

        override suspend fun getAllRemindersSnapshot(): List<Reminder> = reminders.value

        override fun observeReminders(): Flow<List<Reminder>> = reminders

        override fun observeReminderById(reminderId: Long): Flow<Reminder?> {
            return reminders.map { list ->
                list.firstOrNull { reminder -> reminder.id == reminderId }
            }
        }

        override suspend fun getReminderById(reminderId: Long): Reminder? {
            return reminders.value.firstOrNull { reminder -> reminder.id == reminderId }
        }

        override suspend fun createReminder(draft: ReminderDraft): Long {
            error("Not used in this test")
        }

        override suspend fun updateReminder(reminderId: Long, draft: ReminderDraft) {
            error("Not used in this test")
        }

        override suspend fun deleteReminder(reminderId: Long) {
            error("Not used in this test")
        }

        override suspend fun setReminderEnabled(reminderId: Long, enabled: Boolean) {
            error("Not used in this test")
        }

        override suspend fun setNotificationEnabled(reminderId: Long, enabled: Boolean) {
            error("Not used in this test")
        }

        override suspend fun updateNotificationMode(
            reminderId: Long,
            notificationMode: NotificationMode
        ) {
            error("Not used in this test")
        }
    }

    private class FakeOccurrenceRepository(
        initialStates: List<ReminderOccurrenceState>
    ) : ReminderOccurrenceRepository {
        private val states = MutableStateFlow(initialStates)

        override suspend fun upsertState(state: ReminderOccurrence) {
            val newState = ReminderOccurrenceState(
                occurrenceKey = state.occurrenceKey,
                reminderId = state.reminderId,
                scheduledAtMillis = state.scheduledAtMillis,
                status = state.status,
                actedAtMillis = state.scheduledAtMillis,
                occurrenceKind = state.occurrenceKind
            )
            states.value = states.value
                .filterNot { existing -> existing.occurrenceKey == state.occurrenceKey } + newState
        }

        override suspend fun getStateByKey(key: String): ReminderOccurrenceState? {
            return states.value.firstOrNull { state -> state.occurrenceKey == key }
        }

        override fun observeByReminderAndRange(
            reminderId: Long,
            startMillis: Long,
            endMillis: Long
        ): Flow<List<ReminderOccurrenceState>> {
            return states.map { list ->
                list.filter { state ->
                    state.reminderId == reminderId &&
                            state.scheduledAtMillis >= startMillis &&
                            state.scheduledAtMillis < endMillis
                }
            }
        }

        override suspend fun getByReminderAndRange(
            reminderId: Long,
            startMillis: Long,
            endMillis: Long
        ): List<ReminderOccurrenceState> {
            return states.value.filter { state ->
                state.reminderId == reminderId &&
                        state.scheduledAtMillis >= startMillis &&
                        state.scheduledAtMillis < endMillis
            }
        }

        override fun observeByRange(
            startMillis: Long,
            endMillis: Long
        ): Flow<List<ReminderOccurrenceState>> {
            return states.map { list ->
                list.filter { state ->
                    state.scheduledAtMillis in startMillis..<endMillis
                }
            }
        }

        override suspend fun getByRange(
            startMillis: Long,
            endMillis: Long
        ): List<ReminderOccurrenceState> {
            return states.value.filter { state ->
                state.scheduledAtMillis in startMillis..<endMillis
            }
        }

        override suspend fun deleteByReminderId(reminderId: Long) {
            states.value = states.value.filterNot { state -> state.reminderId == reminderId }
        }
    }
}
