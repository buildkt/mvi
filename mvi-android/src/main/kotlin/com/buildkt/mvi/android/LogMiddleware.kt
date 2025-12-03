package com.buildkt.mvi.android

import android.util.Log
import com.buildkt.mvi.Middleware
import com.buildkt.mvi.SideEffect
import com.buildkt.mvi.SideEffectResult

/**
 * A concrete [Middleware] implementation that logs the entire MVI event stream to Android's Logcat,
 * providing a clear, real-time view of the data flow within a screen.
 *
 * It's an invaluable tool for debugging, allowing developers to trace how intents lead to state changes
 * and side effect outcomes.
 *
 * @param tag The Logcat tag to use for all log messages. Defaults to "MVI".
 */
class LogMiddleware<S, I>(
    private val tag: String = "MVI",
) : Middleware<S, I>() {
    override suspend fun onIntent(intent: I) {
        Log.d(tag, "┌ INTENT")
        Log.d(tag, "├─ ${intent?.let { it::class.simpleName }}: $intent")
        Log.d(tag, "└")
    }

    override suspend fun onStateReduced(
        newState: S,
        intent: I,
    ) {
        Log.d(tag, "┌ STATE")
        Log.d(tag, "├─ ${newState?.let { it::class.simpleName }}: $newState")
        Log.d(tag, "└─ (Caused by ${intent?.let { it::class.simpleName }})")
    }

    override suspend fun onSideEffect(
        sideEffect: SideEffect<S, I>,
        intent: I,
    ) {
        Log.d(tag, "┌ SIDE EFFECT (START)")
        Log.d(tag, "├─ ${sideEffect::class.simpleName}")
        Log.d(tag, "└─ (Triggered by ${intent?.let { it::class.simpleName }})")
    }

    override suspend fun onSideEffectResult(
        result: SideEffectResult<I>,
        intent: I,
    ) {
        Log.d(tag, "┌ SIDE EFFECT (RESULT)")
        Log.d(tag, "├─ ${result::class.simpleName}: $result")
        Log.d(tag, "└─ (From ${intent?.let { it::class.simpleName }})")
    }
}
