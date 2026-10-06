package com.tlnacl.githubcompose.core.util

import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.CoroutineStart.LAZY
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.yield

/**
 * Coordinates concurrent requests for the same resource.
 *
 * The winning caller owns the task through structured concurrency. Cancelling that caller
 * cancels the shared task; cancelling a caller that only awaits it does not cancel the task.
 * Use one consistent policy per runner instance.
 */
class ControlledRunner<T> {
    private val activeTask = AtomicReference<Deferred<T>?>(null)

    /** Cancels and waits for earlier work before starting a replacement. */
    suspend fun cancelPreviousThenRun(block: suspend () -> T): T {
        activeTask.get()?.cancelAndJoin()
        return coroutineScope {
            val newTask = async(start = LAZY) { block() }
            newTask.invokeOnCompletion { activeTask.compareAndSet(newTask, null) }
            val result: T
            while (true) {
                if (!activeTask.compareAndSet(null, newTask)) {
                    activeTask.get()?.cancelAndJoin()
                    yield()
                } else {
                    result = newTask.await()
                    break
                }
            }
            result
        }
    }

    /** Shares the running task's result, or starts a task when none is active. */
    suspend fun joinPreviousOrRun(block: suspend () -> T): T {
        activeTask.get()?.let { return it.await() }
        return coroutineScope {
            val newTask = async(start = LAZY) { block() }
            newTask.invokeOnCompletion { activeTask.compareAndSet(newTask, null) }
            val result: T
            while (true) {
                if (!activeTask.compareAndSet(null, newTask)) {
                    val currentTask = activeTask.get()
                    if (currentTask != null) {
                        newTask.cancel()
                        result = currentTask.await()
                        break
                    } else {
                        yield()
                    }
                } else {
                    result = newTask.await()
                    break
                }
            }
            result
        }
    }

    /** Cancels the task observed at invocation without clearing a concurrent replacement. */
    suspend fun cancelPrevious() {
        val task = activeTask.get() ?: return
        task.cancelAndJoin()
        activeTask.compareAndSet(task, null)
    }
}
