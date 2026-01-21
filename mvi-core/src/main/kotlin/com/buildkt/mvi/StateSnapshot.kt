package com.buildkt.mvi

/**
 * Represents a snapshot of state at a specific point in time, along with the intent that led to it.
 * This is used for time-travel debugging to track the history of state changes.
 *
 * @param state The state snapshot at this point in time.
 * @param intent The intent that caused this state change, or null if this is the initial state.
 * @param timestamp The timestamp when this state was captured (in milliseconds since epoch).
 * @param index The index of this state in the history (0-based). Defaults to -1 if not set.
 *              Must be >= -1. Values less than -1 are invalid.
 */
data class StateSnapshot<S, I>(
    val state: S,
    val intent: I?,
    val timestamp: Long = System.currentTimeMillis(),
    val index: Int = -1,
) {
    init {
        require(index >= -1) { "Index must be >= -1, but was $index" }
    }
}
