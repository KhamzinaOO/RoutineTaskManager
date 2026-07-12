package com.okhamzina.routinetaskmanager.core.notifications

import androidx.lifecycle.viewModelScope
import com.okhamzina.routinetaskmanager.core.error.ErrorReporter
import com.okhamzina.routinetaskmanager.core.error.runAppResultCatching
import com.okhamzina.routinetaskmanager.core.notifications.domain.ExactAlarmAccessRepository
import com.okhamzina.routinetaskmanager.core.notifications.domain.ScheduledNotificationRepository
import com.okhamzina.routinetaskmanager.core.presentation.model.MviViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class NotificationAccessViewModel(
    private val accessRepository: ExactAlarmAccessRepository,
    scheduledNotificationRepository: ScheduledNotificationRepository,
    private val rescheduleAllNotificationsUseCase: RescheduleAllNotificationsUseCase,
    private val errorReporter: ErrorReporter
) : MviViewModel<NotificationAccessUiState, NotificationAccessIntent, Nothing>(
    initialState = NotificationAccessUiState(
        hasExactAlarmAccess = accessRepository.hasExactAlarmAccess(),
        hasNotificationAccess = accessRepository.hasNotificationAccess(),
        hasScheduledNotifications = false,
        isExactWarningDismissedForever = accessRepository.isExactWarningDismissedForever(),
        isNotificationWarningDismissedForever = accessRepository.isNotificationWarningDismissedForever()
    )
) {

    init {
        scheduledNotificationRepository.observeHasScheduledNotifications()
            .distinctUntilChanged()
            .onEach { hasScheduledNotifications ->
                updateState { state ->
                    state.copy(hasScheduledNotifications = hasScheduledNotifications)
                }
            }
            .catch { throwable -> errorReporter.record(throwable) }
            .launchIn(viewModelScope)
    }

    override fun onIntent(intent: NotificationAccessIntent) {
        when (intent) {
            NotificationAccessIntent.AppResumed -> refreshAccess()
            NotificationAccessIntent.DismissNotificationWarningForever -> {
                accessRepository.dismissNotificationWarningForever()
                updateState { state ->
                    state.copy(isNotificationWarningDismissedForever = true)
                }
            }

            NotificationAccessIntent.DismissExactWarningForever -> {
                accessRepository.dismissExactWarningForever()
                updateState { state ->
                    state.copy(isExactWarningDismissedForever = true)
                }
            }
        }
    }

    private fun refreshAccess() {
        val hadExactAccess = currentState.hasExactAlarmAccess
        val hasExactAccess = accessRepository.hasExactAlarmAccess()

        updateState { state ->
            state.copy(
                hasExactAlarmAccess = hasExactAccess,
                hasNotificationAccess = accessRepository.hasNotificationAccess(),
                isExactWarningDismissedForever = accessRepository.isExactWarningDismissedForever(),
                isNotificationWarningDismissedForever = accessRepository.isNotificationWarningDismissedForever()
            )
        }

        if (hadExactAccess != hasExactAccess) {
            viewModelScope.launch {
                runAppResultCatching(errorReporter) {
                    rescheduleAllNotificationsUseCase()
                }
            }
        }
    }
}

sealed interface NotificationAccessIntent {
    data object AppResumed : NotificationAccessIntent
    data object DismissNotificationWarningForever : NotificationAccessIntent
    data object DismissExactWarningForever : NotificationAccessIntent
}

data class NotificationAccessUiState(
    val hasExactAlarmAccess: Boolean,
    val hasNotificationAccess: Boolean,
    val hasScheduledNotifications: Boolean,
    val isExactWarningDismissedForever: Boolean,
    val isNotificationWarningDismissedForever: Boolean
) {
    val shouldShowNotificationWarning: Boolean
        get() = hasScheduledNotifications &&
                !hasNotificationAccess &&
                !isNotificationWarningDismissedForever

    val shouldShowExactAlarmWarning: Boolean
        get() = hasScheduledNotifications &&
                !hasExactAlarmAccess &&
                !isExactWarningDismissedForever
}
