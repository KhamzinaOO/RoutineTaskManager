package com.example.routinetaskmanager.featureHome

import com.example.routinetaskmanager.core.presentation.model.UiText

sealed interface HomeEffect{
    data class ShowMessage(val message: UiText) : HomeEffect
}
