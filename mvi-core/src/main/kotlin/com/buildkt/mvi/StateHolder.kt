package com.buildkt.mvi

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

/**
 * Defines the public contract for a platform-agnostic MVI state container.
 *
 * A `StateHolder` is responsible for managing UI state, processing intents, and emitting one-shot
 * events for navigation or transient UI updates. It serves as the single source of truth for a
 * given screen or feature, abstracting the underlying MVI implementation details from the UI layer.
 *
 * This interface is designed to be implemented by platform-specific holders (like an Android
 * `ViewModel`) or pure Kotlin containers, providing a consistent API across different environments.
 *
 * @param State The type representing the UI state.
 * @param Intent The type representing user actions or events from the UI.
 * @param NavEvent The type representing one-shot navigation commands.
 * @param UiEvent The type representing transient, one-shot UI events (e.g., showing a Snackbar).
 */
interface StateHolder<State, Intent, NavEvent, UiEvent> {
    /**
     * A hot [kotlinx.coroutines.flow.Flow] that emits one-shot navigation events.
     * It is designed to be collected by a UI coordinator responsible for navigation to ensure
     * that events are consumed exactly once and not re-played on configuration changes.
     */
    val navigationEvents: SharedFlow<NavEvent>

    /**
     * A hot [kotlinx.coroutines.flow.Flow] that emits transient, one-shot UI events (e.g., showing a Toast or a Dialog).
     * Like `navigationEvents`, it ensures single-consumption to prevent duplicate UI feedback.
     */
    val uiEvents: SharedFlow<UiEvent>

    /**
     * The single source of truth for the UI state, represented as a hot [StateFlow].
     * The UI layer should observe this flow to reactively update itself based on the latest state.
     */
    val uiState: StateFlow<State>

    /**
     * The entry point for dispatching an [Intent] from the UI to the state holder.
     * All user actions and UI-triggered events should be sent through this function to be
     * processed by the MVI loop.
     *
     * @param intent The intent to be processed.
     */
    fun onIntent(intent: Intent)
}

/**
 * Interface that extends [StateHolder] with debug-only state restoration capabilities.
 * This interface is should only be used by debug infrastructure such as time-travel debugging.
 *
 * **Warning**: This interface and its methods should only be used in debug builds and for debugging purposes.
 * Direct state mutation bypasses the normal MVI flow (reducer and side effects) and can lead to
 * inconsistent application state.
 *
 * @param State The type representing the UI state.
 * @param Intent The type representing user actions or events from the UI.
 * @param NavEvent The type representing one-shot navigation commands.
 * @param UiEvent The type representing transient, one-shot UI events.
 */
interface DebuggableStateHolder<State, Intent, NavEvent, UiEvent> : StateHolder<State, Intent, NavEvent, UiEvent> {
    /**
     * Restores the state to a specific value. This is intended for debugging purposes only,
     * such as time-travel debugging. It bypasses the normal MVI flow (reducer and side effects).
     *
     * **Warning**: This method should only be used in debug builds and for debugging purposes.
     * Restoring state directly can lead to inconsistent application state if side effects
     * or other state-dependent logic is not properly handled.
     *
     * **Thread Safety:**
     * - This method is thread-safe and can be called from any thread
     * - State updates are atomic via MutableStateFlow
     * - However, calling this while [onIntent] is processing may cause race conditions
     *   in the MVI flow. It's recommended to call this when the MVI loop is idle.
     *
     * @param state The state to restore.
     */
    fun restoreState(state: State)
}

/**
 * The default, platform-agnostic implementation of the [StateHolder] interface.
 *
 * It orchestrates the entire MVI loop:
 * 1. Receives an `Intent` via [onIntent].
 * 2. Passes the intent and current state to the [Reducer] to produce a new `State`.
 * 3. Updates the [uiState] to reflect the new state.
 * 4. Triggers any associated [SideEffect] for the given intent.
 * 5. Processes the [SideEffectResult] to dispatch further intents or one-shot events.
 *
 * This class is pure Kotlin and relies on an external [CoroutineScope] for lifecycle management,
 * making it fully portable and testable outside the Android framework.
 *
 * @param State The type of the screen's state.
 * @param Intent The type of the screen's intents.
 * @param initialState The initial state of the screen.
 * @param reducer The [Reducer] responsible for state evolution.
 * @param sideEffects The [SideEffectMap] that maps intents to their side effects.
 * @param coroutineScope The [CoroutineScope] in which all asynchronous operations are launched.
 * This scope should be managed by the host environment (e.g., an Android `ViewModel`'s `viewModelScope`).
 * @param middlewares A list of [Middleware]s for observing and intercepting the MVI loop,
 * primarily used for logging or debugging.
 */
