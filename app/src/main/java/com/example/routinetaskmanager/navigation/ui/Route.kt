package com.example.routinetaskmanager.navigation.ui

import com.example.routinetaskmanager.R
import kotlinx.serialization.Serializable

sealed interface Route

sealed interface TopRoute : Route {
    val label : String
    val icon : Int
}

data object Home : TopRoute {
    override val label = "Home"
    override val icon = R.drawable.ic_home
}

data object Widgets : TopRoute {
    override val label = "Widgets"
    override val icon = R.drawable.ic_dashboard
}

data object Reminders : TopRoute {
    override val label = "Reminders"
    override val icon = R.drawable.ic_schedule
}

data object Tasks : TopRoute {
    override val label = "Tasks"
    override val icon = R.drawable.ic_calendar
}

data object AllReminders : Route

@Serializable
data class CreateReminder(
    val reminderId : Long? = null
) : Route
@Serializable
data class ReminderInfo(
    val reminderId : Long? = null
): Route
