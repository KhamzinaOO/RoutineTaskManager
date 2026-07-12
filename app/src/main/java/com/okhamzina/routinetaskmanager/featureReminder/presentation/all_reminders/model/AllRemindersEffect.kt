package com.okhamzina.routinetaskmanager.featureReminder.presentation.all_reminders.model

import com.okhamzina.routinetaskmanager.core.presentation.model.UiText

sealed interface AllRemindersEffect {
    data object NavigateToCreateReminder : AllRemindersEffect
    data object OpenDrawer : AllRemindersEffect
    data object OpenSearch : AllRemindersEffect
    data class NavigateToReminder(val id: Long) : AllRemindersEffect
    data class NavigateToEditReminder(val id: Long) : AllRemindersEffect
    data class ShowMessage(val message: UiText) : AllRemindersEffect
}
