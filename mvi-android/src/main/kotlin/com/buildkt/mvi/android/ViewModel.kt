package com.buildkt.mvi.android

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buildkt.mvi.Middleware
import com.buildkt.mvi.StateHolder
import com.buildkt.mvi.StateHolderImpl
import com.buildkt.mvi.Reducer
import com.buildkt.mvi.SideEffectMap
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * An Android-specific `ViewModel` that acts as the engine for the MVI architecture.
 *
 * It hosts the state, processes intents, and manages the lifecycle of side effects, connecting
 * the pure MVI core (`Reducer`, `SideEffectMap`) to the Android ecosystem (`viewModelScope`).
 *
 * This class is designed to be automatically generated and instantiated by an annotation processor,
 * not manually subclassed by feature developers.
 *
 * @param State The type of the screen's state.
 * @param Intent The type of the screen's intents.
 * @param initialState The initial state of the screen.
 * @param reducer The [Reducer] responsible for state evolution.
 * @param sideEffects The [SideEffectMap] that maps intents to their side effects.
 * @param middlewares A list of [Middleware]s for observing the MVI loop (e.g., for logging).
 */
abstract class ViewModel<State, Intent : Any>(
    initialState: State,
    private val reducer: Reducer<State, Intent>,
    private val sideEffects: SideEffectMap<State, Intent>,
    private val middlewares: List<Middleware<State, Intent>>,
) : ViewModel(), StateHolder<State, Intent, NavigationEvent, UiEvent> {

    private val stateHolder = StateHolderImpl<State, Intent, NavigationEvent, UiEvent>(
        initialState = initialState,
        reducer = reducer,
        sideEffects = sideEffects,
        coroutineScope = viewModelScope,
        middlewares = middlewares,
    )

    override val navigationEvents: SharedFlow<NavigationEvent> = stateHolder.navigationEvents

    override val uiEvents: SharedFlow<UiEvent> = stateHolder.uiEvents

    override val uiState: StateFlow<State> = stateHolder.uiState

    override fun onIntent(intent: Intent) {
        stateHolder.onIntent(intent)
    }
}
