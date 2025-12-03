package com.buildkt.mvi.android

import android.content.Context
import android.widget.Toast
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.buildkt.mvi.SideEffect
import com.buildkt.mvi.SideEffectResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Defines a hierarchy of one-off, transient UI events that are not part of the main screen state.
 * These events are self-executing, containing the logic needed to display themselves.
 * This encapsulates the display logic (e.g., showing a snackbar) with the data for the event.
 */
sealed interface UiEvent {
    /**
     * Executes the logic to display this UI event.
     * @param context The Android [Context] required for operations like showing a `Toast`.
     * @param snackbarHostState The [SnackbarHostState] used to display `Snackbar` messages.
     */
    suspend fun show(
        context: Context,
        snackbarHostState: SnackbarHostState,
    )

    /** A UI event that displays a [Snackbar] message. */
    data class ShowSnackbar(
        val message: String,
        val actionLabel: String? = null,
    ) : UiEvent {
        override suspend fun show(
            context: Context,
            snackbarHostState: SnackbarHostState,
        ) {
            snackbarHostState.showSnackbar(message = message, actionLabel = actionLabel)
        }
    }

    /** A UI event that displays a short [Toast] message. */
    data class ShowToast(
        val message: String,
    ) : UiEvent {
        override suspend fun show(
            context: Context,
            snackbarHostState: SnackbarHostState,
        ) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
}

/**
 * Creates a [SideEffect] that results in showing a [Snackbar].
 *
 * This is a factory function that simplifies the creation of a side effect for a common UI action.
 *
 * @param message The text to be displayed in the Snackbar.
 * @param actionLabel Optional text for the Snackbar's action button.
 */
fun <S, I> showSnackbar(
    message: String,
    actionLabel: String? = null,
): SideEffect<S, I> =
    SideEffect { _, _ ->
        SideEffectResult.ShowUiEvent(event = UiEvent.ShowSnackbar(message, actionLabel))
    }

/**
 * Creates a [SideEffect] that results in showing a [Toast].
 *
 * @param message The text to be displayed in the Toast.
 */
fun <S, I> showToast(message: String): SideEffect<S, I> =
    SideEffect { _, _ ->
        SideEffectResult.ShowUiEvent(event = UiEvent.ShowToast(message))
    }

/**
 * A Composable effect handler that collects [UiEvent]s from a flow
 * and executes their `show` method.
 *
 * This function acts as the bridge between the MVI-driven request to show a UI event
 * and its actual execution on the Android platform. It uses a [LaunchedEffect] to ensure
 * the collection is lifecycle-aware.
 *
 * @param uiEvents The flow of [UiEvent]s emitted by a ViewModel.
 * @param snackbarHostState The [SnackbarHostState] from the current composition,
 * passed to the event for execution.
 */
@Composable
fun CollectUiEvents(
    uiEvents: Flow<UiEvent>,
    snackbarHostState: SnackbarHostState,
) {
    val context = LocalContext.current
    LaunchedEffect(uiEvents, snackbarHostState, context) {
        uiEvents
            .onEach { event -> event.show(context, snackbarHostState) }
            .launchIn(scope = this)
    }
}
