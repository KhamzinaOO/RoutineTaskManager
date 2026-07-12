package com.okhamzina.routinetaskmanager.core.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.okhamzina.routinetaskmanager.core.error.ErrorReporter
import com.okhamzina.routinetaskmanager.core.error.runAppResultCatching
import com.okhamzina.routinetaskmanager.core.notifications.domain.ExactAlarmAccessRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ExactAlarmAccessViewModel(
    private val accessRepository: ExactAlarmAccessRepository,
    private val rescheduleAllNotificationsUseCase: RescheduleAllNotificationsUseCase,
    private val errorReporter: ErrorReporter
) : ViewModel() {

    private val _uiState = MutableStateFlow(readState())
    val uiState: StateFlow<ExactAlarmAccessUiState> = _uiState.asStateFlow()

    fun onAppResumed() {
        val hadAccess = _uiState.value.hasExactAlarmAccess
        val refreshedState = readState()
        _uiState.value = refreshedState

        if (hadAccess != refreshedState.hasExactAlarmAccess) {
            viewModelScope.launch {
                runAppResultCatching(errorReporter) {
                    rescheduleAllNotificationsUseCase()
                }
            }
        }
    }

    fun dismissWarningForever() {
        accessRepository.dismissWarningForever()
        _uiState.update { state ->
            state.copy(isWarningDismissedForever = true)
        }
    }

    private fun readState(): ExactAlarmAccessUiState {
        return ExactAlarmAccessUiState(
            hasExactAlarmAccess = accessRepository.hasExactAlarmAccess(),
            isWarningDismissedForever = accessRepository.isWarningDismissedForever()
        )
    }
}

data class ExactAlarmAccessUiState(
    val hasExactAlarmAccess: Boolean,
    val isWarningDismissedForever: Boolean
) {
    val shouldShowWarning: Boolean
        get() = !hasExactAlarmAccess && !isWarningDismissedForever
}
