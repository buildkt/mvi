# MVI Annotation Processor
This module contains the KSP (Kotlin Symbol Processing) implementation that serves as the engine for 
the BuildKt MVI framework. Its primary responsibility is to eliminate boilerplate by generating the necessary ViewModel 
and navigation components based on a set of simple annotations.This processor is not meant to be used directly by consumers of the library. 

Instead, it is added to the ksp configuration in a developer's build.gradle.kts file, where it will automatically run during the build process.

## Core Responsibilities
The processor scans the source code for the `@MviScreen` annotation. When it finds a `@Composable` function annotated with it, 
it performs the following actions:
1. Parses Metadata: It extracts all necessary information from the `@MviScreen` annotation and the function's parameters, including:
   - The UiState class.
   - The Intent sealed interface.
   - The NavArgument parameters.
   - Intents marked with `@TriggersSideEffect`. 
2. Generates a ViewModel: It creates a dedicated ViewModel class (e.g., ProfilePaneViewModel) that inherits from the base 
   `com.buildkt.mvi.android.ViewModel`. This generated ViewModel is responsible for orchestrating the MVI loop: 
   managing state, processing intents, and launching side effects. 
3. Generates a ViewModel Factory: To support dependency injection of nav arguments, it creates a custom ViewModelProvider.Factory 
   (e.g., ProfilePaneViewModelFactory). This factory knows how to instantiate the ViewModel and pass the required arguments from the NavBackStackEntry.
4. Generates a NavGraphBuilder Extension Function: It creates a developer-friendly extension function for NavGraphBuilder 
   (e.g., profilePane(...)). This function simplifies adding the screen to a NavHost and provides a DSL for configuring its reducer 
   and sideEffects.

## How It Works: A Technical Overview
The processing logic is implemented in the MviScreenProcessor class, which follows these steps:
1. Symbol Resolution: It uses KSP's Resolver to find all symbols annotated with @MviScreen.
2. Validation: For each annotated symbol, it runs a series of validations to prevent common errors at compile time. It checks for things like:
   - Is the annotated symbol actually a @Composable function?
   - Does the specified UiState class have a default constructor?
   - Is the Intent parameter a sealed interface?
   - Are all @NavArgument-annotated parameters valid and serializable?
3. Model Generation: If validation passes, it constructs an internal data model (MviScreenModel) that represents the screen and all its properties in a structured way.
4. Code Generation with KotlinPoet: It uses the excellent KotlinPoet library to generate the source code for the new files. Using the MviScreenModel, it builds the following files:
   - <ScreenName>ViewModel.kt
   - <ScreenName>NavGraph.kt
5. File Output: The generated Kotlin files are placed in the build/generated/ksp/ directory, where they are automatically 
   compiled along with the rest of the project's source code.

## Triggering Annotations
This processor is activated by the annotations found in the mvi-annotation module:
| Annotation | Purpose | 
| :--- | :--- | 
| @MviScreen | (Primary Trigger) Marks a @Composable function as a screen that requires MVI boilerplate generation. | 
| @TriggersSideEffect | Informs the processor that a specific Intent should be handled by a side effect, which influences the generated sideEffects DSL. | 
| @NavArgument | Tells the processor that a function parameter should be treated as a navigation argument, which must be extracted from the NavBackStackEntry and passed to the ViewModel. |

This processor is the key differentiator for the BuildKt MVI framework, transforming a traditionally boilerplate-heavy pattern into a clean, declarative, and highly productive developer experience.