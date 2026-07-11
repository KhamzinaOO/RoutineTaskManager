package com.okhamzina.routinetaskmanager.featureReminder.data.mapper

import com.okhamzina.routinetaskmanager.featureReminder.data.local.ReminderEntity
import com.okhamzina.routinetaskmanager.featureReminder.data.local.ReminderImageEntity
import com.okhamzina.routinetaskmanager.featureReminder.data.local.ReminderWithImages
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.NotificationMode
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.OnScheduleCertainDayRepeat
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.Reminder
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.ReminderImage
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.ReminderRepeatRule
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.RepeatScheduleMode
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.WeeklyRepeat
import java.time.DayOfWeek
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Test

class ReminderEntityMapperTest {

    @Test
    fun reminderToEntity_serializesRepeatRuleAndSimpleFields() {
        val reminder = Reminder(
            id = 7L,
            name = "Drink water",
            instructionsText = "One glass",
            repeatRule = ReminderRepeatRule.OnScheduleCertain(
                schedule = WeeklyRepeat(
                    mode = RepeatScheduleMode.DEFAULT,
                    selectedDays = setOf(DayOfWeek.MONDAY),
                    defaultValue = OnScheduleCertainDayRepeat(
                        pickedTimes = setOf(LocalTime.of(9, 0))
                    ),
                    advancedEntries = emptyList()
                )
            ),
            notificationMode = NotificationMode.VIBRATION,
            images = listOf(
                ReminderImage(
                    id = 1L,
                    reminderId = 7L,
                    imagePath = "image.jpg",
                    sortOrder = 0
                )
            ),
            createdAt = 100L,
            updatedAt = 200L,
            isEnabled = false,
            notificationEnabled = true
        )

        val entity = reminder.toEntity()

        assertEquals(7L, entity.id)
        assertEquals("Drink water", entity.name)
        assertEquals("ON_SCHEDULE_CERTAIN", entity.repeatType)
        assertEquals("VIBRATION", entity.notificationMode)
        assertEquals(false, entity.isEnabled)
        assertEquals(
            reminder.repeatRule,
            ReminderRepeatRuleJsonMapper.fromJson(entity.repeatRuleJson)
        )
    }

    @Test
    fun reminderWithImagesToDomain_restoresReminderAndImages() {
        val rule = ReminderRepeatRule.OnScheduleCertain(
            schedule = WeeklyRepeat(
                mode = RepeatScheduleMode.DEFAULT,
                selectedDays = setOf(DayOfWeek.TUESDAY),
                defaultValue = OnScheduleCertainDayRepeat(
                    pickedTimes = setOf(LocalTime.of(8, 30))
                ),
                advancedEntries = emptyList()
            )
        )
        val entity = ReminderEntity(
            id = 3L,
            name = "Stretch",
            instructionsText = null,
            repeatType = rule.toRepeatType(),
            repeatRuleJson = ReminderRepeatRuleJsonMapper.toJson(rule),
            notificationMode = NotificationMode.SOUND.name,
            createdAt = 10L,
            updatedAt = 20L,
            isEnabled = true,
            notificationEnabled = false
        )
        val image = ReminderImageEntity(
            id = 9L,
            reminderId = 3L,
            imagePath = "stretch.png",
            sortOrder = 2,
            createdAt = 30L
        )

        val domain = ReminderWithImages(
            reminder = entity,
            images = listOf(image)
        ).toDomain()

        assertEquals(3L, domain.id)
        assertEquals(rule, domain.repeatRule)
        assertEquals(NotificationMode.SOUND, domain.notificationMode)
        assertEquals(false, domain.notificationEnabled)
        assertEquals(
            listOf(
                ReminderImage(
                    id = 9L,
                    reminderId = 3L,
                    imagePath = "stretch.png",
                    sortOrder = 2
                )
            ),
            domain.images
        )
    }
}
