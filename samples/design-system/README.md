# Design System Module (`:core:design-system`)
This module contains the core components and visual foundations of the Fabrik design system. 
It is built on top of Material Design 3 and provides an opinionated, consistent, and reusable set of 
UI building blocks for all feature modules.

The primary goal of this module is to accelerate development and ensure visual consistency across 
the entire application by providing smart, pre-configured Composables.

## Core Concepts
### 1. Theming with `ExtendedMaterialTheme`
The foundation of the design system is `ExtendedMaterialTheme`. It's a wrapper around the standard 
`MaterialTheme` that provides additional capabilities:
*   **Automatic Dark/Light Mode:** It automatically applies the correct color scheme (`LightColorScheme` or `DarkColorScheme`) based on the system settings.
*   **Dynamic Color Support:** On Android 12+, it defaults to using `dynamicColor`, adapting the app's theme to the user's wallpaper for a more personal experience.
*   **Custom Token Injection:** It uses `CompositionLocalProvider` to inject custom design tokens, such as our spacing scale, making them accessible throughout the entire app.

**Usage:**
```kotlin
// In your Activity or root Composable 
ExtendedMaterialTheme { /* Your app content goes here */ }
```

### 2. Design Tokens
Tokens are the primitive, named values that define the visual style of the design system.

#### Color (`tokens/Color.kt`)
We define explicit `lightColorScheme` and `darkColorScheme` instances. This ensures that our brand 
colors are consistently applied and gives us a single place to update the look and feel of the entire application.

#### Spacing (`tokens/Spacers.kt`)
We use a custom `Spacers` data class to define a consistent spacing scale (e.g., `extraSmall`, `small`, `medium`, `large`). 
These tokens should be used for all padding and layout arrangements to ensure a harmonious visual rhythm.

**Usage:**
```kotlin
// Access spacers via the MaterialTheme extension property 
val spacing = MaterialTheme.spacers.medium
Spacer(modifier = Modifier.height(spacing))
```

### 3. Smart Components
The real power of the design system comes from its "smart" components. These are opinionated wrappers
around base Material 3 Composables that are pre-configured with our theme's tokens and provide a simplified, consistent API.

#### `Button`
A wrapper for `Button`, `OutlinedButton`, and `TextButton` that automatically uses theme colors and 
supports a built-in loading state.

*   **Variants:** `Primary` (filled), `Outlined` (outlined), `Text` (text).
*   **Features:** `isLoading` parameter to show a `CircularProgressIndicator` automatically.

**Usage:**
```kotlin
FabrikButton( onClick = { }, text = "Save", isLoading = state.isSaving )
```

#### `Text`
An opinionated wrapper for `Text` that enforces the use of our defined typographic styles.
*   **Variants:** `Headline`, `Title`, `Body`, `Subtle`.
*   **Features:** Provides a semantic API and automatically applies the correct `TextStyle` and default `Color` from the theme.

**Usage:**
```kotlin
FabrikText( text = "Welcome to Fabrik", style = FabrikTextStyle.Title )
```

#### `TopAppBar`
A wrapper for `TopAppBar` that is pre-configured with theme colors and automatically handles status bar icon contrast.

*   **Features:**
    *   Intelligently sets status bar icons to light or dark based on its own background color.
    *   Provides a simple `onBackClicked` overload for the most common use case.

**Usage:**
```kotlin
// In a Scaffold 
val topBar = { TopAppBar( title = "Screen Title", onBackClicked = { /* navigate back */ } ) }
```

#### `TextField`
A wrapper for `OutlinedTextField` that applies theme colors, shapes, and provides built-in support for 
displaying helper and error text.

*   **Features:**
    *   Automatically uses the theme's primary color for the focused state.
    *   Manages displaying `supportingText` or an `errorText` with correct coloring and animations.

**Usage:**
```kotlin
TextField( value = state.email, onValueChange = {  }, label = "Email", errorText = "Invalid email" )
```

## Contribution
When adding a new component to the design system, follow these principles:
1.  **Wrap, Don't Reinvent:** Create a wrapper around an existing Material 3 component.
2.  **Be Opinionated:** Pre-configure the component with our design tokens (colors, shapes, typography).
3.  **Simplify the API:** Create overloads and sensible defaults for the most common use cases (e.g., accepting a `String` for a text label).
4.  **Add Documentation:** Provide clear KDoc explaining the component's purpose and usage.
