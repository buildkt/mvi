package com.buildkt.mvi

/**
 * A mapping that associates a given [I]ntent with its corresponding [SideEffect].
 *
 * This functional interface acts as a central lookup for all side effects within a single
 * presentation component (e.g., a ViewModel). It decouples the intent-triggering logic from the
 * execution of the side effect itself.
 *
 * The implementation, typically auto-generated, defines the relationship between an `Intent`
 * and the asynchronous operation that needs to be performed. If no side effect is associated
 * with a given intent, this map should return `null`.
 *
 * @param S The type of the screen's state.
 * @param I The type of the screen's intents.
 */
fun interface SideEffectMap<S, I> {
    /**
     * Given an [intent], returns the corresponding [SideEffect] to be executed, or `null` if
     * no side effect is mapped to this intent.
     */
    operator fun get(intent: I): SideEffect<S, I>?
}
