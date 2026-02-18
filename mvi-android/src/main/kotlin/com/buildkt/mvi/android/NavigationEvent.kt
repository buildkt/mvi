package com.buildkt.mvi.android

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * A sealed interface representing the concrete navigation actions that can be triggered from
 * within the MVI architecture on the Android platform.
 *
 * These events are emitted by the [ViewModel] and collected by a UI-level coordinator, which
 * translates them into actual [NavController] calls. This decouples navigation logic from the
 * core business logic.
 */
sealed interface NavigationEvent {
    /** Represents a navigation action to a specific route string. */
    data class To(val route: String) : NavigationEvent

    /** Represents a navigation action to pop the current back stack entry. */
    data object PopBack : NavigationEvent

    /**
     * Represents a navigation action that pops the back stack up to a specific route.
     * @param route The destination route to pop up to.
     * @param inclusive If true, the destination specified by [route] is also popped.
     */
    data class PopUpTo(val route: String, val inclusive: Boolean = false) : NavigationEvent

    /**
     * Represents a navigation action that pops the back stack and returns a result
     * to the previous screen.
     * @param T The type of the result being passed back.
     * @param key The key to associate with the result in the previous screen's `SavedStateHandle`.
     * @param result The value to be returned.
     */
    data class PopBackWithResult<T>(val key: String, val result: T) : NavigationEvent
}

/**
 * A Composable effect handler that collects [NavigationEvent]s from a [ViewModel]
 * and executes them using the provided [NavController].
 *
 * This function acts as the bridge between the MVI's navigation desires and the Android
 * framework's navigation implementation. It uses a [LaunchedEffect] to ensure the collection
 * is lifecycle-aware, starting when the composable enters the composition and cancelling
 * when it leaves.
 *
 * @param viewModel The `ViewModel` instance that emits the navigation events.
 * @param navController The `NavController` instance that will perform the navigation actions.
 */
@Composable
fun <S, I : Any> CollectNavigationEvents(viewModel: ViewModel<S, I>, navController: NavController) {
    LaunchedEffect(viewModel, navController) {
        viewModel.navigationEvents
            .onEach { navEvent -> navController.navigate(navEvent = navEvent) }
            .launchIn(scope = this)
    }
}

/**
 * A private extension function that translates a [NavigationEvent] into a specific
 * [NavController] method call.
 *
 * By being private, it encapsulates the implementation details of how each abstract event
 * is mapped to the concrete Jetpack Navigation API.
 */
private fun NavController.navigate(navEvent: NavigationEvent) {
    when (navEvent) {
        is NavigationEvent.To -> navigate(route = navEvent.route)
        is NavigationEvent.PopBack -> popBackStack()
        is NavigationEvent.PopUpTo -> popBackStack(navEvent.route, navEvent.inclusive)
        is NavigationEvent.PopBackWithResult<*> -> {
            // Set the result on the SavedStateHandle of the previous back stack entry.
            previousBackStackEntry?.savedStateHandle?.set(navEvent.key, navEvent.result)
            popBackStack()
        }
    }
}
