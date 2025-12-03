# MVI Core Module
This module contains the platform-agnostic core components of the buildkt MVI (Model-View-Intent) architecture.
It provides the fundamental, pure-Kotlin building blocks for state management, ensuring a predictable, unidirectional data flow.

The primary goal of this module is to enforce a strict separation of concerns between pure state-logic and asynchronous operations (side effects), 
making it suitable for Kotlin Multiplatform projects.

## Core Components
### 1. `Reducer<S, I>`
The Reducer is a core component of the architecture, responsible only for evolving the screen's state.
- Type: A functional interface (fun interface).
- Purpose: A pure function that calculates the new screen state based on the current state and an incoming intent.
- Rule: This function must not perform any I/O, network requests, or any other asynchronous work. Its only job is to produce a new state from an old state and an intent.

### 2. SideEffectMap<S, I>
The SideEffectMap defines the relationship between an Intent and the asynchronous work it needs to trigger.
- Type: A functional interface (fun interface).
- Purpose: It acts as a lookup table. Given an intent, it returns the corresponding SideEffect to be executed, or null if the intent does not trigger any side effect.
- Rule: This map does not execute the work; it only declares the "what" (the SideEffect) for a given "when" (the Intent). The execution is handled by the platform-specific ViewModel.

### 3. SideEffect<S, I>
A SideEffect represents a unit of work that happens outside the Reducer's pure state-update cycle. This is where all asynchronous operations live.
- Type: A functional interface (fun interface).
- Execution: It's a suspend function that takes the current state and the triggering intent as context.
- Result: It returns a SideEffectResult, which tells the ViewModel what to do next. 

### 4. SideEffectResult<I>
This sealed class represents the possible outcomes of a SideEffect execution.
- NoOp: The side effect completed without producing any further action.
- NewIntent(intent: I): The side effect produced a single new intent (e.g., UsersLoaded(users)), which is then fed back into the ViewModel to continue the data flow.
- NewIntents(intents: Flow<I>): The side effect produced a stream of new intents over time (e.g., from a database subscription).
- Navigation(event: Any): The side effect triggered a platform-agnostic navigation event. This generic event is interpreted by a platform-specific module (like :mvi-android).
- ShowUiEvent(event: Any): The side effect triggered a transient UI event (like a Toast or Snackbar). This is also interpreted by a platform-specific module.

### 5. Middleware<S, I>
A Middleware allows for observing and reacting to events within the MVI loop. It is an abstract class that provides
hooks into the data flow, making it a powerful mechanism for handling cross-cutting concerns like logging, analytics, or crash reporting in a platform-agnostic way.

## Helper Functions
This module also provides convenient builders for creating common SideEffect types:
- `sideEffect { ... }`: For simple, one-shot async operations that may return a single new intent.
- `observableSideEffect { ... }`: For subscribing to a Flow that emits intents over time.
- `parallelSideEffect(...)`: For executing multiple side effects concurrently and merging their results.
- `noOpSideEffect()`: A default side effect that does nothing.

## Unidirectional Data Flow
The components in this module work together to create a strict, easy-to-follow data flow:
1. `Intent`: The UI dispatches an Intent (e.g., SaveButtonClicked).
2. `ViewModel` (from :mvi-android): The ViewModel receives the intent.
3. `Reducer`: The ViewModel immediately passes the intent to the Reducer, which produces a new State (e.g., setting isLoading = true). The UI instantly reflects this new state.
4. `SideEffectMap`: The ViewModel looks up the SaveButtonClicked intent in the SideEffectMap and retrieves the corresponding SaveUserData SideEffect.
5. `Execution`: The ViewModel executes the SaveUserData SideEffect.
6. `SideEffect Result`: The SideEffect completes and returns a SideEffectResult, such as NewIntent(UserSaveSuccess).
7. `Loop`: The UserSaveSuccess intent is fed back into the ViewModel, which sends it to the Reducer. The Reducer updates the state accordingly (e.g., isLoading = false, showSuccessMessage = true), 
    completing the cycle.

This predictable flow makes the application easier to debug, test, and reason about across any platform.