package com.buildkt.mvi

/**
 * Marks an `Intent` class as one that triggers an asynchronous [SideEffect].
 *
 * The annotation processor uses this to identify which intents require handling
 * outside of the pure `Reducer`. For each annotated intent, the processor generates
 * a corresponding property in the `SideEffectRouter` and its associated DSL builder.
 *
 * This allows you to easily provide an implementation for the side effect (e.g., an API call,
 * database query, or navigation) in your navigation graph's composition block.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class TriggersSideEffect
