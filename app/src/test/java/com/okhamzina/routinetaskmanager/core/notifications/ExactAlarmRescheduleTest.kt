package com.okhamzina.routinetaskmanager.core.notifications

import com.okhamzina.routinetaskmanager.core.coroutines.DispatcherProvider
import com.okhamzina.routinetaskmanager.core.error.AppResult
import com.okhamzina.routinetaskmanager.core.notifications.api.AlarmPrecision
import com.okhamzina.routinetaskmanager.core.notifications.api.AppAlarmScheduleResult
import com.okhamzina.routinetaskmanager.core.notifications.api.AppAlarmScheduler
import com.okhamzina.routinetaskmanager.core.notifications.api.NotificationOccurrenceKind
import com.okhamzina.routinetaskmanager.core.notifications.api.NotificationTargetType
import com.okhamzina.routinetaskmanager.core.notifications.domain.ScheduledNotification
import com.okhamzina.routinetaskmanager.core.notifications.domain.ScheduledNotificationRepository
import com.okhamzina.routinetaskmanager.featureReminder.application.notifications.ReminderSessionNotificationUseCase
import com.okhamzina.routinetaskmanager.featureReminder.application.notifications.RescheduleRemindersUseCase
import com.okhamzina.routinetaskmanager.featureReminder.application.session.PersistedWorkSessionState
import com.okhamzina.routinetaskmanager.featureReminder.application.session.WorkSessionManager
import com.okhamzina.routinetaskmanager.featureReminder.application.session.WorkSessionStateStore
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.IntervalRepeat
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.NotificationMode
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.OnScheduleCertainDayRepeat
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.Reminder
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.ReminderDraft
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.ReminderOccurrence
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.ReminderOccurrenceState
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.ReminderRepeatRule
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.RepeatInterval
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.RepeatScheduleMode
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.RepeatUnit
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.WeeklyRepeat
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.schedule.ReminderScheduleCalculator
import com.okhamzina.routinetaskmanager.featureReminder.domain.repository.ReminderOccurrenceRepository
import com.okhamzina.routinetaskmanager.featureReminder.domain.repository.ReminderRepository
import java.time.DayOfWeek
import java.time.LocalTime
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExactAlarmRescheduleTest {

    @Test
    fun rescheduleAll_afterExactAccessEnabled_replacesAllAlarmsWithExact() = runBlocking {
        val reminderRepository = FakeReminderRepository(
            listOf(regularReminder(), sessionReminder())
        )
        val occurrenceRepository = FakeOccurrenceRepository()
        val scheduledNotificationRepository = FakeScheduledNotificationRepository()
        val alarmScheduler = ExactAccessAwareAlarmScheduler(exactAccessGranted = false)

        val sessionNotificationUseCase = ReminderSessionNotificationUseCase(
            dispatcherProvider = TestDispatcherProvider,
            reminderRepository = reminderRepository,
            reminderOccurrenceRepository = occurrenceRepository,
            alarmScheduler = alarmScheduler,
            scheduledNotificationRepository = scheduledNotificationRepository
        )
        val workSessionManager = WorkSessionManager(
            stateStore = InMemoryWorkSessionStateStore(),
            reminderSessionNotificationUseCase = sessionNotificationUseCase
        )
        val regularRescheduleUseCase = RescheduleRemindersUseCase(
            reminderOccurrenceRepository = occurrenceRepository,
            reminderRepository = reminderRepository,
            scheduleCalculator = ReminderScheduleCalculator(),
            alarmScheduler = alarmScheduler,
            scheduledNotificationRepository = scheduledNotificationRepository,
            dispatcherProvider = TestDispatcherProvider
        )
        val rescheduleAllNotificationsUseCase = RescheduleAllNotificationsUseCase(
            rescheduleRemindersUseCase = regularRescheduleUseCase,
            workSessionManager = workSessionManager
        )

        assertTrue(workSessionManager.startSession() is AppResult.Success)
        assertTrue(rescheduleAllNotificationsUseCase() is AppResult.Success)
        assertContainsRegularAndSessionAlarms(alarmScheduler.activeAlarms.values)
        assertTrue(
            alarmScheduler.activeAlarms.values.all { alarm ->
                alarm.effectivePrecision == AlarmPrecision.INEXACT
            }
        )

        alarmScheduler.exactAccessGranted = true

        assertTrue(rescheduleAllNotificationsUseCase() is AppResult.Success)
        assertContainsRegularAndSessionAlarms(alarmScheduler.activeAlarms.values)
        assertTrue(
            alarmScheduler.activeAlarms.values.all { alarm ->
                alarm.effectivePrecision == AlarmPrecision.EXACT
            }
        )
    }

    private fun assertContainsRegularAndSessionAlarms(alarms: Collection<ScheduledAlarm>) {
        assertTrue(alarms.isNotEmpty())
        assertEquals(
            setOf(NotificationOccurrenceKind.REGULAR, NotificationOccurrenceKind.SESSION),
            alarms.map { alarm -> alarm.occurrenceKind }.toSet()
        )
    }

    private fun regularReminder(): Reminder {
        return reminder(
            id = 1L,
            rule = ReminderRepeatRule.OnScheduleCertain(
                schedule = WeeklyRepeat(
                    mode = RepeatScheduleMode.DEFAULT,
                    selectedDays = DayOfWeek.entries.toSet(),
                    defaultValue = OnScheduleCertainDayRepeat(
                        pickedTimes = setOf(LocalTime.now().plusHours(1))
                    ),
                    advancedEntries = emptyList()
                )
            )
        )
    }

    private fun sessionReminder(): Reminder {
        return reminder(
            id = 2L,
            rule = ReminderRepeatRule.DuringSessionPeriod(
                schedule = WeeklyRepeat(
                    mode = RepeatScheduleMode.DEFAULT,
                    selectedDays = DayOfWeek.entries.toSet(),
                    defaultValue = IntervalRepeat(
                        interval = RepeatInterval(
                            value = 5,
                            unit = RepeatUnit.MINUTES
                        )
                    ),
                    advancedEntries = emptyList()
                )
            )
        )
    }

    private fun reminder(id: Long, rule: ReminderRepeatRule): Reminder {
        return Reminder(
            id = id,
            name = "Reminder $id",
            instructionsText = null,
            repeatRule = rule,
            notificationMode = NotificationMode.MUTE,
            createdAt = 1L,
            updatedAt = 1L
        )
    }

    private class ExactAccessAwareAlarmScheduler(
        var exactAccessGranted: Boolean
    ) : AppAlarmScheduler {
        val activeAlarms = mutableMapOf<Int, ScheduledAlarm>()

        override fun schedule(
            targetType: NotificationTargetType,
            targetId: Long,
            scheduledAtMillis: Long,
            requestCode: Int,
            precision: AlarmPrecision,
            occurrenceKind: NotificationOccurrenceKind
        ): AppAlarmScheduleResult {
            val effectivePrecision = if (
                precision == AlarmPrecision.EXACT && !exactAccessGranted
            ) {
                AlarmPrecision.INEXACT
            } else {
                precision
            }

            activeAlarms[requestCode] = ScheduledAlarm(
                occurrenceKind = occurrenceKind,
                effectivePrecision = effectivePrecision
            )
            return AppAlarmScheduleResult.Scheduled
        }

        override fun cancel(requestCode: Int) {
            activeAlarms.remove(requestCode)
        }
    }

    private class FakeReminderRepository(
        reminders: List<Reminder>
    ) : ReminderRepository {
        private val remindersFlow = MutableStateFlow(reminders)

        override suspend fun getAllRemindersSnapshot(): List<Reminder> = remindersFlow.value
        override fun observeReminders(): Flow<List<Reminder>> = remindersFlow
        override fun observeReminderById(reminderId: Long): Flow<Reminder?> =
            remindersFlow.map { reminders -> reminders.firstOrNull { it.id == reminderId } }

        override suspend fun getReminderById(reminderId: Long): Reminder? =
            remindersFlow.value.firstOrNull { it.id == reminderId }

        override suspend fun createReminder(draft: ReminderDraft): Long = error("Not used")
        override suspend fun updateReminder(reminderId: Long, draft: ReminderDraft) = error("Not used")
        override suspend fun deleteReminder(reminderId: Long) = error("Not used")
        override suspend fun setReminderEnabled(reminderId: Long, enabled: Boolean) = error("Not used")
        override suspend fun setNotificationEnabled(reminderId: Long, enabled: Boolean) = error("Not used")
        override suspend fun updateNotificationMode(reminderId: Long, notificationMode: NotificationMode) =
            error("Not used")
    }

    private class FakeOccurrenceRepository : ReminderOccurrenceRepository {
        override suspend fun upsertState(state: ReminderOccurrence) = Unit
        override suspend fun upsertState(state: ReminderOccurrenceState) = Unit
        override suspend fun getStateByKey(key: String): ReminderOccurrenceState? = null
        override fun observeByReminderAndRange(
            reminderId: Long,
            startMillis: Long,
            endMillis: Long
        ): Flow<List<ReminderOccurrenceState>> = flowOf(emptyList())

        override suspend fun getByReminderAndRange(
            reminderId: Long,
            startMillis: Long,
            endMillis: Long
        ): List<ReminderOccurrenceState> = emptyList()

        override fun observeByRange(
            startMillis: Long,
            endMillis: Long
        ): Flow<List<ReminderOccurrenceState>> = flowOf(emptyList())

        override suspend fun getByRange(
            startMillis: Long,
            endMillis: Long
        ): List<ReminderOccurrenceState> = emptyList()

        override suspend fun deleteByReminderId(reminderId: Long) = Unit
    }

    private class FakeScheduledNotificationRepository : ScheduledNotificationRepository {
        private val notifications = mutableListOf<ScheduledNotification>()

        override suspend fun insert(notification: ScheduledNotification) {
            notifications.removeAll { it.requestCode == notification.requestCode }
            notifications += notification
        }

        override suspend fun insertAll(notifications: List<ScheduledNotification>) {
            notifications.forEach { insert(it) }
        }

        override suspend fun getAll(): List<ScheduledNotification> = notifications.toList()
        override suspend fun getByTargetType(targetType: NotificationTargetType) =
            notifications.filter { it.targetType == targetType }

        override suspend fun getByTargetTypeAndOccurrenceKind(
            targetType: NotificationTargetType,
            occurrenceKind: NotificationOccurrenceKind
        ) = notifications.filter {
            it.targetType == targetType && it.occurrenceKind == occurrenceKind
        }

        override suspend fun getByTargetTypeAndOccurrenceKeyPrefix(
            targetType: NotificationTargetType,
            occurrenceKeyPrefix: String
        ) = notifications.filter {
            it.targetType == targetType && it.occurrenceKey.startsWith(occurrenceKeyPrefix)
        }

        override suspend fun getByTarget(targetType: NotificationTargetType, targetId: Long) =
            notifications.filter { it.targetType == targetType && it.targetId == targetId }

        override suspend fun getByRequestCode(requestCode: Int) =
            notifications.firstOrNull { it.requestCode == requestCode }

        override suspend fun deleteByRequestCode(requestCode: Int) {
            notifications.removeAll { it.requestCode == requestCode }
        }

        override suspend fun deleteByTargetType(targetType: NotificationTargetType) {
            notifications.removeAll { it.targetType == targetType }
        }

        override suspend fun deleteByTargetTypeAndOccurrenceKind(
            targetType: NotificationTargetType,
            occurrenceKind: NotificationOccurrenceKind
        ) {
            notifications.removeAll {
                it.targetType == targetType && it.occurrenceKind == occurrenceKind
            }
        }

        override suspend fun deleteByTarget(targetType: NotificationTargetType, targetId: Long) {
            notifications.removeAll { it.targetType == targetType && it.targetId == targetId }
        }

        override suspend fun deleteAll() {
            notifications.clear()
        }
    }

    private class InMemoryWorkSessionStateStore : WorkSessionStateStore {
        private var state: PersistedWorkSessionState? = null
        override fun load(): PersistedWorkSessionState? = state
        override fun save(state: PersistedWorkSessionState) {
            this.state = state
        }

        override fun clear() {
            state = null
        }
    }

    private object TestDispatcherProvider : DispatcherProvider {
        override val main: CoroutineDispatcher = Dispatchers.Unconfined
        override val io: CoroutineDispatcher = Dispatchers.Unconfined
        override val default: CoroutineDispatcher = Dispatchers.Unconfined
    }
}

private data class ScheduledAlarm(
    val occurrenceKind: NotificationOccurrenceKind,
    val effectivePrecision: AlarmPrecision
)
