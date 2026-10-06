package com.tlnacl.githubcompose.core.util

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CoroutineRunnerTest {
    @Test
    fun `single runner waits for previous work before starting next task`() = runTest {
        val runner = SingleRunner()
        val gate = CompletableDeferred<Unit>()
        val order = mutableListOf<String>()
        val first = async {
            runner.afterPrevious {
                order.add("first started")
                gate.await()
                order.add("first finished")
            }
        }
        runCurrent()
        val second = async { runner.afterPrevious { order.add("second") } }
        runCurrent()
        assertThat(order).isEqualTo(listOf("first started"))
        gate.complete(Unit)
        first.await()
        second.await()
        assertThat(order).isEqualTo(listOf("first started", "first finished", "second"))
    }

    @Test
    fun `concurrent joins share one result and next call runs fresh work`() = runTest {
        val runner = ControlledRunner<Int>()
        val gate = CompletableDeferred<Int>()
        var calls = 0
        val first = async {
            runner.joinPreviousOrRun {
                calls++
                gate.await()
            }
        }
        runCurrent()
        val second = async {
            runner.joinPreviousOrRun {
                calls++
                99
            }
        }
        runCurrent()
        assertThat(calls).isEqualTo(1)
        gate.complete(42)
        assertThat(first.await()).isEqualTo(42)
        assertThat(second.await()).isEqualTo(42)
        assertThat(
            runner.joinPreviousOrRun {
                calls++
                7
            },
        ).isEqualTo(7)
        assertThat(calls).isEqualTo(2)
    }

    @Test
    fun `replacement waits for cancelled task cleanup`() = runTest {
        val runner = ControlledRunner<Int>()
        var cleanedUp = false
        val first = async {
            runner.cancelPreviousThenRun {
                try {
                    awaitCancellation()
                } finally {
                    cleanedUp = true
                }
            }
        }
        runCurrent()
        val replacement = async {
            runner.cancelPreviousThenRun {
                assertThat(cleanedUp).isTrue()
                42
            }
        }
        assertThat(replacement.await()).isEqualTo(42)
        assertThat(first.isCancelled).isTrue()
    }

    @Test
    fun `explicit cancellation stops active task and runner remains reusable`() = runTest {
        val runner = ControlledRunner<Int>()
        val first = async { runner.joinPreviousOrRun { awaitCancellation() } }
        runCurrent()
        runner.cancelPrevious()
        assertThat(first.isCancelled).isTrue()
        assertThat(runner.joinPreviousOrRun { 42 }).isEqualTo(42)
    }

    @Test
    fun `cancelling joining caller does not cancel owner task`() = runTest {
        val runner = ControlledRunner<Int>()
        val gate = CompletableDeferred<Int>()
        val owner = async { runner.joinPreviousOrRun { gate.await() } }
        runCurrent()
        val joining = async { runner.joinPreviousOrRun { 99 } }
        runCurrent()
        joining.cancelAndJoin()
        assertThat(owner.isActive).isTrue()
        gate.complete(42)
        assertThat(owner.await()).isEqualTo(42)
    }

    @Test
    fun `failed task clears active task and allows retry`() = runTest {
        val runner = ControlledRunner<Int>()
        var failed = false
        try {
            runner.joinPreviousOrRun { error("failure") }
        } catch (e: IllegalStateException) {
            failed = true
        }
        assertThat(failed).isTrue()
        assertThat(runner.joinPreviousOrRun { 42 }).isEqualTo(42)
    }
}