class DefaultStateHolder<State, Intent : Any, NavEvent, UiEvent>(
    initialState: State,
    private val reducer: Reducer<State, Intent>,
    private val sideEffects: SideEffectMap<State, Intent>,
    private val middlewares: List<Middleware<State, Intent>> = emptyList(),
    private val coroutineScope: CoroutineScope,
) : DebuggableStateHolder<State, Intent, NavEvent, UiEvent> {
    /**
     * A hot flow for emitting one-shot navigation events.
     *
     * Using a [MutableSharedFlow] ensures that navigation events are delivered once and are not
     * re-emitted on configuration changes (e.g., screen rotation), preventing duplicate
     * navigation actions. It is designed to be collected by a UI-level coordinator.
     */
    private val _navigationEvents =
        MutableSharedFlow<NavEvent>(
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
        )
    override val navigationEvents: SharedFlow<NavEvent> = _navigationEvents

    /**
     * A hot flow for emitting one-shot UI events like Snackbars or Toasts.
     * This follows the same pattern as `navigationEvents` to ensure events are consumed once.
     */
    private val _uiEvents =
        MutableSharedFlow<UiEvent>(
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
        )
    override val uiEvents: SharedFlow<UiEvent> = _uiEvents

    /**
     * The single source of truth for the screen's UI state.
     *
     * Using a [MutableStateFlow], it allows the UI to observe state changes reactively
     * and always have access to the latest state.
     */
    private val _uiState = MutableStateFlow(value = initialState)
    override val uiState: StateFlow<State> = _uiState

    /**
     * Pending jobs for debounced side effects, keyed by the [DebouncedSideEffect] instance.
     * When a new intent triggers the same debounced effect, the previous job is cancelled
     * and replaced. When a job completes (or is cancelled), it is removed from this map.
     */
    private val debouncedJobs = ConcurrentHashMap<Any, Job>()

    init {
        coroutineScope.launch {
            middlewares
                .filterIsInstance<InitializableMiddleware<State, Intent>>()
                .forEach { it.initialize(initialState) }
        }
    }

    override fun restoreState(state: State) {
        _uiState.value = state
    }

    override fun onIntent(intent: Intent) {
        coroutineScope.launch {
            middlewares.forEach { it.onIntent(intent) }
            val stateAtTimeOfReduction = uiState.value

            val newUiState = reducer.reduce(stateAtTimeOfReduction, intent)
            _uiState.value = newUiState
            middlewares.forEach { it.onStateReduced(newUiState, intent) }

            sideEffects[intent]?.let { sideEffect ->
                middlewares.forEach { it.onSideEffect(sideEffect, intent) }

                when (val debounced = sideEffect as? DebouncedSideEffect<State, Intent>) {
                    null -> runSideEffectImmediately(sideEffect, stateAtTimeOfReduction, intent)
                    else -> runDebouncedSideEffect(debounced, intent)
                }
            }
        }
    }

    private suspend fun runSideEffectImmediately(
        sideEffect: SideEffect<State, Intent>,
        state: State,
        intent: Intent,
    ) {
        val result = sideEffect(state, intent)
        middlewares.forEach { it.onSideEffectResult(result, intent) }
        processSideEffectResult(result)
    }

    private fun runDebouncedSideEffect(
        debounced: DebouncedSideEffect<State, Intent>,
        intent: Intent,
    ) {
        debouncedJobs[debounced]?.cancel()
        val job = coroutineScope.launch {
            delay(debounced.delayMs)
            val currentState = _uiState.value
            val result = debounced.wrapped(currentState, intent)
            middlewares.forEach { it.onSideEffectResult(result, intent) }
            processSideEffectResult(result)
        }
        job.invokeOnCompletion { debouncedJobs.remove(debounced, job) }
        debouncedJobs[debounced] = job
    }

    @Suppress("UNCHECKED_CAST")
    private suspend fun processSideEffectResult(result: SideEffectResult<Intent>) {
        when (result) {
            is SideEffectResult.NewIntent -> onIntent(intent = result.intent)
            is SideEffectResult.NewIntents -> result.intents.collect(::onIntent)
            is SideEffectResult.ShowUiEvent -> {
                (result.event as? UiEvent)?.let { _uiEvents.emit(value = it) }
            }
            is SideEffectResult.Navigation -> {
                (result.event as? NavEvent)?.let { _navigationEvents.emit(value = it) }
            }
            is SideEffectResult.NoOp -> { /* Do nothing */ }
        }
    }
}
