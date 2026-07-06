package com.example.routinetaskmanager.data.local

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.routinetaskmanager.core.notifications.api.NotificationOccurrenceKind
import com.example.routinetaskmanager.core.notifications.api.NotificationTargetType
import com.example.routinetaskmanager.data.local.notifications.ScheduledNotificationDao
import com.example.routinetaskmanager.data.local.notifications.ScheduledNotificationEntity
import com.example.routinetaskmanager.featureReminder.data.local.ReminderDao
import com.example.routinetaskmanager.featureReminder.data.local.ReminderEntity
import com.example.routinetaskmanager.featureReminder.data.local.ReminderImageEntity
import com.example.routinetaskmanager.featureReminder.data.local.ReminderOccurrenceDAO
import com.example.routinetaskmanager.featureReminder.data.local.ReminderOccurrenceStateEntity
import com.example.routinetaskmanager.featureReminder.data.mapper.ReminderRepeatRuleJsonMapper
import com.example.routinetaskmanager.featureReminder.domain.model.NotificationMode
import com.example.routinetaskmanager.featureReminder.domain.model.OnScheduleCertainDayRepeat
import com.example.routinetaskmanager.featureReminder.domain.model.ReminderOccurrenceStatus
import com.example.routinetaskmanager.featureReminder.domain.model.ReminderRepeatRule
import com.example.routinetaskmanager.featureReminder.domain.model.RepeatScheduleMode
import com.example.routinetaskmanager.featureReminder.domain.model.WeeklyRepeat
import java.time.DayOfWeek
import java.time.LocalTime
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {

    private lateinit var database: AppDatabase
    private lateinit var reminderDao: ReminderDao
    private lateinit var occurrenceDao: ReminderOccurrenceDAO
    private lateinit var scheduledNotificationDao: ScheduledNotificationDao

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        reminderDao = database.reminderDao()
        occurrenceDao = database.reminderOccurrenceDao()
        scheduledNotificationDao = database.scheduleDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun reminderDao_readsImagesOrderedBySortOrder() = runBlocking {
        val reminderId = reminderDao.insertReminder(reminderEntity())

        reminderDao.insertReminderImages(
            listOf(
                ReminderImageEntity(
                    reminderId = reminderId,
                    imagePath = "second.png",
                    sortOrder = 2
                ),
                ReminderImageEntity(
                    reminderId = reminderId,
                    imagePath = "first.png",
                    sortOrder = 1
                )
            )
        )

        val images = reminderDao.getImagesByReminderId(reminderId)

        assertEquals(listOf("first.png", "second.png"), images.map { it.imagePath })
    }

    @Test
    fun deletingReminder_cascadesToImagesAndOccurrenceStates() = runBlocking {
        val reminderId = reminderDao.insertReminder(reminderEntity())
        reminderDao.insertReminderImage(
            ReminderImageEntity(
                reminderId = reminderId,
                imagePath = "image.png",
                sortOrder = 0
            )
        )
        occurrenceDao.upsertState(
            ReminderOccurrenceStateEntity(
                occurrenceKey = "REMINDER-$reminderId-1000",
                reminderId = reminderId,
                scheduledAtMillis = 1_000L,
                status = ReminderOccurrenceStatus.SKIPPED.name,
                actedAtMillis = 2_000L,
                occurrenceKind = NotificationOccurrenceKind.REGULAR.name
            )
        )

        reminderDao.deleteReminderById(reminderId)

        assertTrue(reminderDao.getImagesByReminderId(reminderId).isEmpty())
        assertNull(occurrenceDao.getStateByKey("REMINDER-$reminderId-1000"))
    }

    @Test
    fun scheduledNotificationDao_filtersAndDeletesByTargetAndOccurrenceKind() = runBlocking {
        scheduledNotificationDao.insertAll(
            listOf(
                scheduledNotification(
                    requestCode = 1,
                    targetType = NotificationTargetType.REMINDER.name,
                    occurrenceKey = "REMINDER-1",
                    occurrenceKind = NotificationOccurrenceKind.REGULAR.name
                ),
                scheduledNotification(
                    requestCode = 2,
                    targetType = NotificationTargetType.REMINDER.name,
                    occurrenceKey = "REMINDER-SESSION-1",
                    occurrenceKind = NotificationOccurrenceKind.SESSION.name
                ),
                scheduledNotification(
                    requestCode = 3,
                    targetType = NotificationTargetType.TASK.name,
                    occurrenceKey = "TASK-1",
                    occurrenceKind = NotificationOccurrenceKind.REGULAR.name
                )
            )
        )

        val reminderRegularNotifications =
            scheduledNotificationDao.getByTargetTypeAndOccurrenceKind(
                targetType = NotificationTargetType.REMINDER.name,
                occurrenceKind = NotificationOccurrenceKind.REGULAR.name
            )

        assertEquals(
            listOf("REMINDER-1"),
            reminderRegularNotifications.map { it.occurrenceKey }
        )
        assertEquals(
            2,
            scheduledNotificationDao
                .getByTargetType(NotificationTargetType.REMINDER.name)
                .size
        )

        scheduledNotificationDao.deleteByTargetTypeAndOccurrenceKind(
            targetType = NotificationTargetType.REMINDER.name,
            occurrenceKind = NotificationOccurrenceKind.REGULAR.name
        )

        assertNull(scheduledNotificationDao.getByRequestCode(1))
        assertEquals(
            "REMINDER-SESSION-1",
            scheduledNotificationDao.getByRequestCode(2)?.occurrenceKey
        )
    }

    private fun reminderEntity(): ReminderEntity {
        val rule = ReminderRepeatRule.OnScheduleCertain(
            schedule = WeeklyRepeat(
                mode = RepeatScheduleMode.DEFAULT,
                selectedDays = setOf(DayOfWeek.MONDAY),
                defaultValue = OnScheduleCertainDayRepeat(
                    pickedTimes = setOf(LocalTime.of(9, 0))
                ),
                advancedEntries = emptyList()
            )
        )

        return ReminderEntity(
            name = "Reminder",
            instructionsText = "Instruction",
            repeatType = "ON_SCHEDULE_CERTAIN",
            repeatRuleJson = ReminderRepeatRuleJsonMapper.toJson(rule),
            notificationMode = NotificationMode.MUTE.name,
            createdAt = 100L,
            updatedAt = 100L
        )
    }

    private fun scheduledNotification(
        requestCode: Int,
        targetType: String,
        occurrenceKey: String,
        occurrenceKind: String
    ): ScheduledNotificationEntity {
        return ScheduledNotificationEntity(
            requestCode = requestCode,
            targetType = targetType,
            targetId = 1L,
            scheduledAtMillis = 1_000L,
            occurrenceKey = occurrenceKey,
            occurrenceKind = occurrenceKind,
            createdAtMillis = 500L
        )
    }
}
