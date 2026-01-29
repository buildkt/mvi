package com.buildkt.mvi

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TimeTravelDebuggerTest {
    @Test
    fun `restoreState should call stateHolder restoreState`() =
        runTest {
            val mockStateHolder = MockDebuggableStateHolder()
            val middleware =
                TimeTravelMiddleware<TestState, TestIntent>(
                    enable = true,
                    maxHistorySize = 100,
                    preloadedStates = emptyList(),
                )
            val debugger = TimeTravelDebugger(mockStateHolder, middleware)

            val state = TestState(value = 42)
            debugger.restoreState(state)

            assertEquals(state, mockStateHolder.restoredState)
        }

    @Test
    fun `restoreStateFromHistory should restore state at index`() =
        runTest {
            val mockStateHolder = MockDebuggableStateHolder()
            val middleware =
                TimeTravelMiddleware<TestState, TestIntent>(
                    enable = true,
                    maxHistorySize = 100,
                    preloadedStates = emptyList(),
                )

            middleware.initialize(TestState(value = 0))
            middleware.onStateReduced(TestState(value = 1), TestIntent.Increment)
            middleware.onStateReduced(TestState(value = 2), TestIntent.Increment)

            val debugger = TimeTravelDebugger(mockStateHolder, middleware)

            val success = debugger.restoreStateFromHistory(1)

            assertTrue(success)
            assertEquals(TestState(value = 1), mockStateHolder.restoredState)
        }

    @Test
    fun `restoreStateFromHistory should return false for invalid index`() =
        runTest {
            val mockStateHolder = MockDebuggableStateHolder()
            val middleware =
                TimeTravelMiddleware<TestState, TestIntent>(
                    enable = true,
                    maxHistorySize = 100,
                    preloadedStates = emptyList(),
                )

            val debugger = TimeTravelDebugger(mockStateHolder, middleware)

            val success = debugger.restoreStateFromHistory(100)

            assertFalse(success)
            assertNull(mockStateHolder.restoredState)
        }

    @Test
    fun `getHistoryStateFlow should return middleware history`() =
        runTest {
            val mockStateHolder = MockDebuggableStateHolder()
            val middleware =
                TimeTravelMiddleware<TestState, TestIntent>(
                    enable = true,
                    maxHistorySize = 100,
                    preloadedStates = emptyList(),
                )

            middleware.initialize(TestState(value = 0))
            middleware.onStateReduced(TestState(value = 1), TestIntent.Increment)

            val debugger = TimeTravelDebugger(mockStateHolder, middleware)

            val history = debugger.getHistoryStateFlow()
            assertNotNull(history)
            val historyList = history!!.first()
            assertEquals(2, historyList.size)
        }

    @Test
    fun `getCurrentIndexStateFlow should return middleware current index`() =
        runTest {
            val mockStateHolder = MockDebuggableStateHolder()
            val middleware =
                TimeTravelMiddleware<TestState, TestIntent>(
                    enable = true,
                    maxHistorySize = 100,
                    preloadedStates = emptyList(),
                )

            middleware.initialize(TestState(value = 0))
            middleware.onStateReduced(TestState(value = 1), TestIntent.Increment)

            val debugger = TimeTravelDebugger(mockStateHolder, middleware)

            val currentIndex = debugger.getCurrentIndexStateFlow()
            assertNotNull(currentIndex)
            assertEquals(1, currentIndex!!.first())
        }

    private class MockDebuggableStateHolder(
        private val shouldThrow: Boolean = false,
    ) : DebuggableStateHolder<TestState, TestIntent, String, String> {
        var restoredState: TestState? = null
        val intentCalls = mutableListOf<TestIntent>()

        private val _uiState = MutableStateFlow(TestState(value = 0))
        override val uiState: StateFlow<TestState> = _uiState.asStateFlow()

        private val _navigationEvents = MutableSharedFlow<String>()
        override val navigationEvents: SharedFlow<String> = _navigationEvents.asSharedFlow()

        private val _uiEvents = MutableSharedFlow<String>()
        override val uiEvents: SharedFlow<String> = _uiEvents.asSharedFlow()

        override fun restoreState(state: TestState) {
            restoredState = state
            _uiState.value = state
        }

        override fun onIntent(intent: TestIntent) {
            intentCalls.add(intent)
            if (shouldThrow) {
                throw RuntimeException("Test error")
            }
        }
    }

    private data class TestState(
        val value: Int,
    )

    private sealed interface TestIntent {
        data object Increment : TestIntent

        data object Decrement : TestIntent
    }
}
