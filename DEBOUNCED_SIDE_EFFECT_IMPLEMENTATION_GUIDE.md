# Implementation Guide: Debounced Side Effect (Option B) for buildkt/mvi

This guide describes how to add **Option B: cancel-previous-run debouncing** to the [buildkt/mvi](https://github.com/buildkt/mvi) library so that a side effect runs only after a period of no new intents, with each new intent cancelling the previous pending run.

**Target repo:** https://github.com/buildkt/mvi

**Behavior:**
- When an intent that triggers a debounced side effect is dispatched, cancel any existing pending run for that side effect.
- Start a new run that: waits `delayMs`, then executes the underlying side effect with the **current** state (and intent).
- Only the last run (after the user stops firing intents) completes; all previous runs are cancelled.

**Use case:** Search-as-you-type: user types "f", "fo", "foo" → only one search runs, 300ms after the last keystroke, with `state.query == "foo"`.

---

## 1. Repo structure (reference)

From the [buildkt/mvi](https://github.com/buildkt/mvi) repo:

| Module | Role |
|--------|------|
| **mvi-core** | Platform-agnostic: `Reducer`, `SideEffect`, `SideEffectResult`. Define the debounce API here. |
| **mvi-android** | ViewModel / MVI loop: runs side effects. Implement cancel-previous-run and delay here. |
| **mvi-annotation** | Annotations only. No change unless you add a new annotation (e.g. `@Debounced`). |
| **mvi-annotation-processor** | KSP: generates ViewModel and `sideEffects { }` DSL. Change only if the DSL needs to know “this effect is debounced”. |

Debouncing is **execution policy**: when to run and whether to cancel. So the main work is in **mvi-core** (contract) and **mvi-android** (execution).

---

## 2. API design

**Goal:** Keep the existing `sideEffect { }` API and add a wrapper that makes a side effect debounced.

**Option 2a – Wrapper in the same module as `sideEffect` (recommended):**

In **mvi-core**, add a function that wraps a `SideEffect` and returns a new “debounced” representation. The Android layer then interprets that representation by cancelling the previous job and running after a delay.

```kotlin
// mvi-core: signature only (platform-agnostic)
fun <S, I> debounced(
    delayMs: Long,
    sideEffect: SideEffect<S, I>
): DebouncedSideEffect<S, I>

// New type so the runtime can treat it differently
interface DebouncedSideEffect<S, I> : SideEffect<S, I> {
    val delayMs: Long
    val wrapped: SideEffect<S, I>
}
```

Or, without a new type, use a **marker + delay** so the Android runtime knows to debounce:

```kotlin
// mvi-core
data class DebouncedSideEffect<S, I>(
    val delayMs: Long,
    val wrapped: SideEffect<S, I>
) {
    suspend fun run(state: S, intent: I): SideEffectResult = wrapped(state, intent)
}
```

**Option 2b – Android-only wrapper:**

If you prefer not to touch mvi-core, you can add in **mvi-android** something like:

```kotlin
// mvi-android
fun <S, I> debounced(
    delayMs: Long,
    sideEffect: SideEffect<S, I>
): SideEffect<S, I>
```

The implementation would capture `delayMs` and the wrapped effect; the ViewModel (or wherever side effects are run) would need to detect “this came from debounced()” and apply cancel + delay. That’s harder without a dedicated type, so **Option 2a is recommended**.

**Consumer usage (in app navigation):**

```kotlin
sideEffects {
    searchQueryChanged = debounced(300L, searchInPredefinedHabits(repository = habitRepository))
}
```

So: **mvi-core** defines `debounced(delayMs, sideEffect)` and a type (e.g. `DebouncedSideEffect<S, I>`). **mvi-android** implements the execution policy (cancel previous job, delay, then run wrapped effect with current state).

---

## 3. Implementation steps

### Step 1: mvi-core – Debounced type and constructor

**Files to add or edit:** e.g. `mvi-core/src/main/kotlin/com/buildkt/mvi/DebouncedSideEffect.kt` (or under existing `sideEffect` / `SideEffect` package).

1. **Define a type** the runtime can use to apply debounce behavior:

   - Either a subtype of `SideEffect<S, I>` (if it’s an interface) or a wrapper that holds `delayMs` and the inner `SideEffect<S, I>`.
   - Ensure the wrapper can be invoked with `(state, intent)` and return whatever your `SideEffectResult` (or equivalent) is.

2. **Add a public function** `debounced`:

   ```kotlin
   fun <S, I> debounced(
       delayMs: Long,
       sideEffect: SideEffect<S, I>
   ): DebouncedSideEffect<S, I>
   ```

   Return an instance that holds `delayMs` and `sideEffect` and, when run, simply delegates to `sideEffect(state, intent)` (the “when to run” is handled in the runtime, not in core).

3. **Document** that the *execution* of this effect (delay and cancel-previous) is defined by the runtime (e.g. mvi-android).

**Check:** Compile mvi-core; existing `sideEffect { }` usages remain unchanged.

---

### Step 2: mvi-android – Detect debounced effects and run with cancel + delay

**Where side effects are executed:** This is typically in the ViewModel or a dedicated “MVI runtime” that processes intents and calls side effects. Locate the place where an intent is mapped to a side effect and that side effect is invoked (e.g. `sideEffect(state, intent)`).

1. **Detect debounced effects**
   - When the side effect for an intent is a `DebouncedSideEffect` (or your wrapper type), do not run it immediately.
   - Instead, use a **per-effect key** (e.g. the intent type or the registered name of the side effect, such as `searchQueryChanged`) to look up or create a **Job** (or `CoroutineScope`) for that effect.

2. **Cancel previous run**
   - Before starting a new run, cancel the existing Job for that key (if any).
   - Store the new Job so the next time the same debounced effect is triggered, you cancel this one.

3. **New run: delay then execute**
   - Launch a new coroutine (e.g. in the ViewModel’s `viewModelScope` or the scope used for other side effects):
     - `delay(delayMs)`
     - Then call the **wrapped** side effect with the **current** state and intent: `wrapped(state, intent)` (or current state and the intent that triggered the effect; see note below).
   - When the effect returns a result (e.g. `NewIntent`), process it as you already do for non-debounced effects (dispatch intent, navigate, etc.).
   - Hold the `Job` for this run so it can be cancelled when the same debounced effect is triggered again.

4. **State used for the run**
   - Use the **current** state at the moment the delay has elapsed (not the state at the time the intent was dispatched). That way, after 300ms of no typing, the search runs with `state.query` already set to the latest text. If your runtime passes `(state, intent)` into the effect when it runs, ensure that `state` is the latest from your state flow/holder.

**Important:** Ensure the same “effect key” is used for all invocations of the same debounced side effect (e.g. one key per `searchQueryChanged`), so that every new `SearchQueryChanged` cancels the previous pending run and starts a new 300ms timer.

**Check:** In a sample (e.g. habit quick-habits), wire `searchQueryChanged = debounced(300L, searchInPredefinedHabits(...))` and remove the in-Pane debounce. Typing quickly should result in a single search run ~300ms after the last keystroke.

---

### Step 3: Annotation processor (only if needed for DSL)

The KSP processor generates the `sideEffects { }` block and maps intent names to side effect instances. You **do not** need to change the processor if:

- `debounced(300L, searchInPredefinedHabits(...))` is valid Kotlin and returns something that is already typed as `SideEffect<S, I>` (or your existing type used in the generated code).

If the generated code expects a specific type (e.g. only `SideEffect<S, I>`), then ensure `DebouncedSideEffect<S, I>` is a subtype or is accepted where `SideEffect<S, I>` is (e.g. the generated code stores it in a map of `SideEffect<S, I>`). Then no processor change is needed.

If the processor generates code that invokes the side effect directly and you need it to call into the “runtime” that handles debounce, then the runtime must be the single place that actually runs effects (and checks for `DebouncedSideEffect`). So: keep the processor generating the same structure; let the Android runtime interpret `DebouncedSideEffect` and apply cancel + delay.

---

### Step 4: Consumer usage and sample

**Habit sample (quick-habits):**

1. **Intent:** Keep `SearchQueryChanged` as the intent that updates the query (and optionally still triggers the debounced effect from the framework). No need for a separate `TriggerSearch` if the library debounces.

2. **Side effect:** Same as today, but wrapped:

   ```kotlin
   searchQueryChanged = debounced(300L, searchInPredefinedHabits(repository = habitRepository))
   ```

3. **Pane:** Remove the `LaunchedEffect(state.query, state.isSearching)` debounce and the `TriggerSearch` intent; the UI can dispatch only `SearchQueryChanged(query)` on each keystroke. The library will debounce and run the effect once with the latest state.

4. **SideEffects.kt:** `searchInPredefinedHabits` stays a plain `sideEffect<...> { state, _ -> ... }` that reads `state.query`; no internal delay.

---

## 5. Edge cases and notes

- **Empty delay:** If `delayMs == 0L`, you can either run immediately (no debounce) or still cancel previous and run after 0ms; both are acceptable (document the choice).
- **Cancellation:** When the previous Job is cancelled, do not dispatch any result from that run (e.g. do not emit `NewIntent` from a cancelled effect). Only the run that completes after the delay should produce results.
- **Concurrent keys:** If you later add multiple debounced effects (e.g. search and “apply filters”), each should have its own key (e.g. intent type or effect name) so they don’t cancel each other.
- **Tests:** Unit-test the runtime: “when the same debounced effect is triggered twice within delayMs, only the second run executes”; “when triggered once, after delayMs the wrapped effect runs with current state.”

---

## 6. Summary checklist for an agent

- [ ] **mvi-core:** Add `DebouncedSideEffect<S, I>` (or equivalent) and `debounced(delayMs, sideEffect)`.
- [ ] **mvi-android:** Where side effects are run, detect `DebouncedSideEffect`; for that effect key, cancel previous Job, then launch a new Job that `delay(delayMs)` and runs the wrapped effect with current state; store the Job for that key.
- [ ] **Processor:** Only change if the generated type doesn’t accept `DebouncedSideEffect`; otherwise leave as-is.
- [ ] **Sample:** Switch quick-habits to use `debounced(300L, searchInPredefinedHabits(...))` and remove in-Pane debounce and `TriggerSearch` if present.
- [ ] **Docs:** Add a short “Debounced side effects” section to the repo README or docs, linking to this behavior (cancel previous run, run after delay with current state).

This gives a concrete, Option-B–style debounced side effect in [buildkt/mvi](https://github.com/buildkt/mvi) that an agent or developer can implement step by step.
