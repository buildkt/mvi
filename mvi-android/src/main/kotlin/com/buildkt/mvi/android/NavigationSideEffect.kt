package com.buildkt.mvi.android

import com.buildkt.mvi.SideEffect
import com.buildkt.mvi.SideEffectResult
import kotlin.reflect.KClass

/**
 * Creates a [SideEffect] that triggers a navigation event to a specific route string.
 *
 * This is the most common navigation side effect. It simplifies navigating to a destination
 * whose route may depend on the current state or the triggering intent.
 *
 * Example:
 * sideEffects {
 *   addressSelected = routeTo { state, intent ->
 *      "address-details/${intent.addressId}"
 *   }
 * }
 *
 * @param S The screen's `UiState` type.
 * @param I The screen's `Intent` type.
 * @param route A lambda that receives the current state and intent and must return a route `String`.
 * @return A [SideEffect] that emits a [NavigationEvent.To].
 */
fun <S, I> routeTo(route: (state: S, intent: I) -> String): SideEffect<S, I> =
    SideEffect { state, intent ->
        SideEffectResult.Navigation(event = NavigationEvent.To(route = route(state, intent)))
    }

/**
 * Creates a [SideEffect] that triggers a navigation event to a route string, with the intent
 * typed as a specific subclass [T] of [I] so the lambda receives the concrete intent without casting.
 *
 * Use this when the side effect is mapped to a single intent type (e.g. [AddressListIntent.EditAddress])
 * and you want to access intent properties without casting.
 *
 * Example:
 * sideEffects {
 *   editAddress = routeToIntent(AddressListIntent.EditAddress::class) { _, intent ->
 *     editAddressRoute(addressId = intent.addressId)
 *   }
 * }
 *
 * @param intentClass The [KClass] of the concrete intent type that triggers this side effect.
 * @param route A lambda that receives the current state and the typed intent and returns a route `String`.
 * @return A [SideEffect] that emits a [NavigationEvent.To].
 */
fun <S, I : Any, T : I> routeToIntent(route: (state: S, intent: T) -> String): SideEffect<S, I> =
    SideEffect { state, intent ->
      //  @Suppress("UNCHECKED_CAST")
       // if (intentClass.isInstance(intent)) {
            SideEffectResult.Navigation(event = NavigationEvent.To(route = route(state, intent as T)))
        //} else {
         //   SideEffectResult.NoOp
        //}
    }

/**
 * Creates a [SideEffect] that triggers a predefined, static [NavigationEvent].
 * This is ideal for simple, non-dynamic navigation actions, such as popping the back stack.
 *
 * Example:
 * sideEffects {
 *   addressSelected = navigate(event = NavigationEvent.PopBack)
 * }
 *
 * @param event The static [NavigationEvent] to be emitted (e.g., `NavigationEvent.PopBack`).
 * @return A [SideEffect] that emits the provided navigation event.
 */
fun <S, I> navigate(event: NavigationEvent): SideEffect<S, I> =
    SideEffect { _, _ ->
        SideEffectResult.Navigation(event = event)
    }

/**
 * Creates a [SideEffect] that dynamically constructs a [NavigationEvent].
 *
 * This is an advanced use case for when the entire navigation action, not just the route,
 * needs to be determined at runtime based on the current state and intent.
 *
 * Example:
 * sideEffects {
 *   addressSelected = navigate { state, intent ->
 *      SideEffectResult.Navigation(event = NavigationEvent.To(route = route(state, intent)))
 *   }
 * }
 *
 * @param S The screen's `UiState` type.
 * @param I The screen's `Intent` type.
 * @param event A lambda that receives the current state and intent and must return a [NavigationEvent].
 * @return A [SideEffect] that emits the dynamically created [NavigationEvent].
 */
fun <S, I> navigate(event: (state: S, intent: I) -> NavigationEvent): SideEffect<S, I> =
    SideEffect { state, intent ->
        SideEffectResult.Navigation(event = event(state, intent))
    }

/**
 * Creates a [SideEffect] that dynamically constructs a [NavigationEvent], with the intent
 * typed as a specific subclass [T] of [I] so the lambda receives the concrete intent without casting.
 *
 * Use this when the side effect is mapped to a single intent type and you want to access
 * intent properties without casting.
 *
 * Example:
 * sideEffects {
 *   addressSelected = navigateIntent(AddressListIntent.AddressSelected::class) { _, intent ->
 *     NavigationEvent.PopBackWithResult(key = ADDRESS_FLOW_RESULT, result = intent.addressId)
 *   }
 * }
 *
 * @param intentClass The [KClass] of the concrete intent type that triggers this side effect.
 * @param event A lambda that receives the current state and the typed intent and returns a [NavigationEvent].
 * @return A [SideEffect] that emits the dynamically created [NavigationEvent].
 */
fun <S, I : Any, T : I> navigateIntent(event: (state: S, intent: T) -> NavigationEvent): SideEffect<S, I> =
    SideEffect { state, intent ->
        @Suppress("UNCHECKED_CAST")
       // if (intentClass.isInstance(intent)) {
            SideEffectResult.Navigation(event = event(state, intent as T))
        //} else {
         //   SideEffectResult.NoOp
        //}
    }
