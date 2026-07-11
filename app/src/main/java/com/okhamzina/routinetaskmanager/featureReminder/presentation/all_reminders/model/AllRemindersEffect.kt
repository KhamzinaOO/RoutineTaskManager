package com.okhamzina.routinetaskmanager.featureReminder.presentation.all_reminders.model

import com.okhamzina.routinetaskmanager.core.presentation.model.UiText

sealed interface AllRemindersEffect {
    data object FABClicked : AllRemindersEffect
    data object MenuButtonClicked : AllRemindersEffect
    data class ItemClicked (val id : Long) : AllRemindersEffect

    data class EditClicked (val id : Long) : AllRemindersEffect

    data class OpenClicked (val id : Long) : AllRemindersEffect

    data class ShowMessage (val message : UiText) : AllRemindersEffect
}
