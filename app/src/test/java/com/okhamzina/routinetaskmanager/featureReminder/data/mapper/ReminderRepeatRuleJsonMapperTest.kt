package com.okhamzina.routinetaskmanager.featureReminder.data.mapper

import com.okhamzina.routinetaskmanager.featureReminder.domain.model.OnScheduleCertainDayRepeat
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.ReminderRepeatRule
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.ReminderRepeatType
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.RepeatScheduleMode
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.WeeklyRepeat
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
        val rule = ReminderRepeatRule.OnScheduleCertain(
            schedule = WeeklyRepeat(
                mode = RepeatScheduleMode.DEFAULT,
                selectedDays = setOf(DayOfWeek.FRIDAY),
                defaultValue = OnScheduleCertainDayRepeat(
                    pickedTimes = setOf(LocalTime.of(12, 15))
                ),
                advancedEntries = emptyList()
            )
        )
        val json = ReminderRepeatRuleJsonMapper
            .toJson(rule)
            .replaceFirst("{", """{"ignored":"value",""")

        val restored = ReminderRepeatRuleJsonMapper.fromJson(json)

        assertEquals(rule, restored)
    }

    @Test
    fun repeatRuleToRepeatType_mapsOnScheduleCertainType() {
        val rule = ReminderRepeatRule.OnScheduleCertain(
            schedule = WeeklyRepeat(
                mode = RepeatScheduleMode.DEFAULT,
                selectedDays = setOf(DayOfWeek.SUNDAY),
                defaultValue = OnScheduleCertainDayRepeat(
                    pickedTimes = setOf(LocalTime.of(7, 0))
                ),
                advancedEntries = emptyList()
            )
        )

        assertEquals("ON_SCHEDULE_CERTAIN", rule.toRepeatType())
        assertEquals(ReminderRepeatType.ON_SCHEDULE_CERTAIN, rule.toRepeatTypeDomain())
    }
}
