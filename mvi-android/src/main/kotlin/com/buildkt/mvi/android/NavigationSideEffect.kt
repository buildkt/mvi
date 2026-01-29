package com.buildkt.mvi.android

import com.buildkt.mvi.SideEffect
import com.buildkt.mvi.SideEffectResult

/**
 * Creates a [SideEffect] that triggers a navigation event.
 *
 * This function has two overloads:
 * 1.  **Static Event**: Pass a predefined [NavigationEvent] directly. This is ideal for simple,
 *     non-dynamic navigation actions, such as popping the back stack.
 *
 *     Example:
 *     ```kotlin
 *     sideEffects {
 *       goBack = navigate(event = NavigationEvent.PopBack)
 *     }
 *     ```
 *
 * 2.  **Dynamic Event**: Provide a lambda that receives the current state and intent and
 *     returns a [NavigationEvent]. This is an advanced use case for when the entire navigation
 *     action, not just the route, needs to be determined at runtime.
 *
 *     Example:
 *     ```kotlin
 *     sideEffects {
 *       processAction = navigate { state, intent ->
 *         if (state.isFlowComplete) {
 *           NavigationEvent.PopBackWithResult(key = "result", result = state.resultData)
 *         } else {
 *           NavigationEvent.To(route = "next-step/${intent.id}")
 *         }
 *       }
 *     }
 *     ```
 *
 * @param S The screen's `UiState` type.
 * @param I The screen's `Intent` type.
 * @param event A static [NavigationEvent] or a lambda `(S, I) -> NavigationEvent` to dynamically create one.
 * @return A [SideEffect] that emits the resulting [NavigationEvent].
 */
fun <S, I> navigateToEvent(event: NavigationEvent): SideEffect<S, I> =
    SideEffect { _, _ ->
        SideEffectResult.Navigation(event = event)
    }

/**
 * Creates a [SideEffect] that triggers a navigation event.
 *
 * This function has two overloads:
 * 1.  **Static Event**: Pass a predefined [NavigationEvent] directly. This is ideal for simple,
 *     non-dynamic navigation actions, such as popping the back stack.
 *
 *     Example:
 *     ```kotlin
 *     sideEffects {
 *       goBack = navigate(event = NavigationEvent.PopBack)
 *     }
 *     ```
 *
 * 2.  **Dynamic Event**: Provide a lambda that receives the current state and intent and
 *     returns a [NavigationEvent]. This is an advanced use case for when the entire navigation
 *     action, not just the route, needs to be determined at runtime.
 *
 *     Example:
 *     ```kotlin
 *     sideEffects {
 *       processAction = navigate { state, intent ->
 *         if (state.isFlowComplete) {
 *           NavigationEvent.PopBackWithResult(key = "result", result = state.resultData)
 *         } else {
 *           NavigationEvent.To(route = "next-step/${intent.id}")
 *         }
 *       }
 *     }
 *     ```
 *
 * @param S The screen's `UiState` type.
 * @param I The screen's `Intent` type.
 * @param event A static [NavigationEvent] or a lambda `(S, I) -> NavigationEvent` to dynamically create one.
 * @return A [SideEffect] that emits the resulting [NavigationEvent].
 */
inline fun <S, I : Any, reified T : I> navigateToEvent(crossinline event: (state: S, intent: T) -> NavigationEvent): SideEffect<S, I> =
    SideEffect { state, intent ->
        if (intent is T) {
            SideEffectResult.Navigation(event = event(state, intent))
        } else {
             SideEffectResult.NoOp
        }
    }

/**
 * Creates a [SideEffect] that dynamically constructs a [NavigationEvent].
 *
 * This is an advanced use case for when the entire navigation action, not just the route,
 * needs to be determined at runtime based on the current state and intent.
 *
 * Example:
 * sideEffects {
 *   addressSelected = navigateRoute { state, intent ->
 *      NavigationEvent.To(route = "details/${intent.addressId}")
 *   }
 * }
 *
 * @param S The screen's `UiState` type.
 * @param I The screen's `Intent` type.
 * @param event A lambda that receives the current state and intent and must return a [NavigationEvent].
 * @return A [SideEffect] that emits the dynamically created [NavigationEvent].
 */
inline fun <S, I : Any, reified T : I> navigateToRoute(crossinline route: (state: S, intent: T) -> String): SideEffect<S, I> =
    SideEffect { state, intent ->
        if (intent is T) {
            SideEffectResult.Navigation(event = NavigationEvent.To(route = route(state, intent)))
        } else {
            SideEffectResult.NoOp
        }
    }