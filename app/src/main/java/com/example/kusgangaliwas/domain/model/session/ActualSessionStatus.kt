package com.example.kusgangaliwas.domain.model.session

/**
 * Strongly typed lifecycle state for actual workout sessions.
 *
 * KA intentionally treats planning and cycles as guidance systems,
 * not strict enforcement systems.
 *
 * Any completed session is considered success.
 */
enum class ActualSessionStatus {
    /**
     * Session has started but is not yet finalized.
     */
    IN_PROGRESS,

    /**
     * Session was successfully completed.
     */
    COMPLETED,

    /**
     * Session was intentionally stopped/discarded.
     */
    ABANDONED,
}