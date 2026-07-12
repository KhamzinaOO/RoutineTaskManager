package com.okhamzina.routinetaskmanager.core.presentation.model

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class MviViewModelTest {

    @Test
    fun intentReducesStateThroughSingleStateFlow() {
        val viewModel = TestViewModel()

        viewModel.onIntent(TestIntent.Increment)

        assertEquals(TestState(count = 1), viewModel.uiState.value)
    }

    @Test
    fun oneOffEventIsPublishedAsEffect() = runBlocking {
        val viewModel = TestViewModel()

        viewModel.onIntent(TestIntent.Submit)

        assertEquals(TestEffect.Submitted, viewModel.effects.first())
    }

    private class TestViewModel : MviViewModel<TestState, TestIntent, TestEffect>(TestState()) {
        override fun onIntent(intent: TestIntent) {
            when (intent) {
                TestIntent.Increment -> updateState { state ->
                    state.copy(count = state.count + 1)
                }

                TestIntent.Submit -> sendEffect(TestEffect.Submitted)
            }
        }
    }

    private data class TestState(val count: Int = 0)

    private sealed interface TestIntent {
        data object Increment : TestIntent
        data object Submit : TestIntent
    }

    private sealed interface TestEffect {
        data object Submitted : TestEffect
    }
}
