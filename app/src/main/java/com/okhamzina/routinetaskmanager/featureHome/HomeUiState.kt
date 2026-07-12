package com.okhamzina.routinetaskmanager.featureHome

import com.okhamzina.routinetaskmanager.R
import com.okhamzina.routinetaskmanager.core.presentation.model.UiText
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.ReminderOccurrence
import java.time.LocalDateTime

data class HomeUiState (
    val greetingText : UiText = UiText.StringResource(R.string.greeting_morning),
    val dateText : String = "",
    val currentDateTime: LocalDateTime = LocalDateTime.now(),
    val reminders: List<ReminderOccurrence> = emptyList(),
    val nextOccurrence: ReminderOccurrence? = null,
    val isSessionActive: Boolean = false,
    val sessionStartedAtMillis: Long? = null,
    val sessionReminderCount: Int = 0,
    val isSessionActionInProgress: Boolean = false,
    val selectedScheduleSection: HomeScheduleSection = HomeScheduleSection.REMINDERS
)

enum class HomeScheduleSection {
    REMINDERS,
    TASKS
}
