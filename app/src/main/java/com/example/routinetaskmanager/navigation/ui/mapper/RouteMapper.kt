package com.example.routinetaskmanager.navigation.ui.mapper

import com.example.routinetaskmanager.navigation.ui.AllReminders
import com.example.routinetaskmanager.navigation.ui.CreateReminder
import com.example.routinetaskmanager.navigation.ui.EditReminder
import com.example.routinetaskmanager.navigation.ui.Home
import com.example.routinetaskmanager.navigation.ui.ReminderInfo
import com.example.routinetaskmanager.navigation.ui.Reminders
import com.example.routinetaskmanager.navigation.ui.Route
import com.example.routinetaskmanager.navigation.ui.Tasks
import com.example.routinetaskmanager.navigation.ui.Widgets

fun Route.toSavedString(): String {
    return when (this) {
        Home -> "home"
        Widgets -> "widgets"
        Reminders -> "reminders"
        Tasks -> "tasks"
        AllReminders -> "all_reminders"
        CreateReminder -> "create_reminder"

        is EditReminder -> "edit_reminder:$reminderId"

        is ReminderInfo -> {
            val id = reminderId?.toString().orEmpty()
            "reminder_info:$id"
        }
    }
}

fun String.toRouteOrNull(): Route? {
    return when {
        this == "home" -> Home
        this == "widgets" -> Widgets
        this == "reminders" -> Reminders
        this == "tasks" -> Tasks
        this == "all_reminders" -> AllReminders
        this == "create_reminder" -> CreateReminder

        startsWith("edit_reminder:") -> {
            val id = substringAfter("edit_reminder:").toLongOrNull()
            id?.let { EditReminder(it) }
        }

        startsWith("reminder_info:") -> {
            val rawId = substringAfter("reminder_info:")
            val id = rawId.takeIf { it.isNotBlank() }?.toLongOrNull()
            ReminderInfo(id)
        }

        else -> null
    }
}