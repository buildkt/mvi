# BuildKt MVI: A Modern Android Architecture, Without the Boilerplate
| Library              | Version                                                                                               | 
|:---------------------|:------------------------------------------------------------------------------------------------------| 
| Android              | ![Maven Central Version](https://img.shields.io/maven-central/v/com.buildkt.mvi/android)              |
| Annotations          | ![Maven Central Version](https://img.shields.io/maven-central/v/com.buildkt.mvi/annotation)           |
| Annotation processor | ![Maven Central Version](https://img.shields.io/maven-central/v/com.buildkt.mvi/annotation-processor) |
| Core                 | ![Maven Central Version](https://img.shields.io/maven-central/v/com.buildkt.mvi/core)                 |

BuildKt MVI is an opinionated, highly-decoupled MVI (Model-View-Intent) framework for Android, built from 
the ground up for Jetpack Compose. Its goal is simple: to let you build scalable and testable applications by 
eliminating nearly all the repetitive code associated with the MVI pattern.

This is achieved through a powerful annotation processor (KSP) that automates the plumbing, so you can 
focus on what truly matters: your app's business logic.

## Why BuildKt MVI?
In modern Android development, MVI is a preferred pattern for its predictability and testability. 
However, implementing it manually requires a significant amount of boilerplate: creating ViewModels, Factories, state flows,
event channels, and wiring everything up to navigation.BuildKt MVI solves this. 

With a single annotation, you generate the entire skeleton for your screen.

| Without BuildKt MVI (Manual Code)           | With BuildKt MVI (Generated Code) | 
|:--------------------------------------------|:----------------------------------| 
| ✅ MyScreenPane.kt                           | ✅ MyScreenPane.kt                 |
| ❌ MyScreenViewModel.kt                      | ✨ Auto-Generated                  | 
| ❌ MyScreenViewModelFactory.kt               | ✨ Auto-Generated                  | 
| ❌ NavGraphBuilder.myScreen()                | ✨ Auto-Generated                  | 
| ❌ Manual Nav Argument handling              | ✨ Auto-Generated                  | 
| ❌ Manual StateFlow & SharedFlow management  | ✨ Auto-Generated                  |

Your only job is to define the business logic.

## Installation
Add the `mavenCentral()` repository to your settings.gradle.kts (if you haven't already) and add the 
following dependencies to your feature module.

```kotlin
// In your module's build.gradle.kts

plugins {
    // Ensure the KSP plugin is applied
    id("com.google.devtools.ksp")
}

dependencies {
    // MVI Android 
    implementation("com.buildkt.mvi:android:<latest>")
    
    // KSP Annotation Processor
    ksp("com.buildkt.mvi:annotation-processor:<latest>")
}
```

## Quick Start: Creating Your First Screen
Let's see how easy it is to create a user profile screen.

1. Define your State and Intents
```kotlin
data class ProfileUiState(
    val isLoading: Boolean = true,
    val userName: String = ""
)

sealed class ProfileIntent {
  @TriggersSideEffect
  data object LoadUserName : ProfileIntent

  data class OnLoadUserNameLoaded(val userName: String) : ProfileIntent
}
```

2. Create your Composable Pane and Annotate It
Annotate your Composable with `@MviScreen`. The only required parameters are `state` and `onIntent`. 
The `uiEvents` parameter for collecting one-shot events is **optional**.

```kotlin
@MviScreen(
    uiState = ProfileUiState::class,
    intent = ProfileIntent::class
)
@Composable
fun ProfilePane(
    state: ProfileUiState,
    onIntent: (ProfileIntent) -> Unit,
    uiEvents: Flow<UiEvent>, // Optional. Use with `CollectUiEvents()` to handle one-shot events, like showing a toast.
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    CollectUiEvents(uiEvents, snackbarHostState)

    LaunchedEffect(true) { onIntent(ProfileIntent.LoadUserName) }

    ScreenScaffold(
      modifier = modifier,
      isLoading = state.isLoading,
      snackbarHostState = snackbarHostState,
    ) {
      Text("Hello world!")
    }
}
```

3. Implement the Logic in Your Navigation Graph
The processor generates an extension function for `NavGraphBuilder`. Use it to define your reducer and sideEffects.
```kotlin
NavHost(navController, startDestination = "profile") {
    // KSP-generated function
    profilePane(navController = navController, route = "profile") {
        // Inject a reducer to update the state
        reducer = Reducer { state, intent ->
            when (intent) {
                is LoadUserName -> state.copy(isLoading = true)
                is OnLoadUserNameLoaded -> state.copy(isLoading = false, userName = intent.name)
            }
        }

        // Inject the side effects triggered by an Intent  
        sideEffects {
            loadUserName = sideEffect {
                delay(2000)  // Simulate an API call
                
                newIntent(OnUserNameLoaded(name = "Matias")) // Return an optional Intent as the result
            }
        }
    }
}
```

## Core Principles & Module Documentation
This framework is designed around a unidirectional data flow and a strict separation of concerns.
- Unidirectional Data Flow: State flows down, events flow up. Predictable and easy to debug.
- Single Source of Truth: The UiState is the single, immutable source of truth for your UI.
- Separation of Concerns:
  - UI (@Composable): Only displays state and emits user intents.
  - Reducer: A pure function that evolves the current state based on an intent.
  - SideEffect: Handles all asynchronous work (API calls, database access, navigation).
 
The framework is split into four distinct modules:
| Module   | Description | 
| :------: | :------------ |  
| mvi-core | The platform-agnostic core of the MVI pattern. Defines Reducer, SideEffect, etc. |
| mvi-annotation | Contains the KSP annotations (@MviScreen, @TriggersSideEffect) that drive code generation. | 
| mvi-android | Android-specific helpers, including the base ViewModel and event collectors. | 
| mvi-annotation-processor | The KSP processor that generates the MVI boilerplate code. |

### 1. mvi-core
This module is written in pure Kotlin with no Android dependencies, making it suitable for multiplatform use.
- Reducer<S, I>: A functional interface for a pure function reduce(S, I): S. Its sole responsibility is to produce a new state.
- SideEffect<S, I>: A functional interface that executes business logic. It can access the current state and returns a SideEffectResult.
- SideEffectResult<I>: A sealed class representing the outcome of a SideEffect (NewIntent, Navigation, ShowUiEvent, or NoOp).
- Middleware<S, I>: An abstract class for observing the MVI loop (for logging, analytics, etc.).

#### Debounced side effects
For intents that fire frequently (e.g. search-as-you-type), wrap a side effect with `debounced(delayMs, sideEffect)`. The runtime will **cancel** any existing pending run for that effect and start a new one that waits `delayMs`, then runs the wrapped effect with the **current** state and intent. Only the last run completes. Example:

```kotlin
sideEffects {
    searchQueryChanged = debounced(300L, searchInPredefinedHabits(repository = habitRepository))
}
```

The UI can dispatch `SearchQueryChanged(query)` on every keystroke; the reducer updates `state.query`, and after 300ms of no typing a single search runs with the latest `state.query`. 

### 2. mvi-annotation
This lightweight module contains only the annotations used to configure code generation. 
- @MviScreen(...): Marks a @Composable as a screen, triggering the generation of a ViewModel and a NavGraphBuilder extension function.
- @TriggersSideEffect: Marks an Intent class as one that should be handled by a SideEffect, which generates the on<...> DSL.
- @NavArgument: Marks a @Composable parameter as a navigation argument to be extracted from the NavBackStackEntry.
 
### 3. mvi-android
This module connects the platform-agnostic core to the Android framework. 
- ViewModel<S, I>: An abstract Android ViewModel that orchestrates the MVI loop.
- UiEvent: A sealed interface for one-shot UI events (e.g., ShowSnackbar).
- NavigationEvent: A sealed interface for one-shot navigation events (e.g., To, PopBack).
- CollectUiEvents(...) & CollectNavigationEvents(...): Composable helpers to collect and handle one-shot events safely.

## 4. mvi-annotation-processor
This is the KSP implementation that works behind the scenes. When you annotate a composable with @MviScreen, it generates 
the corresponding ViewModel, its Factory, and the NavGraphBuilder extension function, eliminating MVI boilerplate.