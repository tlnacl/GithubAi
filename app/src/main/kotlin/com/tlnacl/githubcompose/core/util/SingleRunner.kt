package com.tlnacl.githubcompose.core.util

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Runs each task after the previous task finishes, without blocking a thread. */
class SingleRunner {
    private val mutex = Mutex()

    suspend fun <T> afterPrevious(block: suspend () -> T): T = mutex.withLock { block() }
}
