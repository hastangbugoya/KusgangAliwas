package com.example.kusgangaliwas.domain.gymremote

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Foreground-only bridge for gym remote inputs.
 *
 * MainActivity emits platform key events as GymRemoteInput.
 * Active gym/session ViewModels collect the input and decide what to do.
 *
 * This keeps:
 * - MainActivity independent from SessionDetailViewModel
 * - ViewModels independent from Android KeyEvent
 * - the reducer testable and platform-free
 *
 * Later, a foreground GymRemoteService can use this same input type.
 */
@Singleton
class GymRemoteInputBus @Inject constructor() {

    private val _inputs = MutableSharedFlow<GymRemoteInput>(
        extraBufferCapacity = 16,
    )

    val inputs: SharedFlow<GymRemoteInput> = _inputs

    fun emit(input: GymRemoteInput) {
        _inputs.tryEmit(input)
    }
}