package com.buildkt.mvi

import kotlin.reflect.KClass

/**
 * Designates a Composable function as the main entry point for a screen powered by the MVI framework.
 *
 * This annotation is the primary hook for the MVI code generator. Applying it to a
 * Composable function identifies it as the "View" layer of a screen, enabling the
 * automatic generation of platform-specific boilerplate code required to run the MVI loop.
 *
 * For the specified [platform], the processor generates:
 * - A `ViewModel` that hosts the MVI engine.
 * - A `SideEffectRouter` to handle asynchronous operations.
 * - A `NavGraphBuilder` extension function with a DSL for providing the `reducer`, `sideEffects`,
 *   and `middlewares`, seamlessly integrating the screen into the Navigation Component.
 *
 * @param uiState The [KClass] of the screen's state data class.
 * @param intent The [KClass] of the screen's sealed intent interface.
 * @param platform The target platform for which to generate code. Defaults to Android.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class MviScreen(
    val uiState: KClass<*>,
    val intent: KClass<*>,
    val platform: Platform = Platform.ANDROID
)

/**
 * An enumeration of the supported target platforms for code generation.
 * This allows the annotation processor to generate platform-specific boilerplate.
 */
enum class Platform {
    /** Generates code for Android, including `ViewModel`, `ViewModelProvider.Factory`, and `NavGraphBuilder` extensions. */
    ANDROID
}