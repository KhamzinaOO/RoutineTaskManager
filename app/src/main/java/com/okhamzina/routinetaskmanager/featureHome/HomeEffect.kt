package com.okhamzina.routinetaskmanager.featureHome

import com.okhamzina.routinetaskmanager.core.presentation.model.UiText

sealed interface HomeEffect{
    data class ShowMessage(val message: UiText) : HomeEffect
    data object NavigateCreateReminder : HomeEffect
    data object NavigateTasks : HomeEffect
    data object NavigateToSettings : HomeEffect
}
