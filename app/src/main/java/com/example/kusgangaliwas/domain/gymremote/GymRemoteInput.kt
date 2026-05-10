package com.example.kusgangaliwas.domain.gymremote

/**
 * Inputs coming from the gym remote control system.
 *
 * These are intentionally platform-independent:
 * - no Android KeyEvent references
 * - no Bluetooth references
 * - no UI references
 *
 * Android key events are mapped into these inputs elsewhere.
 */
sealed interface GymRemoteInput {

    /**
     * Main confirm/action button.
     *
     * Typical remote:
     * - play/pause
     */
    data object Confirm : GymRemoteInput

    /**
     * Move to next editable field or item.
     *
     * Typical remote:
     * - next track
     */
    data object Next : GymRemoteInput

    /**
     * Move to previous editable field or item.
     *
     * Typical remote:
     * - previous track
     */
    data object Previous : GymRemoteInput

    /**
     * Increase currently focused value.
     *
     * Typical remote:
     * - volume up
     */
    data object Increment : GymRemoteInput

    /**
     * Decrease currently focused value.
     *
     * Typical remote:
     * - volume down
     */
    data object Decrement : GymRemoteInput
}