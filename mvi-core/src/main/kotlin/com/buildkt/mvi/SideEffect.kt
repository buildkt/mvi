package com.buildkt.mvi

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.merge

/**
 * Defines a suspendable unit of work that is executed outside of the [Reducer].
 * A SideEffect is typically used for asynchronous operations like API calls, database access,
 * or complex calculations that should not block the main thread or the state reduction process.
 *
 * It takes the current state `S` and the triggering intent `I` as context and produces a
 * [SideEffectResult], which is then processed by the ViewModel.
 *
 * @param S The type of the screen's state.
 * @param I The type of the screen's intents.
 */
fun interface SideEffect<S, I> {
    /**
     * Executes the side effect's logic.
     *
     * @param state The current screen state at the time of execution.
     * @param intent The intent that triggered this side effect.
     * @return A [SideEffectResult] indicating the outcome of the operation.
     */
    suspend operator fun invoke(
        state: S,
        intent: I,
    ): SideEffectResult<I>
}

/**
 * Represents the possible outcomes of executing a [SideEffect].
 * This is an abstract class rather than a sealed interface to allow for operator functions.
 *
 * A side effect can result in a new single [I]ntent, a [Flow] of new intents, a navigation
 * action, or nothing at all. Results can be combined using the `plus` operator.
 *
 * @param I The type of the intents produced by the side effect.
 */
sealed class SideEffectResult<out I> {
    /**
     * Combines this result with another, merging their intents.
     * `Navigation` and `ShowUiEvent` from the right-hand side are ignored if the left-hand side
     * already contains intents.
     */
    abstract operator fun plus(other: SideEffectResult<@UnsafeVariance I>): SideEffectResult<I>

    /** Indicates that the side effect completed without producing any further action. */
    data object NoOp : SideEffectResult<Nothing>() {
        override fun plus(other: SideEffectResult<Nothing>): SideEffectResult<Nothing> {
            // NoOp + anything = anything
            return other
        }
    }

    /** Indicates that the side effect produced a single new intent to be processed. */
    data class NewIntent<I>(
        val intent: I,
    ) : SideEffectResult<I>() {
        override fun plus(other: SideEffectResult<I>): SideEffectResult<I> {
            val thisAsFlow = flowOf(this.intent)
            return when (other) {
                is NewIntent -> NewIntents(merge(thisAsFlow, flowOf(other.intent)))
                is NewIntents -> NewIntents(merge(thisAsFlow, other.intents))
                // For NoOp, Navigation, etc., just promote this to NewIntents
                else -> NewIntents(thisAsFlow)
            }
        }
    }

    /** Indicates that the side effect produced a stream of new intents to be processed over time. */
    data class NewIntents<I>(
        val intents: Flow<I>,
    ) : SideEffectResult<I>() {
        override fun plus(other: SideEffectResult<I>): SideEffectResult<I> =
            when (other) {
                is NewIntent -> NewIntents(merge(this.intents, flowOf(other.intent)))
                is NewIntents -> NewIntents(merge(this.intents, other.intents))
                // Ignore NoOp, Navigation, etc. from the right-hand side
                else -> this
            }
    }

    /**
     * Indicates that the side effect triggered a navigation event.
     * The [event] is a generic object to be interpreted by a platform-specific navigation handler,
     * ensuring this core library remains platform-agnostic.
     */
    data class Navigation(
        val event: Any,
    ) : SideEffectResult<Nothing>() {
        override fun plus(other: SideEffectResult<Nothing>): SideEffectResult<Nothing> {
            // Navigation combined with anything that has intents results in the intents taking precedence.
            return when (other) {
                is NewIntent, is NewIntents -> other
                else -> this // Otherwise, keep this navigation event
            }
        }
    }

