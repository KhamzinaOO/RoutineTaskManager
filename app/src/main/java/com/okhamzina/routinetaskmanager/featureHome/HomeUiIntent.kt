package com.okhamzina.routinetaskmanager.featureHome

import com.okhamzina.routinetaskmanager.featureReminder.domain.model.ReminderOccurrence
import java.time.LocalDate

sealed interface HomeUiIntent {
    data object OnSessionButtonClick : HomeUiIntent
    data class OnNextReminderDoneClick(
        val occurrence: ReminderOccurrence
    ) : HomeUiIntent

    data class OnNextReminderSkipClick(
        val occurrence: ReminderOccurrence
    ) : HomeUiIntent

    data object NotificationPermissionDenied : HomeUiIntent
    data class DateClick(
        val date : LocalDate
    ) : HomeUiIntent
    data object AddReminderClick : HomeUiIntent
    data object AddTaskClick : HomeUiIntent
}
