package com.example.routinetaskmanager.featureReminder.data.mapper

import com.example.routinetaskmanager.featureReminder.domain.model.OnScheduleCertainDayRepeat
import com.example.routinetaskmanager.featureReminder.domain.model.ReminderRepeatRule
import com.example.routinetaskmanager.featureReminder.domain.model.ReminderRepeatType
import com.example.routinetaskmanager.featureReminder.domain.model.RepeatInterval
import com.example.routinetaskmanager.featureReminder.domain.model.RepeatScheduleMode
import com.example.routinetaskmanager.featureReminder.domain.model.RepeatUnit
import com.example.routinetaskmanager.featureReminder.domain.model.WeeklyRepeat
import java.time.DayOfWeek
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Test

class ReminderRepeatRuleJsonMapperTest {

    @Test
    fun toJsonAndFromJson_roundTripsSealedRepeatRule() {
        val rule = ReminderRepeatRule.OnScheduleCertain(
            schedule = WeeklyRepeat(
                mode = RepeatScheduleMode.DEFAULT,
                selectedDays = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),
                defaultValue = OnScheduleCertainDayRepeat(
                    pickedTimes = setOf(LocalTime.of(9, 0), LocalTime.of(18, 30))
                ),
                advancedEntries = emptyList()
            )
        )

        val json = ReminderRepeatRuleJsonMapper.toJson(rule)
        val restored = ReminderRepeatRuleJsonMapper.fromJson(json)

        assertEquals(rule, restored)
    }

    @Test
    fun fromJson_ignoresUnknownFields() {
        val rule = ReminderRepeatRule.AfterAnother(
            waitInterval = RepeatInterval(2, RepeatUnit.HOURS)
        )
        val json = ReminderRepeatRuleJsonMapper
            .toJson(rule)
            .replaceFirst("{", """{"ignored":"value",""")

        val restored = ReminderRepeatRuleJsonMapper.fromJson(json)

        assertEquals(rule, restored)
    }

    @Test
    fun repeatRuleToRepeatType_mapsAfterAnotherType() {
        val rule = ReminderRepeatRule.AfterAnother(
            waitInterval = RepeatInterval(1, RepeatUnit.MINUTES)
        )

        assertEquals("AFTER_ANOTHER", rule.toRepeatType())
        assertEquals(ReminderRepeatType.AFTER_ANOTHER_ACTIVITY, rule.toRepeatTypeDomain())
    }
}
