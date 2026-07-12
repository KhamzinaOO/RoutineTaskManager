package com.okhamzina.routinetaskmanager.core.presentation.model

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

abstract class MviViewModel<State, Intent, Effect>(
    initialState: State
) : ViewModel() {

    private val mutableUiState = MutableStateFlow(initialState)
    val uiState: StateFlow<State> = mutableUiState.asStateFlow()

    private val effectChannel = Channel<Effect>(Channel.BUFFERED)
    val effects: Flow<Effect> = effectChannel.receiveAsFlow()

    protected val currentState: State
        get() = mutableUiState.value

    abstract fun onIntent(intent: Intent)

    protected fun updateState(reducer: (State) -> State) {
        mutableUiState.update(reducer)
    }

    protected fun sendEffect(effect: Effect) {
        effectChannel.trySend(effect)
    }
}
