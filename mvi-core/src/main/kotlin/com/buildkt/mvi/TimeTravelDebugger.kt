package com.buildkt.mvi

import kotlinx.coroutines.flow.StateFlow

/**
 * Class that provides access to time-travel debugging functionality.
 * This provides access to time-travel debugging operations without exposing the full ViewModel.
 *
 * **Warning**: This class should only be used in debug builds and for debugging purposes.
 *
 * **Thread Safety:**
 * - All methods are safe to call from any thread
 * - State reads are thread-safe
 * - State modifications are synchronized internally
 */
class TimeTravelDebugger<State, Intent : Any, NavEvent, UiEvent>(
    private val stateHolder: DebuggableStateHolder<State, Intent, NavEvent, UiEvent>,
    private val timeTravelMiddleware: TimeTravelMiddleware<State, Intent>,
) {
    /**
     * Restores the state to a specific value from the time-travel debugging history.
     * This bypasses the normal MVI flow and directly sets the state.
     *
     * **Warning**: This method should only be used in debug builds and for debugging purposes.
     *
     * @param state The state to restore.
     */
    fun restoreState(state: State) {
        stateHolder.restoreState(state)
    }

    /**
     * Restores the state at a specific index from the time-travel debugging history.
     *
     * @param index The index of the state to restore.
     * @return true if the state was successfully restored, false otherwise.
     */
    fun restoreStateFromHistory(index: Int): Boolean = timeTravelMiddleware.restoreStateAt(index) { state -> restoreState(state) }

    /**
     * Loads a history list into the middleware. This replaces the current history.
     * Useful for restoring history from persistence.
     *
     * **Validation:**
     * - Validates that all entries have consistent indices after re-indexing
     * - Ensures the history is not corrupted
     * - If validation fails, clears the history and returns false
     *
     * @param history The history to load. Should be a valid list of state snapshots.
     * @param setCurrentIndexToLast If true, sets the current index to the last entry.
     *                              If false, keeps the current index (if valid) or sets to 0.
     * @return `true` if the history was successfully loaded, `false` if validation failed.
     */
    suspend fun loadHistory(
        history: List<StateSnapshot<State, Intent>>,
        setCurrentIndexToLast: Boolean = false,
    ): Boolean = timeTravelMiddleware.loadHistory(history, setCurrentIndexToLast)

    fun getHistoryStateFlow(): StateFlow<List<StateSnapshot<State, Intent>>> = timeTravelMiddleware.history

    fun getCurrentIndexStateFlow(): StateFlow<Int> = timeTravelMiddleware.currentHistoryIndex
}

/**
 * A high-level feature configuration for time-travel debugging.
 * This class provides a DSL-friendly API for configuring time-travel debugging
 * and automatically wires up the middleware and persistence.
 *
 * **Usage Example:**
 * ```
 * timeTravel {
 *     enable = true
 *     maxHistorySize = 100
 *     persistence = InMemoryTimeTravelPersistence()
 *     enableTimeTravelOverlayUi = true
 * }
 * ```
 *
 * @param S The type of the state.
 * @param I The type of the intent.
 */
class TimeTravelDebuggerConfig<S, I> {
    /**
     * Whether time-travel debugging is enabled. Defaults to false.
     */
    var enable: Boolean = false

    /**
     * Whether to enable the time-travel overlay UI. Defaults to true.
     * Note: If enable is false this option is ignored.
     */
    var enableTimeTravelOverlayUi: Boolean = true

    /**
     * Preloaded state snapshots to inject into the history. Defaults to empty list.
     */
    var preloadedStates: List<StateSnapshot<S, I>> = emptyList()

    /**
     * Maximum number of state snapshots to keep in history. Defaults to 100.
     */
    var maxHistorySize: Int = 100

    /**
     * Storage implementation for persisting and loading state history.
     * Defaults to [InMemoryStateHistoryStorage] which stores history in memory.
     *
     * **Note**: The history is automatically saved/loaded by the time-travel overlay UI
     * if enabled. Custom implementations should handle serialization of state and intent types.
     */
    var stateHistoryStorage: StateHistoryStorage<S, I> = InMemoryStateHistoryStorage()

    /**
     * Optional callback invoked when replay fails (e.g. host can log the error).
     */
    var onReplayError: ((Throwable) -> Unit)? = null

    /**
     * Optional callback invoked when history size approaches [maxHistorySize] (at 80% and when at limit).
     * Use for monitoring and logging memory pressure.
     */
    var onHistorySizeWarning: ((currentSize: Int, maxSize: Int) -> Unit)? = null

    /**
     * Creates a [TimeTravelMiddleware] instance based on this feature's configuration.
     * The middleware will be configured with the persistence if provided.
     *
     * @return A configured [TimeTravelMiddleware] instance.
     */
    fun createMiddleware(): TimeTravelMiddleware<S, I> =
        TimeTravelMiddleware(
            enable = enable,
            maxHistorySize = maxHistorySize,
            preloadedStates = preloadedStates,
            onReplayError = onReplayError,
            onHistorySizeWarning = onHistorySizeWarning,
        )
}
