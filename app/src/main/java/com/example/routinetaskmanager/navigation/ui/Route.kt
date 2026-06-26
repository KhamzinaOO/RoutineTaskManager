package com.example.routinetaskmanager.navigation.ui

import com.example.routinetaskmanager.R
import kotlinx.serialization.Serializable

sealed interface Route

sealed interface TopRoute : Route {
    val labelRes : Int
    val icon : Int
}

data object Home : TopRoute {
    override val labelRes = R.string.nav_home
    override val icon = R.drawable.ic_home
}

data object Widgets : TopRoute {
    override val labelRes = R.string.nav_widgets
    override val icon = R.drawable.ic_dashboard
}

data object Reminders : TopRoute {
    override val labelRes = R.string.nav_reminders
    override val icon = R.drawable.ic_schedule
}

data object Tasks : TopRoute {
    override val labelRes = R.string.nav_tasks
    override val icon = R.drawable.ic_calendar
}

data object AllReminders : Route

@Serializable
data object CreateReminder : Route

@Serializable
data class EditReminder(
    val reminderId : Long
): Route
@Serializable
data class ReminderInfo(
    val reminderId : Long
): Route
