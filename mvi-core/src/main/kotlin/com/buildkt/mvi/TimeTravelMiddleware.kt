package com.buildkt.mvi

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * A middleware that enables time-travel by tracking state history and allowing
 * replay of intents and jumping to specific states.
 *
 * This middleware captures state snapshots after each reduction and maintains a history
 * that can be used to debug the application state at any point in time.
 *
 * **Key Features:**
 * - Tracks complete state history with associated intents
 * - Supports state restoration via [restoreStateAt]
 * - Thread-safe operations using mutex synchronization
 * - Configurable history size limits
 * - Preloaded state support for testing
 *
 * **Thread Safety:**
 * - All history modifications are protected by a mutex to prevent race conditions
 * - State reads are safe from any thread
 * - State writes should be done through the provided methods
 *
 * **Usage Example:**
 * ```
 * // In your navigation setup:
 * middlewares += timeTravelMiddleware {
 *     enable = true
 *     maxHistorySize = 100
 * }
 *
 * // In your debug UI:
 * val middleware = viewModel.getTimeTravelMiddleware()
 * middleware?.restoreStateAt(index) { state ->
 *     viewModel.restoreState(state)
 * }
 * ```
 */
class TimeTravelMiddleware<S, I>(
    private val enable: Boolean,
    private val maxHistorySize: Int,
    private val preloadedStates: List<StateSnapshot<S, I>>,
    private val onReplayError: ((Throwable) -> Unit)? = null,
    private val onHistorySizeWarning: ((currentSize: Int, maxSize: Int) -> Unit)? = null,
) : Middleware<S, I>(),
    InitializableMiddleware<S, I> {
    /**
     * Threshold (number of entries) at which [onHistorySizeWarning] is invoked.
     * Set to 80% of [maxHistorySize] to warn before truncation.
     */
    private val historySizeWarningThreshold = (maxHistorySize * 0.8).toInt().coerceAtLeast(0)

    private val historyMutex = Mutex()

    private val _history =
        MutableStateFlow<List<StateSnapshot<S, I>>>(
            value = preloadedStates.mapIndexed { index, history -> history.copy(index = index) },
        )
    val history: StateFlow<List<StateSnapshot<S, I>>> = _history.asStateFlow()

    /**
     * The current index in the history.
     * - `-1` means no state has been initialized yet
     * - `>= 0` means the index of the current state in history
     */
    private val _currentHistoryIndex = MutableStateFlow(-1)
    val currentHistoryIndex: StateFlow<Int> = _currentHistoryIndex.asStateFlow()

    private val _isReplaying = MutableStateFlow(false)
    val isReplaying: StateFlow<Boolean> = _isReplaying.asStateFlow()

    init {
        // If preloaded states exist, set current index to the last entry
        // Otherwise, keep it at -1 until addInitialState() is called
        if (preloadedStates.isNotEmpty()) {
            _currentHistoryIndex.value = preloadedStates.size - 1
        }
    }

    /**
     * Adds the initial state to the history. This should be called by the ViewModel
     * during initialization.
     *
     * If preloaded states exist but don't include an initial state (intent == null),
     * the provided initial state will be prepended to the history.
     *
     * **Thread Safety:** This method is thread-safe and uses mutex synchronization
     * to prevent race conditions with concurrent state reductions.
     */
    override suspend fun initialize(state: S) {
        if (!enable) return

        historyMutex.withLock {
            val currentHistory = _history.value
            if (currentHistory.isEmpty()) {
                // No history exists, add initial state
                val initialState =
                    StateSnapshot<S, I>(
                        state = state,
                        intent = null,
                        index = 0,
                    )
                _history.value = listOf(initialState)
                _currentHistoryIndex.value = 0
            } else {
                // History exists (from preloaded states), check if initial state is missing
                val hasInitialState = currentHistory.any { it.intent == null }
                if (!hasInitialState) {
                    // Prepend initial state and re-index
                    val reindexedHistory =
                        currentHistory.mapIndexed { index, entry ->
                            entry.copy(index = index + 1)
                        }
                    val initialState =
                        StateSnapshot<S, I>(
                            state = state,
                            intent = null,
                            index = 0,
                        )
                    _history.value = listOf(initialState) + reindexedHistory
                    // Don't change currentHistoryIndex as it should point to the last entry
                }
            }
        }
    }

    /**
     * Thread-safe method to add a new state to the history.
     * Uses mutex to prevent race conditions when multiple state reductions occur concurrently.
     *
     * **Performance Note**: This method optimizes for the common case (no truncation needed)
     * by avoiding unnecessary list copies when possible.
     */
    override suspend fun onStateReduced(
        newState: S,
        intent: I,
    ) {
        if (!enable || _isReplaying.value) return

        historyMutex.withLock {
            val currentHistory = _history.value
            val newIndex = currentHistory.size
            val newHistoryEntry =
                StateSnapshot(
                    state = newState,
                    intent = intent,
                    index = newIndex,
                )

            // Optimize for common case: no truncation needed
            if (currentHistory.size + 1 <= maxHistorySize) {
                // Direct immutable update - no copy needed
                _history.value = currentHistory + newHistoryEntry
                _currentHistoryIndex.value = newIndex
                // Warn when history size approaches limit
                if (currentHistory.size + 1 >= historySizeWarningThreshold) {
                    onHistorySizeWarning?.invoke(currentHistory.size + 1, maxHistorySize)
                }
            } else {
                // Truncation needed - create mutable copy
                val mutableHistory = currentHistory.toMutableList()
                mutableHistory.add(newHistoryEntry)

                val removedCount = mutableHistory.size - maxHistorySize
                // Remove oldest entries (but preserve initial state if it exists)
                val initialState = mutableHistory.firstOrNull()?.takeIf { it.intent == null }
                val toRemove =
                    if (initialState != null && removedCount > 0) {
                        // Remove entries after initial state with bounds checking
                        // removedCount + 1 because we skip the initial state at index 0
                        val endIndex = minOf(removedCount + 1, mutableHistory.size)
                        mutableHistory.subList(1, endIndex)
                    } else {
                        // Remove from start with bounds checking
                        val endIndex = minOf(removedCount, mutableHistory.size)
                        mutableHistory.subList(0, endIndex)
                    }
                toRemove.clear()

                // Re-index remaining entries
                mutableHistory.forEachIndexed { index, entry ->
                    mutableHistory[index] = entry.copy(index = index)
                }

                // Adjust currentHistoryIndex - point to the last entry (the new one we just added)
                val finalIndex = mutableHistory.lastIndex
                _history.value = mutableHistory
                _currentHistoryIndex.value = finalIndex
                // Warn when at limit (truncation just occurred)
                onHistorySizeWarning?.invoke(mutableHistory.size, maxHistorySize)
            }
        }
    }

    /**
     * Restores the state at the given index. This method should be called with
     * a state restoration function that can actually apply the state to the StateHolder.
     *
     * @param index The index of the state to restore.
     * @param restoreFunction A function that applies the state to the StateHolder.
     * @return true if the state was successfully restored, false otherwise.
     */
    fun restoreStateAt(
        index: Int,
        restoreFunction: (S) -> Unit,
    ): Boolean {
        if (!enable) return false

        val state = getStateAt(index) ?: return false
        startReplay()
        try {
            restoreFunction(state)
            _currentHistoryIndex.value = index
            return true
        } catch (e: ClassCastException) {
            onReplayError?.invoke(e)
            return false
        } finally {
            endReplay()
        }
    }

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
        history: List<StateSnapshot<S, I>>,
        setCurrentIndexToLast: Boolean = false,
    ): Boolean {
        if (!enable) return false

        if (history.isEmpty()) {
            clearHistory()
            return true
        }

        return historyMutex.withLock {
            // Re-index the history to ensure indices are correct
            val reindexedHistory =
                history.mapIndexed { index, entry ->
                    entry.copy(index = index)
                }

            // Validate the re-indexed history
            val isValid =
                reindexedHistory
                    .mapIndexed { index, entry ->
                        entry.index == index
                    }.all { it }

            if (!isValid) {
                // History is corrupted, clear it
                clearHistory()
                return@withLock false
            }

            _history.value = reindexedHistory
            _currentHistoryIndex.value =
                if (setCurrentIndexToLast) {
                    reindexedHistory.lastIndex
                } else {
                    val currentIndex = _currentHistoryIndex.value
                    if (currentIndex >= 0 && currentIndex < reindexedHistory.size) {
                        currentIndex
                    } else {
                        // If no valid current index, default to 0 (initial state) if it exists,
                        // otherwise use the last index
                        if (reindexedHistory.isNotEmpty()) {
                            val initialIndex = reindexedHistory.indexOfFirst { it.intent == null }
                            if (initialIndex >= 0) initialIndex else reindexedHistory.lastIndex
                        } else {
                            -1
                        }
                    }
                }
            true
        }
    }

    /**
     * Gets the state at a specific index in the history.
     * This is a read-only operation - use [restoreStateAt] to actually restore the state.
     *
     * @param index The index of the state to retrieve.
     * @return The state at the given index, or null if the index is invalid.
     */
    private fun getStateAt(index: Int): S? {
        if (!enable) return null

        val historyList = _history.value
        if (index < 0 || index >= historyList.size) {
            return null
        }

        return historyList[index].state
    }

    /**
     * Gets all intents between two indices.
     *
     * This method normalizes the indices (handles both forward and backward traversal)
     * and returns intents from `min(startIndex, endIndex) + 1` to `max(startIndex, endIndex)` (inclusive).
     *
     * **Note**: Initial states (where intent == null) are automatically filtered out
     * from the result, as they don't represent actionable intents.
     *
     * **Examples:**
     * - `getIntentsBetween(0, 3)` returns intents at indices [1, 2, 3]
     * - `getIntentsBetween(3, 0)` returns intents at indices [1, 2, 3] (normalized)
     * - `getIntentsBetween(2, 2)` returns empty list (no intents between same index)
     *
     * This is useful for replaying intents to reach a specific state.
     *
     * @param startIndex The starting index (exclusive - intents after this index are included).
     * @param endIndex The ending index (inclusive - intent at this index is included).
     * @return List of intents between the indices, or empty list if indices are invalid.
     *         Returns empty list if startIndex == endIndex (no intents to replay).
     *         Initial states (intent == null) are filtered out.
     */
    private fun getIntentsBetween(
        startIndex: Int,
        endIndex: Int,
    ): List<I> {
        if (!enable) return emptyList()

        val historyList = _history.value
        if (startIndex < 0 || endIndex < 0 || startIndex >= historyList.size || endIndex >= historyList.size) {
            return emptyList()
        }

        val actualStart = minOf(startIndex, endIndex)
        val actualEnd = maxOf(startIndex, endIndex)

        // If indices are the same, no intents to return
        if (actualStart == actualEnd) {
            return emptyList()
        }

        // Return intents from (actualStart + 1) to actualEnd (inclusive)
        // Note: mapNotNull filters out null intents (initial states)
        return historyList
            .subList(actualStart + 1, actualEnd + 1)
            .mapNotNull { it.intent }
    }

    /**
     * Gets all intents from the current history index to a target index.
     * This is useful for replaying intents to reach a specific state.
     *
     * @param targetIndex The target index to replay to.
     * @return List of intents from current to target, or empty list if indices are invalid.
     */
    private fun getIntentsFromCurrentTo(targetIndex: Int): List<I> {
        if (!enable) return emptyList()

        val currentIndex = _currentHistoryIndex.value
        return getIntentsBetween(currentIndex, targetIndex)
    }

    /**
     * Checks if it's possible to jump to the given index.
     * @return true if the index is valid and can be jumped to.
     */
    private fun canJumpTo(index: Int): Boolean {
        if (!enable) return false
        val historyList = _history.value
        return index >= 0 && index < historyList.size
    }

    /**
     * Marks that a replay operation is starting. This prevents the middleware
     * from recording state changes during replay.
     */
    private fun startReplay() {
        _isReplaying.value = true
    }

    /**
     * Marks that a replay operation has finished.
     */
    private fun endReplay() {
        _isReplaying.value = false
    }

    /**
     * Clears the history, keeping only the initial state if it exists.
     */
    private fun clearHistory() {
        if (!enable) return

        val initial = _history.value.firstOrNull()?.takeIf { it.intent == null }
        if (initial != null) {
            _history.value = listOf(initial.copy(index = 0))
            _currentHistoryIndex.value = 0
        } else {
            _history.value = emptyList()
            _currentHistoryIndex.value = -1
        }
    }
}
