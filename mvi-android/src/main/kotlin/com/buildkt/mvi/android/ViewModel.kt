package com.buildkt.mvi.android

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buildkt.mvi.DefaultStateHolder
import com.buildkt.mvi.Middleware
import com.buildkt.mvi.Reducer
import com.buildkt.mvi.SideEffectMap
import com.buildkt.mvi.StateHistoryStorage
import com.buildkt.mvi.StateHolder
import com.buildkt.mvi.TimeTravelDebugger
import com.buildkt.mvi.TimeTravelMiddleware
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
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
 * @param State The type of the screen's state.
 * @param Intent The type of the screen's intents.
 * @param stateHistoryStorage Optional storage for time-travel history. When set, history is saved
 * on [onCleared] for persistence across process death. Use with [com.buildkt.mvi.TimeTravelDebuggerConfig.stateHistoryStorage].
 * Persistence on clear is best-effort: the save is not awaited, so if the process is killed
 * immediately after [onCleared], the save may not complete.
 * @param onPersistenceError Optional callback when saving history fails (e.g. for logging).
 */
abstract class ViewModel<State, Intent : Any>(
    initialState: State,
    reducer: Reducer<State, Intent>,
    sideEffects: SideEffectMap<State, Intent>,
    private val middlewares: List<Middleware<State, Intent>> = emptyList(),
    private val stateHistoryStorage: StateHistoryStorage<State, Intent>? = null,
    private val onPersistenceError: ((Throwable) -> Unit)? = null,
) : ViewModel(),
    StateHolder<State, Intent, NavigationEvent, UiEvent> {
    /**
     * Scope used for persistence on clear; independent of viewModelScope so save can complete
     * after the ViewModel is cleared.
     */
    private val persistenceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val stateHolder =
        DefaultStateHolder<State, Intent, NavigationEvent, UiEvent>(
            initialState = initialState,
            reducer = reducer,
            middlewares = middlewares,
            sideEffects = sideEffects,
            coroutineScope = viewModelScope,
        )

    override val navigationEvents: SharedFlow<NavigationEvent> = stateHolder.navigationEvents
    override val uiEvents: SharedFlow<UiEvent> = stateHolder.uiEvents
    override val uiState: StateFlow<State> = stateHolder.uiState

    override fun onIntent(intent: Intent) {
        stateHolder.onIntent(intent)
    }

    /**
     * Returns a time-travel debugger when this ViewModel has TimeTravelMiddleware in its middlewares.
     * Used by the time-travel overlay; does not expose the state holder or middleware.
     */
    fun getTimeTravelDebugger(): TimeTravelDebugger<State, Intent, NavigationEvent, UiEvent>? =
        middlewares
            .filterIsInstance<TimeTravelMiddleware<State, Intent>>()
            .firstOrNull()
            ?.let { TimeTravelDebugger(stateHolder, timeTravelMiddleware = it) }

    override fun onCleared() {
        persistHistoryBeforeClear()
        super.onCleared()
    }

    private fun persistHistoryBeforeClear() {
        val stateHistoryStorage = stateHistoryStorage ?: return
        val timeTravelDebugger = getTimeTravelDebugger() ?: return
        val historyFlow = timeTravelDebugger.getHistoryStateFlow()

        persistenceScope.launch {
            try {
                val history = historyFlow.first()
                if (history.isNotEmpty()) {
                    stateHistoryStorage.saveHistory(history)
                }
            } catch (e: Exception) {
                onPersistenceError?.invoke(e)
            }
        }
    }
}
