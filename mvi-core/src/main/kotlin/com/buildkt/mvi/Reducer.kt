package com.buildkt.mvi

/**
 * The Reducer is the brain of the MVI pattern, responsible for processing incoming [I]ntents
 * and evolving the current screen [S]tate.
 *
 * A Reducer is a pure function that takes the current state `S` and an incoming `I`ntent,
 * and returns a new state `S`. It must be free of side effects (e.g., no API calls,
 * database access, or logging) to ensure predictable state transitions.
 *
 * As a functional interface, it can be implemented with a simple lambda.
 *
 * @param S The type of the screen's state.
 * @param I The type of the screen's intents.
 */
fun interface Reducer<S, I> {
    /**
     * A pure function that defines how the screen state evolves in response to an intent.
     *
     * This function MUST NOT perform any asynchronous operations, I/O, or other side effects.
     *
     * @param state The current state to be reduced.
     * @param intent The intent to process.
     * @return The new, updated state [S].
     */
    fun reduce(
        state: S,
        intent: I,
    ): S
}
