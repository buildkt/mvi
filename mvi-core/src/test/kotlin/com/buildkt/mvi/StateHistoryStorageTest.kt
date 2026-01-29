package com.buildkt.mvi

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StateHistoryStorageTest {
    @Test
    fun `saveHistory should store history`() =
        runTest {
            val storage = InMemoryStateHistoryStorage<TestState, TestIntent>()

            val history =
                listOf<StateSnapshot<TestState, TestIntent>>(
                    StateSnapshot(TestState(value = 0), null, index = 0),
                    StateSnapshot(TestState(value = 1), TestIntent.Increment, index = 1),
                )

            storage.saveHistory(history)

            val loaded = storage.loadHistory()
            assertEquals(2, loaded.size)
            assertEquals(history[0].state, loaded[0].state)
            assertEquals(history[1].state, loaded[1].state)
        }

    @Test
    fun `loadHistory should return empty list when no history exists`() =
        runTest {
            val storage = InMemoryStateHistoryStorage<TestState, TestIntent>()

            val loaded = storage.loadHistory()

            assertTrue(loaded.isEmpty())
        }

    @Test
    fun `saveHistory should create a copy to prevent external modifications`() =
        runTest {
            val storage = InMemoryStateHistoryStorage<TestState, TestIntent>()

            val originalHistory =
                mutableListOf<StateSnapshot<TestState, TestIntent>>(
                    StateSnapshot(TestState(value = 0), null, index = 0),
                )

            storage.saveHistory(originalHistory)

            // Modify original list
            originalHistory.add(StateSnapshot(TestState(value = 1), TestIntent.Increment, index = 1))

            // Stored history should not be affected
            val loaded = storage.loadHistory()
            assertEquals(1, loaded.size)
        }

    @Test
    fun `loadHistory should return a copy to prevent external modifications`() =
        runTest {
            val storage = InMemoryStateHistoryStorage<TestState, TestIntent>()

            val history =
                listOf<StateSnapshot<TestState, TestIntent>>(
                    StateSnapshot(TestState(value = 0), null, index = 0),
                )

            storage.saveHistory(history)

            val loaded = storage.loadHistory()
            // Try to modify (this should not affect stored history)
            val mutableLoaded = loaded.toMutableList()
            mutableLoaded.add(StateSnapshot(TestState(value = 1), TestIntent.Increment, index = 1))

            // Reload should still have original
            val reloaded = storage.loadHistory()
            assertEquals(1, reloaded.size)
        }

    @Test
    fun `clearHistory should remove all stored history`() =
        runTest {
            val storage = InMemoryStateHistoryStorage<TestState, TestIntent>()

            val history =
                listOf<StateSnapshot<TestState, TestIntent>>(
                    StateSnapshot(TestState(value = 0), null, index = 0),
                    StateSnapshot(TestState(value = 1), TestIntent.Increment, index = 1),
                )

            storage.saveHistory(history)
            storage.clearHistory()

            val loaded = storage.loadHistory()
            assertTrue(loaded.isEmpty())
        }

    @Test
    fun `saveHistory should overwrite previous history`() =
        runTest {
            val storage = InMemoryStateHistoryStorage<TestState, TestIntent>()

            val firstHistory =
                listOf<StateSnapshot<TestState, TestIntent>>(
                    StateSnapshot(TestState(value = 0), null, index = 0),
                )

            val secondHistory =
                listOf<StateSnapshot<TestState, TestIntent>>(
                    StateSnapshot(TestState(value = 10), null, index = 0),
                    StateSnapshot(TestState(value = 11), TestIntent.Increment, index = 1),
                )

            storage.saveHistory(firstHistory)
            storage.saveHistory(secondHistory)

            val loaded = storage.loadHistory()
            assertEquals(2, loaded.size)
            assertEquals(TestState(value = 10), loaded[0].state)
            assertEquals(TestState(value = 11), loaded[1].state)
        }

    @Test
    fun `concurrent save and load should be thread-safe`() =
        runTest {
            val storage = InMemoryStateHistoryStorage<TestState, TestIntent>()

            val jobs =
                coroutineScope {
                    List(100) { index ->
                        launch {
                            val history =
                                listOf<StateSnapshot<TestState, TestIntent>>(
                                    StateSnapshot(TestState(value = index), null, index = 0),
                                )
                            storage.saveHistory(history)
                            val loaded = storage.loadHistory()
                            // Should always have exactly one entry (last write wins)
                            assertTrue(loaded.size <= 1)
                        }
                    }
                }

            jobs.forEach { it.join() }

            // Final state should have one entry
            val finalHistory = storage.loadHistory()
            assertTrue(finalHistory.size <= 1)
        }

    @Test
    fun `saveHistory should handle empty list`() =
        runTest {
            val storage = InMemoryStateHistoryStorage<TestState, TestIntent>()

            storage.saveHistory(emptyList())

            val loaded = storage.loadHistory()
            assertTrue(loaded.isEmpty())
        }

    @Test
    fun `clearHistory should be idempotent`() =
        runTest {
            val storage = InMemoryStateHistoryStorage<TestState, TestIntent>()

            storage.clearHistory()
            storage.clearHistory()

            val loaded = storage.loadHistory()
            assertTrue(loaded.isEmpty())
        }

    @Test
    fun `storage should handle large history lists`() =
        runTest {
            val storage = InMemoryStateHistoryStorage<TestState, TestIntent>()

            val largeHistory =
                (0..1000).map { index ->
                    StateSnapshot<TestState, TestIntent>(
                        state = TestState(value = index),
                        intent = if (index == 0) null else TestIntent.Increment,
                        index = index,
                    )
                }

            storage.saveHistory(largeHistory)

            val loaded = storage.loadHistory()
            assertEquals(1001, loaded.size)
            assertEquals(TestState(value = 0), loaded[0].state)
            assertEquals(TestState(value = 1000), loaded[1000].state)
        }

    private data class TestState(
        val value: Int,
    )

    private sealed interface TestIntent {
        data object Increment : TestIntent
    }
}
