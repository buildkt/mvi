package com.buildkt.mvi.android

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buildkt.mvi.Middleware
import com.buildkt.mvi.Reducer
import com.buildkt.mvi.SideEffectMap
import com.buildkt.mvi.SideEffectResult
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * An Android-specific `ViewModel` that acts as the engine for the MVI architecture.
 *
 * It hosts the state, processes intents, and manages the lifecycle of side effects, connecting
 * the pure MVI core (`Reducer`, `SideEffectMap`) to the Android ecosystem (`viewModelScope`).
 *
 * This class is designed to be automatically generated and instantiated by an annotation processor,
 * not manually subclassed by feature developers.
 *
 * @param S The type of the screen's state.
 * @param I The type of the screen's intents.
 * @param initialState The initial state of the screen.
 * @param reducer The [Reducer] responsible for state evolution.
 * @param sideEffects The [SideEffectMap] that maps intents to their side effects.
 * @param middlewares A list of [Middleware]s for observing the MVI loop (e.g., for logging).
 */
abstract class ViewModel<S, I : Any>(
    initialState: S,
    private val reducer: Reducer<S, I>,
    private val sideEffects: SideEffectMap<S, I>,
    private val middlewares: List<Middleware<S, I>> = listOf(LogMiddleware()),
) : ViewModel() {
    /**
     * A hot flow for emitting one-shot navigation events.
     *
     * Using a [MutableSharedFlow] ensures that navigation events are delivered once and are not
     * re-emitted on configuration changes (e.g., screen rotation), preventing duplicate
     * navigation actions. It is designed to be collected by a UI-level coordinator.
     */
    private val _navigationEvents =
        MutableSharedFlow<NavigationEvent>(
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
        )
    val navigationEvents: SharedFlow<NavigationEvent> = _navigationEvents

    /**
     * A hot flow for emitting one-shot UI events like Snackbars or Toasts.
     * This follows the same pattern as `navigationEvents` to ensure events are consumed once.
     */
    private val _uiEvents =
        MutableSharedFlow<UiEvent>(
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
        )
    val uiEvents: SharedFlow<UiEvent> = _uiEvents

    /**
     * The single source of truth for the screen's UI state.
     *
     * Using a [MutableStateFlow], it allows the UI to observe state changes reactively
     * and always have access to the latest state.
     */
    private val _uiState = MutableStateFlow(value = initialState)
    val uiState: StateFlow<S> = _uiState

    /**
     * The sole entry point for dispatching an [I]ntent to the ViewModel.
     *
     * This function orchestrates the MVI loop:
     * 1. Notifies middlewares that an intent has been received.
     * 2. Captures the current state and uses the [reducer] to calculate the new state.
     * 3. Updates the [_uiState] to reflect the new state immediately.
     * 4. Notifies middlewares of the new state.
     * 5. Retrieves the corresponding [com.buildkt.mvi.SideEffect] from the [sideEffects] map.
     * 6. If a side effect exists, it is executed within the [viewModelScope].
     * 7. The [SideEffectResult] is processed, potentially triggering new intents and continuing the loop.
     *
     * @param intent The intent to be processed.
     */
    fun onIntent(intent: I) {
        viewModelScope.launch {
            middlewares.forEach { it.onIntent(intent) }

            // Capture the state at the time of reduction to ensure consistency for the side effect.
            val stateAtTimeOfReduction = uiState.value

            val newUiState = reducer.reduce(state = stateAtTimeOfReduction, intent)
            _uiState.value = newUiState
            middlewares.forEach { it.onStateReduced(newState = newUiState, intent) }

            val sideEffect = sideEffects[intent]
            if (sideEffect != null) {
                middlewares.forEach { it.onSideEffect(sideEffect, intent) }

                // Execute the side effect with the same state snapshot used for its creation.
                val sideEffectResult = sideEffect(state = stateAtTimeOfReduction, intent)
                middlewares.forEach { it.onSideEffectResult(sideEffectResult, intent) }

                when (sideEffectResult) {
                    is SideEffectResult.NewIntent -> onIntent(intent = sideEffectResult.intent)
                    is SideEffectResult.NewIntents -> sideEffectResult.intents.collect(::onIntent)
                    is SideEffectResult.Navigation -> _navigationEvents.emit(sideEffectResult.event as NavigationEvent)
                    is SideEffectResult.ShowUiEvent -> _uiEvents.emit(sideEffectResult.event as UiEvent)
                    is SideEffectResult.NoOp -> { /* Do nothing */ }
                }
            }
        }
    }
}
