package com.okhamzina.routinetaskmanager.featureReminder.application.notifications

import com.okhamzina.routinetaskmanager.core.coroutines.DispatcherProvider
import com.okhamzina.routinetaskmanager.core.error.AppError
import com.okhamzina.routinetaskmanager.core.error.AppResult
import com.okhamzina.routinetaskmanager.core.notifications.api.AlarmPrecision
import com.okhamzina.routinetaskmanager.core.notifications.api.AppAlarmScheduleResult
import com.okhamzina.routinetaskmanager.core.notifications.api.AppAlarmScheduler
import com.okhamzina.routinetaskmanager.core.notifications.api.NotificationOccurrenceKind
import com.okhamzina.routinetaskmanager.core.notifications.api.NotificationTargetType
import com.okhamzina.routinetaskmanager.core.notifications.domain.ScheduledNotification
import com.okhamzina.routinetaskmanager.core.notifications.domain.ScheduledNotificationRepository
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.NotificationMode
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.OnScheduleCertainDayRepeat
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.Reminder
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.ReminderDraft
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.ReminderOccurrence
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.ReminderOccurrenceState
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.ReminderOccurrenceStatus
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.ReminderRepeatRule
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.RepeatScheduleMode
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.WeeklyRepeat
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.schedule.ReminderScheduleCalculator
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.schedule.dayRange
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.schedule.ScheduleRange
import com.okhamzina.routinetaskmanager.featureReminder.domain.repository.ReminderOccurrenceRepository
import com.okhamzina.routinetaskmanager.featureReminder.domain.repository.ReminderRepository
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RescheduleRemindersUseCaseTest {

    private val calculator = ReminderScheduleCalculator()

    @Test
    fun invoke_schedulesTomorrowOccurrenceWhenTodayOccurrenceIsSkipped() = runBlocking {
        val today = LocalDate.now()
        val reminder = reminder(
            id = 1L,
            selectedDays = DayOfWeek.entries.toSet(),
            time = LocalTime.NOON
        )
        val todayOccurrence = calculator
            .buildOccurrencesForReminder(
                reminder = reminder,
                range = dayRange(today)
            )
            .single()
        val tomorrowOccurrence = calculator
            .buildOccurrencesForReminder(
                reminder = reminder,
                range = dayRange(today.plusDays(1))
            )
            .single()

        val notificationRepository = FakeScheduledNotificationRepository()
        val useCase = RescheduleRemindersUseCase(
            reminderOccurrenceRepository = FakeOccurrenceRepository(
                initialStates = listOf(
                    todayOccurrence.toState(ReminderOccurrenceStatus.SKIPPED)
                )
            ),
            reminderRepository = FakeReminderRepository(
                initialReminders = listOf(reminder)
            ),
            scheduleCalculator = calculator,
            alarmScheduler = FakeAlarmScheduler(),
            scheduledNotificationRepository = notificationRepository,
            dispatcherProvider = TestDispatcherProvider
        )

        useCase()

        val scheduledNotifications = notificationRepository.getAll()
        assertTrue(
            scheduledNotifications.any { notification ->
                notification.occurrenceKey == tomorrowOccurrence.occurrenceKey
            }
        )
        assertFalse(
            scheduledNotifications.any { notification ->
                notification.occurrenceKey == todayOccurrence.occurrenceKey
            }
        )
        assertEquals(
            tomorrowOccurrence.scheduledAtMillis,
            scheduledNotifications
                .minBy { notification -> notification.scheduledAtMillis }
                .scheduledAtMillis
        )
    }

    @Test
    fun invoke_returnsExactAlarmErrorWhenExactAlarmAccessDenied() = runBlocking {
        val notificationRepository = FakeScheduledNotificationRepository()
        val useCase = RescheduleRemindersUseCase(
            reminderOccurrenceRepository = FakeOccurrenceRepository(),
            reminderRepository = FakeReminderRepository(
                initialReminders = listOf(
                    reminder(
                        id = 1L,
                        selectedDays = DayOfWeek.entries.toSet(),
                        time = LocalTime.NOON
                    )
                )
            ),
            scheduleCalculator = calculator,
            alarmScheduler = FakeAlarmScheduler {
                AppAlarmScheduleResult.ExactAlarmAccessDenied
            },
            scheduledNotificationRepository = notificationRepository,
            dispatcherProvider = TestDispatcherProvider
        )

        val result = useCase()

        assertTrue(result is AppResult.Error)
        assertEquals(
            AppError.ExactAlarmPermissionDenied,
            (result as AppResult.Error).error
        )
        assertTrue(notificationRepository.getAll().isEmpty())
    }

    @Test
    fun invoke_cancelsAlreadyScheduledNotificationsWhenLaterSchedulingFails() = runBlocking {
        val notificationRepository = FakeScheduledNotificationRepository()
        val alarmScheduler = FakeAlarmScheduler { callIndex ->
            if (callIndex == 1) {
                AppAlarmScheduleResult.Scheduled
            } else {
                AppAlarmScheduleResult.Failed(RuntimeException("Scheduling failed"))
            }
        }
        val useCase = RescheduleRemindersUseCase(
            reminderOccurrenceRepository = FakeOccurrenceRepository(),
            reminderRepository = FakeReminderRepository(
                initialReminders = listOf(
                    reminder(
                        id = 1L,
                        selectedDays = DayOfWeek.entries.toSet(),
                        time = LocalTime.NOON
                    )
                )
            ),
            scheduleCalculator = calculator,
            alarmScheduler = alarmScheduler,
            scheduledNotificationRepository = notificationRepository,
            dispatcherProvider = TestDispatcherProvider
        )

        val result = useCase()

        assertTrue(result is AppResult.Error)
        assertTrue((result as AppResult.Error).error is AppError.AlarmSchedulingFailed)
        assertTrue(notificationRepository.getAll().isEmpty())
        assertEquals(
            alarmScheduler.scheduledRequestCodes,
            alarmScheduler.cancelled
        )
    }

    @Test
    fun invoke_schedulesOnlyThirtyNearestOccurrencesInChronologicalOrder() = runBlocking {
        val reminders = (0 until 40).map { index ->
            reminder(
                id = index.toLong() + 1L,
                selectedDays = DayOfWeek.entries.toSet(),
                time = LocalTime.of(12, index)
            )
        }
        val alarmScheduler = FakeAlarmScheduler()
        val useCase = RescheduleRemindersUseCase(
            reminderOccurrenceRepository = FakeOccurrenceRepository(),
            reminderRepository = FakeReminderRepository(reminders),
            scheduleCalculator = calculator,
            alarmScheduler = alarmScheduler,
            scheduledNotificationRepository = FakeScheduledNotificationRepository(),
            dispatcherProvider = TestDispatcherProvider
        )

        val result = useCase()

        assertTrue(result is AppResult.Success)
        assertEquals(30, alarmScheduler.scheduled.size)
        assertEquals(alarmScheduler.scheduled.sorted(), alarmScheduler.scheduled)
    }

    @Test
    fun invoke_restoresReplacedAlarmAndKeepsOldDatabaseStateWhenSchedulingFails() = runBlocking {
        val reminders = listOf(
            reminder(
                id = 1L,
                selectedDays = DayOfWeek.entries.toSet(),
                time = LocalTime.NOON
            ),
            reminder(
                id = 2L,
                selectedDays = DayOfWeek.entries.toSet(),
                time = LocalTime.of(13, 0)
            )
        )
        val now = LocalDateTime.now()
        val firstOccurrence = calculator.buildOccurrences(
            reminders = reminders,
            range = ScheduleRange(
                start = now,
                endExclusive = now.toLocalDate().plusDays(8).atStartOfDay()
            )
        ).first { occurrence -> occurrence.scheduledAt.isAfter(now) }
        val oldNotification = ScheduledNotification(
            id = 7,
            requestCode = 4242,
            targetType = NotificationTargetType.REMINDER,
            targetId = firstOccurrence.reminderId,
            scheduledAtMillis = firstOccurrence.scheduledAtMillis,
            occurrenceKey = firstOccurrence.occurrenceKey,
            occurrenceKind = NotificationOccurrenceKind.REGULAR,
            createdAtMillis = 1L
        )
        val notificationRepository = FakeScheduledNotificationRepository(
            initialNotifications = listOf(oldNotification)
        )
        val alarmScheduler = FakeAlarmScheduler { callIndex ->
            if (callIndex == 2) {
                AppAlarmScheduleResult.Failed(RuntimeException("Scheduling failed"))
            } else {
                AppAlarmScheduleResult.Scheduled
            }
        }
        val useCase = RescheduleRemindersUseCase(
            reminderOccurrenceRepository = FakeOccurrenceRepository(),
            reminderRepository = FakeReminderRepository(reminders),
            scheduleCalculator = calculator,
            alarmScheduler = alarmScheduler,
            scheduledNotificationRepository = notificationRepository,
            dispatcherProvider = TestDispatcherProvider
        )

        val result = useCase()

        assertTrue(result is AppResult.Error)
        assertEquals(listOf(oldNotification), notificationRepository.getAll())
        assertEquals(listOf(4242, 4242), alarmScheduler.scheduledRequestCodes)
        assertFalse(4242 in alarmScheduler.cancelled)
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
        selectedDays: Set<DayOfWeek>,
        time: LocalTime
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
            isEnabled = true,
            notificationEnabled = true
        )
    }

    private class FakeAlarmScheduler(
        private val resultProvider: (callIndex: Int) -> AppAlarmScheduleResult = {
            AppAlarmScheduleResult.Scheduled
        }
    ) : AppAlarmScheduler {
        val scheduled = mutableListOf<Long>()
        val scheduledRequestCodes = mutableListOf<Int>()
        val cancelled = mutableListOf<Int>()
        private var callIndex: Int = 0

        override fun schedule(
            targetType: NotificationTargetType,
            targetId: Long,
            scheduledAtMillis: Long,
            requestCode: Int,
            precision: AlarmPrecision,
            occurrenceKind: NotificationOccurrenceKind
        ): AppAlarmScheduleResult {
            callIndex += 1
            val result = resultProvider(callIndex)

            if (result == AppAlarmScheduleResult.Scheduled) {
                scheduled += scheduledAtMillis
                scheduledRequestCodes += requestCode
            }

            return result
        }

        override fun cancel(requestCode: Int) {
            cancelled += requestCode
        }
    }

    private object TestDispatcherProvider : DispatcherProvider {
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
        initialStates: List<ReminderOccurrenceState> = emptyList()
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

        override suspend fun upsertState(state: ReminderOccurrenceState) {
            states.value = states.value
                .filterNot { existing -> existing.occurrenceKey == state.occurrenceKey } + state
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

    private class FakeScheduledNotificationRepository(
        initialNotifications: List<ScheduledNotification> = emptyList()
    ) : ScheduledNotificationRepository {
        private val notifications = initialNotifications.toMutableList()

        override suspend fun insert(notification: ScheduledNotification) {
            notifications.removeAll { existing ->
                existing.occurrenceKey == notification.occurrenceKey
            }
            notifications += notification
        }

        override suspend fun insertAll(notifications: List<ScheduledNotification>) {
            notifications.forEach { notification ->
                insert(notification)
            }
        }

        override suspend fun getAll(): List<ScheduledNotification> {
            return notifications.toList()
        }

        override fun observeHasScheduledNotifications(): Flow<Boolean> {
            return flowOf(notifications.isNotEmpty())
        }

        override suspend fun getByTargetType(
            targetType: NotificationTargetType
        ): List<ScheduledNotification> {
            return notifications.filter { notification ->
                notification.targetType == targetType
            }
        }

        override suspend fun getByTargetTypeAndOccurrenceKind(
            targetType: NotificationTargetType,
            occurrenceKind: NotificationOccurrenceKind
        ): List<ScheduledNotification> {
            return notifications.filter { notification ->
                notification.targetType == targetType &&
                        notification.occurrenceKind == occurrenceKind
            }
        }

        override suspend fun getByTargetTypeAndOccurrenceKeyPrefix(
            targetType: NotificationTargetType,
            occurrenceKeyPrefix: String
        ): List<ScheduledNotification> {
            return notifications.filter { notification ->
                notification.targetType == targetType &&
                        notification.occurrenceKey.startsWith(occurrenceKeyPrefix)
            }
        }

        override suspend fun getByTarget(
            targetType: NotificationTargetType,
            targetId: Long
        ): List<ScheduledNotification> {
            return notifications.filter { notification ->
                notification.targetType == targetType &&
                        notification.targetId == targetId
            }
        }

        override suspend fun getByRequestCode(
            requestCode: Int
        ): ScheduledNotification? {
            return notifications.firstOrNull { notification ->
                notification.requestCode == requestCode
            }
        }

        override suspend fun deleteByRequestCode(requestCode: Int) {
            notifications.removeAll { notification ->
                notification.requestCode == requestCode
            }
        }

        override suspend fun deleteByTargetType(targetType: NotificationTargetType) {
            notifications.removeAll { notification ->
                notification.targetType == targetType
            }
        }

        override suspend fun deleteByTargetTypeAndOccurrenceKind(
            targetType: NotificationTargetType,
            occurrenceKind: NotificationOccurrenceKind
        ) {
            notifications.removeAll { notification ->
                notification.targetType == targetType &&
                        notification.occurrenceKind == occurrenceKind
            }
        }

        override suspend fun deleteByTarget(
            targetType: NotificationTargetType,
            targetId: Long
        ) {
            notifications.removeAll { notification ->
                notification.targetType == targetType &&
                        notification.targetId == targetId
            }
        }

        override suspend fun deleteAll() {
            notifications.clear()
        }
    }
}
