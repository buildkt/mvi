package com.buildkt.mvi

/**
 * A Middleware allows for observing and reacting to events within the MVI loop.
 * It is a powerful mechanism for handling cross-cutting concerns like logging, analytics,
 * or crash reporting without cluttering the core business logic of Reducers and SideEffects.
 *
 * Each function in the middleware corresponds to a specific point in the ViewModel's `onIntent` loop.
 * By extending this abstract class, you only need to override the functions you are interested in.
 *
 * @param S The type of the screen's state.
 * @param I The type of the screen's intents.
 */
abstract class Middleware<S, I> {
    /**
     * Called at the very beginning of the `onIntent` function, before any processing occurs.
     * This is the ideal place for logging all user-initiated actions.
     *
     * @param intent The intent that was just dispatched.
     */
    open suspend fun onIntent(intent: I) {}

    /**
     * Called immediately after the [Reducer] has produced a new state.
     * This is useful for analytics that need to capture the state of the screen
     * after a specific action.
     *
     * @param newState The state produced by the reducer.
     * @param intent The original intent that led to this state change.
     */
    open suspend fun onStateReduced(
        newState: S,
        intent: I,
    ) {}

    /**
     * Called right before a [SideEffect] is executed.
     * This allows for observing which asynchronous operations are about to run.
     *
     * @param sideEffect The side effect that is about to be executed.
     * @param intent The original intent that triggered this side effect.
     */
    open suspend fun onSideEffect(
        sideEffect: SideEffect<S, I>,
        intent: I,
    ) {}

    /**
     * Called immediately after a [SideEffect] has finished its execution.
     * This is useful for logging the results of asynchronous operations,
     * including success, failure, or any new intents produced.
     *
     * @param result The [SideEffectResult] produced by the side effect.
     * @param intent The original intent that triggered the side effect.
     */
    open suspend fun onSideEffectResult(
        result: SideEffectResult<I>,
        intent: I,
    ) {}
}
