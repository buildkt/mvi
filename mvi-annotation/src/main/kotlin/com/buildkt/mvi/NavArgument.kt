package com.buildkt.mvi

/**
 * Marks a parameter within a Composable function annotated with `@MviScreen` as a
 * navigation argument.
 *
 * The annotation processor uses this to distinguish between parameters that are provided
 * by the MVI framework (like `state`, `onIntent`) and those that must be passed
 * down from the navigation graph entry (e.g., an item ID from the route).
 *
 * Example:
 * @MviScreen(...)
 * ```
 * @Composable
 * fun EditAddressPane(
 *  state: EditAddressState,
 *  onIntent: (EditAddressIntent) -> Unit,
 *  uiEvents: Flow<UiEvent>,
 *  @NavArgument addressId: String? // This is a navigation argument
 * ) { ... }
 * ```
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class NavArgument
