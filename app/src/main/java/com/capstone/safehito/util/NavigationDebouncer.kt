package com.capstone.safehito.util

import kotlinx.coroutines.*

class NavigationDebouncer {
    private var job: Job? = null

    fun navigate(scope: CoroutineScope, delayMillis: Long = 250, block: () -> Unit) {
        job?.cancel()
        job = scope.launch {
            delay(delayMillis)
            block()
        }
    }
}
