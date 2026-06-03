package com.example.routinetaskmanager.featureReminder.data.mapper

import com.example.routinetaskmanager.featureReminder.domain.model.ReminderRepeatRule
import kotlinx.serialization.json.Json

object ReminderRepeatRuleJsonMapper {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        classDiscriminator = "type"
    }

    fun toJson(
        rule: ReminderRepeatRule
    ): String {
        return json.encodeToString<ReminderRepeatRule>(rule)
    }

    fun fromJson(
        value: String
    ): ReminderRepeatRule {
        return json.decodeFromString<ReminderRepeatRule>(value)
    }
}