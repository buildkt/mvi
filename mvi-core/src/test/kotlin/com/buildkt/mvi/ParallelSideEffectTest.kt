package com.buildkt.mvi

import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParallelSideEffectTest {
    @Test
    fun `parallelSideEffect should execute all child side effects and merge NewIntent results`() =
        runTest {
            val sideEffect1 = sideEffect<TestState, TestIntent> { _, _ -> TestIntent.Result1(data = "data1") }
            val sideEffect2 = sideEffect<TestState, TestIntent> { _, _ -> TestIntent.Result2(data = "data2") }
            val parallel = parallelSideEffect(sideEffect1, sideEffect2)

            val result = parallel(TestState, intent = TestIntent.Trigger)

            assertTrue(actual = result is SideEffectResult.NewIntents, message = "Result should be NewIntents")
            val intents = result.intents.toList()

            assertEquals(expected = 2, actual = intents.size)
            assertTrue(actual = intents.contains(TestIntent.Result1("data1")))
            assertTrue(actual = intents.contains(TestIntent.Result2("data2")))
        }

    @Test
    fun `parallelSideEffect should merge results from NewIntent and NewIntents`() =
        runTest {
            val sideEffect1 = sideEffect<TestState, TestIntent> { _, _ -> TestIntent.Result1("data1") }
            val sideEffect2 =
                observableSideEffect<TestState, TestIntent> {
                    flowOf(TestIntent.ResultFromFlow(index = 0), TestIntent.ResultFromFlow(index = 1))
                }
            val parallel = parallelSideEffect(sideEffect1, sideEffect2)

            val result = parallel(TestState, TestIntent.Trigger)

            assertTrue(actual = result is SideEffectResult.NewIntents)
            val intents = result.intents.toList()

            assertEquals(expected = 3, actual = intents.size)
            assertTrue(actual = intents.contains(TestIntent.Result1(data = "data1")))
            assertTrue(actual = intents.contains(TestIntent.ResultFromFlow(index = 0)))
            assertTrue(actual = intents.contains(TestIntent.ResultFromFlow(index = 1)))
        }

    @Test
    fun `parallelSideEffect should ignore NoOp and Navigation results`() =
        runTest {
            val sideEffectWithIntent = sideEffect<TestState, TestIntent> { _, _ -> TestIntent.Result1("data1") }
            val noOp = noOpSideEffect<TestState, TestIntent>()
            val navigation = SideEffect<TestState, TestIntent> { _, _ -> SideEffectResult.Navigation("route") }
            val parallel = parallelSideEffect(sideEffectWithIntent, noOp, navigation)

            val result = parallel(TestState, intent = TestIntent.Trigger)

            assertTrue(result is SideEffectResult.NewIntents)
            val intents = result.intents.toList()

            assertEquals(expected = 1, actual = intents.size)
            assertEquals(expected = TestIntent.Result1("data1"), actual = intents.first())
        }

    @Test
    fun `parallelSideEffect should handle null and empty inputs gracefully`() =
        runTest {
            val sideEffect1 = sideEffect<TestState, TestIntent> { _, _ -> TestIntent.Result1("data1") }
            val parallelWithNulls = parallelSideEffect<TestState, TestIntent>(null, null)
            val parallelEmpty = parallelSideEffect<TestState, TestIntent>()
            val parallelMixed = parallelSideEffect<TestState, TestIntent>(sideEffect1, null)

            val resultWithNulls = parallelWithNulls(TestState, TestIntent.Trigger)
            val resultEmpty = parallelEmpty(TestState, TestIntent.Trigger)
            val resultMixed = parallelMixed(TestState, TestIntent.Trigger)

            assertTrue(resultWithNulls is SideEffectResult.NewIntents)
            assertTrue(resultEmpty is SideEffectResult.NewIntents)
            assertTrue(resultMixed is SideEffectResult.NewIntents)

            assertEquals(0, resultWithNulls.intents.toList().size)
            assertEquals(0, resultEmpty.intents.toList().size)

            val mixedIntents = resultMixed.intents.toList()
            assertEquals(1, mixedIntents.size)
            assertEquals(TestIntent.Result1("data1"), mixedIntents.first())
        }

    private object TestState

    private sealed interface TestIntent {
        data object Trigger : TestIntent

        data class Result1(
            val data: String,
        ) : TestIntent

        data class Result2(
            val data: String,
        ) : TestIntent

        data class ResultFromFlow(
            val index: Int,
        ) : TestIntent
    }
}
