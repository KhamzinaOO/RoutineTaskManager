package com.example.routinetaskmanager.featureReminder.application.notifications

import com.example.routinetaskmanager.core.notifications.api.AlarmPrecision
import com.example.routinetaskmanager.core.notifications.api.AppAlarmScheduler
import com.example.routinetaskmanager.core.notifications.api.NotificationTargetType
import com.example.routinetaskmanager.data.local.notifications.ScheduledNotificationDao
import com.example.routinetaskmanager.data.local.notifications.ScheduledNotificationEntity
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
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

        val notificationDao = FakeScheduledNotificationDao()
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
            scheduledNotificationDao = notificationDao
        )

        useCase()

        val scheduledNotifications = notificationDao.getAll()
        assertTrue(
            scheduledNotifications.any { entity ->
                entity.occurrenceKey == tomorrowOccurrence.occurrenceKey
            }
        )
        assertFalse(
            scheduledNotifications.any { entity ->
                entity.occurrenceKey == todayOccurrence.occurrenceKey
            }
        )
        assertEquals(
            tomorrowOccurrence.scheduledAtMillis,
            scheduledNotifications
                .minBy { entity -> entity.scheduledAtMillis }
                .scheduledAtMillis
        )
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

    private class FakeAlarmScheduler : AppAlarmScheduler {
        val scheduled = mutableListOf<Long>()
        val cancelled = mutableListOf<Int>()

        override fun schedule(
            targetType: NotificationTargetType,
            targetId: Long,
            scheduledAtMillis: Long,
            requestCode: Int,
            precision: AlarmPrecision
        ): Boolean {
            scheduled += scheduledAtMillis
            return true
        }

        override fun cancel(requestCode: Int) {
            cancelled += requestCode
        }
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

    private class FakeScheduledNotificationDao : ScheduledNotificationDao {
        private val notifications = mutableListOf<ScheduledNotificationEntity>()

        override suspend fun insert(entity: ScheduledNotificationEntity) {
            notifications.removeAll { existing ->
                existing.occurrenceKey == entity.occurrenceKey
            }
            notifications += entity
        }

        override suspend fun insertAll(entities: List<ScheduledNotificationEntity>) {
            entities.forEach { entity ->
                insert(entity)
            }
        }

        override suspend fun getAll(): List<ScheduledNotificationEntity> {
            return notifications.toList()
        }

        override suspend fun getByTargetType(
            targetType: String
        ): List<ScheduledNotificationEntity> {
            return notifications.filter { entity ->
                entity.targetType == targetType
            }
        }

        override suspend fun getByTargetTypeAndOccurrenceKind(
            targetType: String,
            occurrenceKind: String
        ): List<ScheduledNotificationEntity> {
            return notifications.filter { entity ->
                entity.targetType == targetType &&
                        entity.occurrenceKind == occurrenceKind
            }
        }

        override suspend fun getByTargetTypeAndOccurrenceKeyPrefix(
            targetType: String,
            occurrenceKeyPrefix: String
        ): List<ScheduledNotificationEntity> {
            return notifications.filter { entity ->
                entity.targetType == targetType &&
                        entity.occurrenceKey.startsWith(occurrenceKeyPrefix)
            }
        }

        override suspend fun getByTarget(
            targetType: String,
            targetId: Long
        ): List<ScheduledNotificationEntity> {
            return notifications.filter { entity ->
                entity.targetType == targetType &&
                        entity.targetId == targetId
            }
        }

        override suspend fun getByRequestCode(
            requestCode: Int
        ): ScheduledNotificationEntity? {
            return notifications.firstOrNull { entity ->
                entity.requestCode == requestCode
            }
        }

        override suspend fun deleteByRequestCode(requestCode: Int) {
            notifications.removeAll { entity ->
                entity.requestCode == requestCode
            }
        }

        override suspend fun deleteByTargetType(targetType: String) {
            notifications.removeAll { entity ->
                entity.targetType == targetType
            }
        }

        override suspend fun deleteByTargetTypeAndOccurrenceKind(
            targetType: String,
            occurrenceKind: String
        ) {
            notifications.removeAll { entity ->
                entity.targetType == targetType &&
                        entity.occurrenceKind == occurrenceKind
            }
        }

        override suspend fun deleteByTarget(
            targetType: String,
            targetId: Long
        ) {
            notifications.removeAll { entity ->
                entity.targetType == targetType &&
                        entity.targetId == targetId
            }
        }

        override suspend fun deleteAll() {
            notifications.clear()
        }
    }
}
