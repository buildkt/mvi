package com.buildkt.mvi

/**
 * A [SideEffect] wrapper that signals the runtime to apply debounce execution policy:
 * cancel any existing pending run for this effect, wait [delayMs], then run the [wrapped]
 * effect with the **current** state and intent. Only the last run (after intents stop)
 * completes; previous runs are cancelled.
 *
 * The actual execution policy (cancel-previous job, delay, then run with current state)
 * is implemented by the runtime (e.g. [DefaultStateHolder] in mvi-core, or the Android
 * ViewModel layer). This type is platform-agnostic and only defines the contract.
 *
 * **Use case:** Search-as-you-type: user types "f", "fo", "foo" → only one search runs,
 * [delayMs] after the last keystroke, with the latest state (e.g. `state.query == "foo"`).
 *
 * @param S The type of the screen's state.
 * @param I The type of the screen's intents.
 * @param delayMs Delay in milliseconds before running the wrapped effect. If 0, the runtime
 *   may run immediately (no debounce) or still cancel previous and run after 0ms.
 * @param wrapped The underlying side effect to run after the delay.
 */
data class DebouncedSideEffect<S, I>(
    val delayMs: Long,
    val wrapped: SideEffect<S, I>,
) : SideEffect<S, I> {
    override suspend fun invoke(state: S, intent: I): SideEffectResult<I> =
        wrapped(state, intent)
}

/**
 * Wraps a [SideEffect] so that the runtime applies cancel-previous-run debouncing.
 *
 * When an intent that triggers this effect is dispatched, any existing pending run for
 * this effect is cancelled. A new run is started that waits [delayMs], then executes
 * [sideEffect] with the **current** state and intent. Only the last run completes.
 *
 * @param delayMs Delay in milliseconds before running the effect after the last intent.
 * @param sideEffect The side effect to run after the delay.
 * @return A [DebouncedSideEffect] that the runtime will run with debounce semantics.
 */
fun <S, I> debounced(
    delayMs: Long,
    sideEffect: SideEffect<S, I>,
): DebouncedSideEffect<S, I> = DebouncedSideEffect(delayMs = delayMs, wrapped = sideEffect)
