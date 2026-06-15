package com.example.routinetaskmanager.featureReminder.presentation.reminder_main.model

import com.example.routinetaskmanager.featureReminder.presentation.all_reminders.model.AllRemindersEffect

sealed interface ReminderMainEffect {
    data object OpenDrawer : ReminderMainEffect
    data object FABClicked : ReminderMainEffect
}