    /**
     * Indicates that a transient UI event (like a Toast or Snackbar) should be shown.
     * The [event] is a generic object to be interpreted by a platform-specific UI event handler.
     */
    data class ShowUiEvent(
        val event: Any,
    ) : SideEffectResult<Nothing>() {
        override fun plus(other: SideEffectResult<Nothing>): SideEffectResult<Nothing> {
            // UI events follow the same precedence logic as Navigation events.
            return when (other) {
                is NewIntent, is NewIntents -> other
                else -> this
            }
        }
    }
}

/**
 * A [SideEffect] that executes multiple child side effects in parallel.
 *
 * It launches all provided side effects concurrently, awaits their completion, and merges
 * their results into a single [SideEffectResult] using the `plus` operator.
 * This is ideal for when a single user action needs to trigger multiple independent data loads.
 *
 * @param S The type of the screen's state.
 * @param I The type of the screen's intents.
 */
class ParallelSideEffect<S, I : Any> internal constructor(
    private vararg val sideEffects: SideEffect<S, I>?,
) : SideEffect<S, I> {
    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun invoke(
        state: S,
        intent: I,
    ): SideEffectResult<I> =
        coroutineScope {
            // 1. Launch all non-null side effects in parallel and await their results.
            val results =
                sideEffects
                    .filterNotNull()
                    .map { async { it(state, intent) } }
                    .map { it.await() }

            // 2. Combine all results. Start with a NoOp, which correctly handles empty lists
            //    and delegates to the first meaningful result.
            val combinedResult =
                results.fold<SideEffectResult<I>, SideEffectResult<I>>(
                    SideEffectResult.NoOp,
                ) { acc, next -> acc + next }

            // 3. Ensure a consistent return type for downstream consumers.
            //    If the final result is just a single intent, wrap it in NewIntents.
            //    If it's NoOp or Navigation, return an empty NewIntents.
            return@coroutineScope when (combinedResult) {
                is SideEffectResult.NewIntents -> combinedResult
                is SideEffectResult.NewIntent -> SideEffectResult.NewIntents(flowOf(combinedResult.intent))
                else -> SideEffectResult.NewIntents(emptyFlow())
            }
        }
}

/**
 * Creates a [SideEffect] that performs a one-shot async operation and may return a single new [I]ntent.
 * This is the most common type of side effect for simple request-response patterns.
 *
 * @param block A suspendable lambda that receives the current state and the triggering intent.
 *              If it returns a non-null [Intent], it will be dispatched as a [SideEffectResult.NewIntent].
 *              If it returns `null`, it becomes a [SideEffectResult.NoOp].
 */
fun <S, I> sideEffect(block: suspend (state: S, intent: I) -> I?): SideEffect<S, I> =
    SideEffect { state, intent ->
        block(state, intent)?.let { SideEffectResult.NewIntent(intent = it) } ?: SideEffectResult.NoOp
    }

/**
 * A default [SideEffect] that does nothing and produces a [SideEffectResult.NoOp].
 * Useful for defaults or for intents that require no asynchronous work.
 */
fun <S, I> noOpSideEffect(): SideEffect<S, I> = SideEffect { _, _ -> SideEffectResult.NoOp }

/**
 * Creates a [SideEffect] that executes multiple other side effects in parallel using [ParallelSideEffect].
 *
 * @param sideEffects A vararg of `SideEffect` instances to be executed concurrently.
 * @return A [ParallelSideEffect] instance.
 */
fun <S, I : Any> parallelSideEffect(vararg sideEffects: SideEffect<S, I>?): SideEffect<S, I> = ParallelSideEffect(*sideEffects)

/**
 * Creates a [SideEffect] that starts an observable stream of work and can emit multiple new intents
 * over time. This is ideal for subscribing to data sources that change, like a WebSocket,
 * a database query, or sensor data.
 *
 * @param block A lambda that receives the current state and returns a [Flow] of intents.
 * @return A [SideEffect] that wraps the provided flow.
 */
fun <S, I> observableSideEffect(block: (state: S) -> Flow<I>): SideEffect<S, I> =
    SideEffect { state, _ ->
        SideEffectResult.NewIntents(intents = block(state))
    }
