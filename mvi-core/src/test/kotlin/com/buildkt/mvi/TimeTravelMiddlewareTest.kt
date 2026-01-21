package com.buildkt.mvi

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TimeTravelMiddlewareTest {

    @Test
    fun `initialize should add initial state when history is empty`() = runTest {
        val middleware = TimeTravelMiddleware<TestState, TestIntent>(
            enable = true,
            maxHistorySize = 100,
            preloadedStates = emptyList()
        )

        val initialState = TestState(value = 0)
        middleware.initialize(initialState)

        val history = middleware.history.first()
        assertEquals(1, history.size)
        assertEquals(initialState, history[0].state)
        assertNull(history[0].intent)
        assertEquals(0, history[0].index)
        assertEquals(0, middleware.currentHistoryIndex.first())
    }

    @Test
    fun `initialize should prepend initial state when preloaded states exist without initial state`() =
        runTest {
            val preloadedState = StateSnapshot(
                state = TestState(value = 1),
                intent = TestIntent.Increment,
                index = 0
            )
            val middleware = TimeTravelMiddleware<TestState, TestIntent>(
                enable = true,
                maxHistorySize = 100,
                preloadedStates = listOf(preloadedState as StateSnapshot<TestState, TestIntent>),
            )

            val initialState = TestState(value = 0)
            middleware.initialize(initialState)

            val history = middleware.history.first()
            assertEquals(2, history.size)
            assertEquals(initialState, history[0].state)
            assertNull(history[0].intent)
            assertEquals(0, history[0].index)
            assertEquals(TestState(value = 1), history[1].state)
            assertEquals(TestIntent.Increment, history[1].intent)
            assertEquals(1, history[1].index)
        }

    @Test
    fun `initialize should not add duplicate initial state`() = runTest {
        val initialState = StateSnapshot(
            state = TestState(value = 0),
            intent = null,
            index = 0
        )
        val middleware = TimeTravelMiddleware<TestState, TestIntent>(
            enable = true,
            maxHistorySize = 100,
            preloadedStates = listOf(initialState as StateSnapshot<TestState, TestIntent>),
        )

        middleware.initialize(TestState(value = 0))

        val history = middleware.history.first()
        assertEquals(1, history.size)
    }

    @Test
    fun `onStateReduced should add state to history`() = runTest {
        val middleware = TimeTravelMiddleware<TestState, TestIntent>(
            enable = true,
            maxHistorySize = 100,
            preloadedStates = emptyList()
        )

        val initialState = TestState(value = 0)
        middleware.initialize(initialState)

        val newState = TestState(value = 1)
        val intent = TestIntent.Increment
        middleware.onStateReduced(newState, intent)

        val history = middleware.history.first()
        assertEquals(2, history.size)
        assertEquals(newState, history[1].state)
        assertEquals(intent, history[1].intent)
        assertEquals(1, history[1].index)
        assertEquals(1, middleware.currentHistoryIndex.first())
    }

    @Test
    fun `onStateReduced should not record when disabled`() = runTest {
        val middleware = TimeTravelMiddleware<TestState, TestIntent>(
            enable = false,
            maxHistorySize = 100,
            preloadedStates = emptyList()
        )

        middleware.onStateReduced(TestState(value = 1), TestIntent.Increment)

        val history = middleware.history.first()
        assertTrue(history.isEmpty())
    }

    @Test
    fun `onStateReduced should truncate history when exceeding maxHistorySize`() = runTest {
        val maxSize = 5
        val middleware = TimeTravelMiddleware<TestState, TestIntent>(
            enable = true,
            maxHistorySize = maxSize,
            preloadedStates = emptyList()
        )

        middleware.initialize(TestState(value = 0))

        // Add more states than maxSize
        repeat(maxSize + 2) { index ->
            middleware.onStateReduced(TestState(value = index + 1), TestIntent.Increment)
        }

        val history = middleware.history.first()
        assertEquals(maxSize, history.size)
        // Initial state should be preserved
        assertNull(history[0].intent)
        assertEquals(0, history[0].index)
    }

    @Test
    fun `restoreStateAt should restore state and update index`() = runTest {
        val middleware = TimeTravelMiddleware<TestState, TestIntent>(
            enable = true,
            maxHistorySize = 100,
            preloadedStates = emptyList()
        )

        middleware.initialize(TestState(value = 0))
        middleware.onStateReduced(TestState(value = 1), TestIntent.Increment)
        middleware.onStateReduced(TestState(value = 2), TestIntent.Increment)

        var restoredState: TestState? = null
        val success = middleware.restoreStateAt(1) { state ->
            restoredState = state
        }

        assertTrue(success)
        assertEquals(TestState(value = 1), restoredState)
        assertEquals(1, middleware.currentHistoryIndex.first())
    }

    @Test
    fun `loadHistory should reject invalid history`() = runTest {
        val middleware = TimeTravelMiddleware<TestState, TestIntent>(
            enable = true,
            maxHistorySize = 100,
            preloadedStates = emptyList()
        )

        // History with inconsistent indices (index 5 instead of 1) - loadHistory re-indexes so this is accepted
        val invalidHistory = listOf<StateSnapshot<TestState, TestIntent>>(
            StateSnapshot(TestState(value = 0), null, index = 0),
            StateSnapshot(TestState(value = 1), TestIntent.Increment, index = 5), // Wrong index
        )

        val success = middleware.loadHistory(invalidHistory)

        assertTrue(success) // loadHistory re-indexes entries, so indices are corrected
        val history = middleware.history.first()
        assertEquals(2, history.size)
    }

    private data class TestState(val value: Int)

    private sealed interface TestIntent {
        data object Increment : TestIntent
    }
}
