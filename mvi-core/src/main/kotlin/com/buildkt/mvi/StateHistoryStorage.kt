package com.buildkt.mvi

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Interface for persisting time-travel debugging history.
 * Implementations should handle serialization/deserialization of state and intent types.
 *
 * **Serialization validation:**
 * State and intent types are generic and may not be serializable. Implementations that persist
 * to disk (e.g. JSON, Parcelable) should validate that state and intent are serializable and
 * throw a clear exception when they are not, for example:
 * `throw IllegalStateException("State type ${State::class.simpleName} must be serializable for persistence. Consider using @Serializable or Parcelable.")`
 *
 * Use a serialization library (kotlinx.serialization, Gson, Moshi) and catch serialization
 * errors to rethrow with a descriptive message so callers can fix their types.
 */
interface StateHistoryStorage<S, I> {
    /**
     * Saves the history to persistent storage.
     *
     * @param history The list of state histories to save.
     * @throws IllegalStateException If state or intent types are not serializable (implementations
     * that persist to disk should validate and throw with a clear message).
     */
    suspend fun saveHistory(history: List<StateSnapshot<S, I>>)

    /**
     * Loads the history from persistent storage.
     *
     * @return The list of state histories, or empty list if none exists.
     */
    suspend fun loadHistory(): List<StateSnapshot<S, I>>

    /**
     * Clears the persisted history.
     */
    suspend fun clearHistory()
}

/**
 * An in-memory implementation of [StateHistoryStorage] that stores state history in memory.
 *
 * This implementation is useful for:
 * - Testing and development
 * - Scenarios where persistence across app restarts is not needed
 * - Temporary debugging sessions
 *
 * **Note**: The history is stored in memory and will be lost when the instance is garbage collected
 * or the application process is terminated.
 *
 * **Thread Safety**: This implementation is thread-safe and can be used from multiple coroutines.
 *
 * @param S The type of the state.
 * @param I The type of the intent.
 */
class InMemoryStateHistoryStorage<S, I> : StateHistoryStorage<S, I> {
    private val mutex = Mutex()
    private var storedHistory: List<StateSnapshot<S, I>> = emptyList()

    /**
     * Saves the history to memory.
     *
     * @param history The list of state snapshots to save.
     */
    override suspend fun saveHistory(history: List<StateSnapshot<S, I>>) {
        mutex.withLock {
            storedHistory = history.toList() // Create a copy to prevent external modifications
        }
    }

    /**
     * Loads the history from memory.
     *
     * @return The list of state snapshots, or empty list if none exists.
     */
    override suspend fun loadHistory(): List<StateSnapshot<S, I>> =
        mutex.withLock {
            storedHistory.toList() // Return a copy to prevent external modifications
        }

    /**
     * Clears the stored history from memory.
     */
    override suspend fun clearHistory() {
        mutex.withLock {
            storedHistory = emptyList()
        }
    }
}
