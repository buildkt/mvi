package com.buildkt.mvi.android

import android.util.Log
import com.buildkt.mvi.Middleware
import com.buildkt.mvi.SideEffect
import com.buildkt.mvi.SideEffectResult

/**
 * Creates a [LogMiddleware] with the provided configuration.
 *
 * Example usage:
 * ```
 * middlewares += logMiddleware {
 *     tag = "MyScreen"
 *     logLevel = Log.VERBOSE
 *     logSideEffectResult = false
 * }
 * ```
 *
 * @param block Configuration block for the log middleware.
 * @return A configured [LogMiddleware] instance.
 */
fun <S, I> logMiddleware(block: LogMiddlewareConfig.() -> Unit = {}): LogMiddleware<S, I> {
    val config = LogMiddlewareConfig().apply(block)
    return LogMiddleware(config)
}

/**
 * A concrete [Middleware] implementation that logs the entire MVI event stream to Android's Logcat,
 * providing a clear, real-time view of the data flow within a screen.
 *
 * It's an invaluable tool for debugging, allowing developers to trace how intents lead to state changes
 * and side effect outcomes.
 *
 * **Usage Example:**
 * ```
 * // Simple usage with defaults
 * middlewares += LogMiddleware()
 *
 * // With custom tag
 * middlewares += LogMiddleware(tag = "MyScreen")
 *
 * // Using factory function with configuration
 * middlewares += logMiddleware {
 *     tag = "MyScreen"
 *     logLevel = Log.VERBOSE
 *     logSideEffectResult = false
 * }
 * ```
 *
 * @param config The configuration for the log middleware.
 */
class LogMiddleware<S, I> internal constructor(
    private val config: LogMiddlewareConfig = LogMiddlewareConfig(),
) : Middleware<S, I>() {
    override suspend fun onIntent(intent: I) {
        if (!config.logIntent || intent == null) return

        log(config.tag, "┌ INTENT")
        log(config.tag, "├─ ${intent::class.simpleName}: $intent")
        log(config.tag, "└")
    }

    override suspend fun onStateReduced(
        newState: S,
        intent: I,
    ) {
        if (!config.logState || newState == null || intent == null) return

        log(config.tag, "┌ STATE")
        log(config.tag, "├─ ${newState::class.simpleName}: $newState")
        log(config.tag, "└ (Caused by ${intent::class.simpleName})")
    }

    override suspend fun onSideEffect(
        sideEffect: SideEffect<S, I>,
        intent: I,
    ) {
        if (!config.logSideEffectStart || intent == null) return

        log(config.tag, "┌ SIDE EFFECT (START)")
        log(config.tag, "├─ ${sideEffect::class.simpleName}")
        log(config.tag, "└ (Triggered by ${intent::class.simpleName})")
    }

    override suspend fun onSideEffectResult(
        result: SideEffectResult<I>,
        intent: I,
    ) {
        if (!config.logSideEffectResult || intent == null) return

        log(config.tag, "┌ SIDE EFFECT (RESULT)")
        log(config.tag, "├─ ${result::class.simpleName}: $result")
        log(config.tag, "└ (From ${intent::class.simpleName})")
    }

    private fun log(
        tag: String,
        message: String,
    ) {
        when (config.logLevel) {
            Log.VERBOSE -> Log.v(tag, message)
            Log.DEBUG -> Log.d(tag, message)
            Log.INFO -> Log.i(tag, message)
            Log.WARN -> Log.w(tag, message)
            Log.ERROR -> Log.e(tag, message)
            else -> Log.d(tag, message)
        }
    }
}

/**
 * Configuration for log middleware.
 *
 * @param tag The Logcat tag to use for all log messages. Defaults to "MVI".
 * @param logLevel The Android log level to use. Defaults to [Log.DEBUG].
 * @param logIntent Whether to log intents. Defaults to true.
 * @param logState Whether to log state changes. Defaults to true.
 * @param logSideEffectStart Whether to log side effect starts. Defaults to true.
 * @param logSideEffectResult Whether to log side effect results. Defaults to true.
 */
data class LogMiddlewareConfig(
    var tag: String = "MVI",
    var logLevel: Int = Log.DEBUG,
    var logIntent: Boolean = true,
    var logState: Boolean = true,
    var logSideEffectStart: Boolean = true,
    var logSideEffectResult: Boolean = true,
)
