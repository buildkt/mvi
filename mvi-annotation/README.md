# MVI Annotation Module
This module contains the core annotations that power the MVI framework's code generation engine.
By annotating your code, you provide the necessary metadata for the `:mvi:annotation-processor` to generate the required boilerplate,
seamlessly integrating your UI with the MVI pattern.

## Core Annotations
1. `@MviScreen` This is the most important annotation in the framework. It designates a Composable
function as the main entry point for a screen, acting as the "View" in the MVI pattern.
Applying @MviScreen to a Composable function triggers the annotation processor to generate all the necessary components to run the MVI loop.

Usage:
```kotlin
@MviScreen(
    uiState = CreateAddressState::class,
    intent = CreateAddressIntent::class
)
@Composable
fun CreateAddressPane(
    state: CreateAddressState,
    onIntent: (CreateAddressIntent) -> Unit,
    uiEvents: Flow<UiEvent>,
    @NavArgument addressId: String? // Example of a navigation parameter
) {
// Your UI implementation here...
}
```

Key Parameters:
- `uiState`: The KClass of the data class representing your screen's state (e.g., CreateAddressState::class).
- `intent`: The KClass of the sealed interface representing all possible user actions and async results for the screen (e.g., CreateAddressIntent::class).
- `platform`: The target platform for code generation. It defaults to `Platform.ANDROID`, which generates an Android-specific ViewModel, ViewModelProvider.Factory, and a NavGraphBuilder extension function.

2. `@NavArgument` This annotation marks a parameter within a Composable function annotated with `@MviScreen` as a navigation argument.
The annotation processor uses this to identify which parameters need to be extracted from the NavBackStackEntry's arguments inside the generated NavGraphBuilder function.
This makes the passing of arguments from a navigation route to your screen's Pane explicit and robust.

Usage:
```kotlin
@MviScreen(/* ... */)
@Composable
fun EditAddressPane(
    state: EditAddressState,
    onIntent: (EditAddressIntent) -> Unit,
    uiEvents: Flow<UiEvent>,
    @NavArgument addressId: String? // The processor will generate code to get "addressId" from the arguments
) {
}

```

By using these annotations, you opt into the MVI framework's convention-over-configuration approach,
significantly reducing boilerplate and ensuring your screens are structured in a consistent, predictable, and robust way.
