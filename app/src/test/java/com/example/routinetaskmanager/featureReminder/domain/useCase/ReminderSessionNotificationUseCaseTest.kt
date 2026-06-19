package com.example.routinetaskmanager.featureReminder.domain.useCase

import android.net.Uri
import com.example.routinetaskmanager.core.notifications.AlarmPrecision
import com.example.routinetaskmanager.core.notifications.AppAlarmScheduler
import com.example.routinetaskmanager.core.notifications.NotificationTargetType
import com.example.routinetaskmanager.data.local.notifications.ScheduledNotificationDao
import com.example.routinetaskmanager.data.local.notifications.ScheduledNotificationEntity
import com.example.routinetaskmanager.featureReminder.domain.model.IntervalRepeat
import com.example.routinetaskmanager.featureReminder.domain.model.NotificationMode
import com.example.routinetaskmanager.featureReminder.domain.model.Reminder
import com.example.routinetaskmanager.featureReminder.domain.model.ReminderRepeatRule
import com.example.routinetaskmanager.featureReminder.domain.model.ReminderSaveData
import com.example.routinetaskmanager.featureReminder.domain.model.RepeatInterval
import com.example.routinetaskmanager.featureReminder.domain.model.RepeatScheduleMode
import com.example.routinetaskmanager.featureReminder.domain.model.RepeatUnit
import com.example.routinetaskmanager.featureReminder.domain.model.WeeklyRepeat
import com.example.routinetaskmanager.featureReminder.domain.repository.ReminderRepository
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.ZoneId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReminderSessionNotificationUseCaseTest {

    @Test
    fun startSessionSchedulesDuringSessionAndAfterAnotherNotifications() = runBlocking {
        val dao = FakeScheduledNotificationDao()
        val alarmScheduler = FakeAlarmScheduler()
        val startedAt = LocalDateTime.of(2026, 6, 15, 9, 0)
        val useCase = ReminderSessionNotificationUseCase(
            reminderRepository = FakeReminderRepository(
                reminders = listOf(
                    duringSessionReminder(id = 10L, intervalMinutes = 10),
                    afterAnotherReminder(id = 20L, waitMinutes = 5),
                    afterAnotherReminder(id = 30L, waitMinutes = 7)
                )
            ),
            alarmScheduler = alarmScheduler,
            scheduledNotificationDao = dao
        )

        val result = useCase.startSession(startedAt)

        assertEquals(3, result.sessionReminderCount)
        assertEquals(14, result.scheduledNotificationCount)
        assertEquals(14, dao.entities.size)
        assertEquals(14, alarmScheduler.scheduled.size)
        assertTrue(alarmScheduler.scheduledPrecisions.all { it == AlarmPrecision.INEXACT })

        val firstAfterAnotherMillis = startedAt
            .plusMinutes(5)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        val secondAfterAnotherMillis = startedAt
            .plusMinutes(12)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        assertTrue(
            dao.entities.any { entity ->
                entity.targetId == 20L && entity.scheduledAtMillis == firstAfterAnotherMillis
            }
        )
        assertTrue(
            dao.entities.any { entity ->
                entity.targetId == 30L && entity.scheduledAtMillis == secondAfterAnotherMillis
            }
        )

        useCase.endSession()

        assertTrue(dao.entities.isEmpty())
        assertEquals(14, alarmScheduler.cancelled.size)
    }

    private fun duringSessionReminder(
        id: Long,
        intervalMinutes: Int
    ): Reminder {
        return reminder(
            id = id,
            repeatRule = ReminderRepeatRule.DuringSessionPeriod(
                schedule = WeeklyRepeat(
                    mode = RepeatScheduleMode.DEFAULT,
                    selectedDays = setOf(DayOfWeek.MONDAY),
                    defaultValue = IntervalRepeat(
                        interval = RepeatInterval(
                            value = intervalMinutes,
                            unit = RepeatUnit.MINUTES
                        )
                    ),
                    advancedEntries = emptyList()
                )
            )
        )
    }

    private fun afterAnotherReminder(
        id: Long,
        waitMinutes: Int
    ): Reminder {
        return reminder(
            id = id,
            repeatRule = ReminderRepeatRule.AfterAnother(
                waitInterval = RepeatInterval(
                    value = waitMinutes,
                    unit = RepeatUnit.MINUTES
                )
            )
        )
    }

    private fun reminder(
        id: Long,
        repeatRule: ReminderRepeatRule
    ): Reminder {
        return Reminder(
            id = id,
            name = "Reminder $id",
            instructionsText = null,
            repeatRule = repeatRule,
            notificationMode = NotificationMode.SOUND,
            createdAt = 0L,
            updatedAt = 0L
        )
    }

    private class FakeReminderRepository(
        private val reminders: List<Reminder>
    ) : ReminderRepository {
        override suspend fun getAllRemindersSnapshot(): List<Reminder> = reminders
        override fun observeReminders(): Flow<List<Reminder>> = flowOf(reminders)
        override fun observeReminderById(reminderId: Long): Flow<Reminder?> =
            flowOf(reminders.firstOrNull { it.id == reminderId })

        override suspend fun getReminderById(reminderId: Long): Reminder? =
            reminders.firstOrNull { it.id == reminderId }

        override suspend fun createReminder(data: ReminderSaveData): Long =
            error("Not needed")

        override suspend fun updateReminder(id: Long, data: ReminderSaveData) =
            error("Not needed")

        override suspend fun deleteReminder(reminderId: Long) =
            error("Not needed")

        override suspend fun addImageToReminder(reminderId: Long, imageUri: Uri) =
            error("Not needed")

        override suspend fun deleteImage(imageId: Long) =
            error("Not needed")

        override suspend fun setReminderEnabled(reminderId: Long, enabled: Boolean) =
            error("Not needed")

        override suspend fun setNotificationEnabled(reminderId: Long, enabled: Boolean) =
            error("Not needed")

        override suspend fun updateNotificationMode(
            reminderId: Long,
            notificationMode: NotificationMode
        ) = error("Not needed")
    }

    private class FakeAlarmScheduler : AppAlarmScheduler {
        val scheduled = mutableListOf<Int>()
        val scheduledPrecisions = mutableListOf<AlarmPrecision>()
        val cancelled = mutableListOf<Int>()

        override fun schedule(
            targetType: NotificationTargetType,
            targetId: Long,
            scheduledAtMillis: Long,
            requestCode: Int,
            precision: AlarmPrecision
        ): Boolean {
            scheduled.add(requestCode)
            scheduledPrecisions.add(precision)
            return true
        }

        override fun cancel(requestCode: Int) {
            cancelled.add(requestCode)
        }
    }

    private class FakeScheduledNotificationDao : ScheduledNotificationDao {
        val entities = mutableListOf<ScheduledNotificationEntity>()

        override suspend fun insert(entity: ScheduledNotificationEntity) {
            entities.removeAll { it.requestCode == entity.requestCode }
            entities.add(entity)
        }

        override suspend fun insertAll(entities: List<ScheduledNotificationEntity>) {
            entities.forEach { insert(it) }
        }

        override suspend fun getAll(): List<ScheduledNotificationEntity> = entities

        override suspend fun getByTargetType(targetType: String): List<ScheduledNotificationEntity> {
            return entities.filter { it.targetType == targetType }
        }

        override suspend fun getByTargetTypeAndOccurrenceKind(
            targetType: String,
            occurrenceKind: String
        ): List<ScheduledNotificationEntity> {
            return entities.filter {
                it.targetType == targetType && it.occurrenceKind == occurrenceKind
            }
        }

        override suspend fun getByTargetTypeAndOccurrenceKeyPrefix(
            targetType: String,
            occurrenceKeyPrefix: String
        ): List<ScheduledNotificationEntity> {
            return entities.filter {
                it.targetType == targetType && it.occurrenceKey.startsWith(occurrenceKeyPrefix)
            }
        }

        override suspend fun getByTarget(
            targetType: String,
            targetId: Long
        ): List<ScheduledNotificationEntity> {
            return entities.filter { it.targetType == targetType && it.targetId == targetId }
        }

        override suspend fun getByRequestCode(requestCode: Int): ScheduledNotificationEntity? {
            return entities.firstOrNull { it.requestCode == requestCode }
        }

        override suspend fun deleteByRequestCode(requestCode: Int) {
            entities.removeAll { it.requestCode == requestCode }
        }

        override suspend fun deleteByTargetType(targetType: String) {
            entities.removeAll { it.targetType == targetType }
        }

        override suspend fun deleteByTargetTypeAndOccurrenceKind(
            targetType: String,
            occurrenceKind: String
        ) {
            entities.removeAll {
                it.targetType == targetType && it.occurrenceKind == occurrenceKind
            }
        }

        override suspend fun deleteByTargetTypeAndOccurrenceKeyPrefix(
            targetType: String,
            occurrenceKeyPrefix: String
        ) {
            entities.removeAll {
                it.targetType == targetType && it.occurrenceKey.startsWith(occurrenceKeyPrefix)
            }
        }

        override suspend fun deleteByTargetTypeExceptOccurrenceKeyPrefix(
            targetType: String,
            occurrenceKeyPrefix: String
        ) {
            entities.removeAll {
                it.targetType == targetType && !it.occurrenceKey.startsWith(occurrenceKeyPrefix)
            }
        }

        override suspend fun deleteByTarget(targetType: String, targetId: Long) {
            entities.removeAll { it.targetType == targetType && it.targetId == targetId }
        }

        override suspend fun deleteAll() {
            entities.clear()
        }
    }
}
