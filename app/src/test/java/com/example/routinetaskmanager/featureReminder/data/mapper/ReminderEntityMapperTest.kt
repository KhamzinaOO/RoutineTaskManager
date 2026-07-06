package com.example.routinetaskmanager.featureReminder.data.mapper

import com.example.routinetaskmanager.featureReminder.data.local.ReminderEntity
import com.example.routinetaskmanager.featureReminder.data.local.ReminderImageEntity
import com.example.routinetaskmanager.featureReminder.data.local.ReminderWithImages
import com.example.routinetaskmanager.featureReminder.domain.model.NotificationMode
import com.example.routinetaskmanager.featureReminder.domain.model.Reminder
import com.example.routinetaskmanager.featureReminder.domain.model.ReminderImage
import com.example.routinetaskmanager.featureReminder.domain.model.ReminderRepeatRule
import com.example.routinetaskmanager.featureReminder.domain.model.RepeatInterval
import com.example.routinetaskmanager.featureReminder.domain.model.RepeatUnit
import org.junit.Assert.assertEquals
import org.junit.Test

class ReminderEntityMapperTest {

    @Test
    fun reminderToEntity_serializesRepeatRuleAndSimpleFields() {
        val reminder = Reminder(
            id = 7L,
            name = "Drink water",
            instructionsText = "One glass",
            repeatRule = ReminderRepeatRule.AfterAnother(
                waitInterval = RepeatInterval(15, RepeatUnit.MINUTES)
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
        assertEquals("AFTER_ANOTHER", entity.repeatType)
        assertEquals("VIBRATION", entity.notificationMode)
        assertEquals(false, entity.isEnabled)
        assertEquals(
            reminder.repeatRule,
            ReminderRepeatRuleJsonMapper.fromJson(entity.repeatRuleJson)
        )
    }

    @Test
    fun reminderWithImagesToDomain_restoresReminderAndImages() {
        val rule = ReminderRepeatRule.AfterAnother(
            waitInterval = RepeatInterval(1, RepeatUnit.HOURS)
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
