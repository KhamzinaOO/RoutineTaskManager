package com.okhamzina.routinetaskmanager.featureHome

import com.okhamzina.routinetaskmanager.featureReminder.domain.model.ReminderOccurrence

sealed interface HomeIntent {
    data object SessionButtonClicked : HomeIntent
    data class NextReminderDoneClicked(
        val occurrence: ReminderOccurrence
    ) : HomeIntent

    data class NextReminderSkipClicked(
        val occurrence: ReminderOccurrence
    ) : HomeIntent

    data object NotificationPermissionDenied : HomeIntent
    data object SettingsClicked : HomeIntent
    data class ScheduleSectionSelected(
        val section: HomeScheduleSection
    ) : HomeIntent

    data object AddScheduleItemClicked : HomeIntent
}
