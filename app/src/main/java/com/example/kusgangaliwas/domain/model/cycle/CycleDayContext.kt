package com.example.kusgangaliwas.domain.model.cycle

/**
 * Lightweight UI-facing cycle summary for the day screen.
 */
data class CycleDayContext(
    val trainingCycleId: Long,
    val trainingCycleName: String,

    val lastCompletedStepName: String? = null,
    val lastCompletedEpochDay: Long? = null,

    val nextStepId: Long? = null,
    val nextStepName: String? = null,

    val hasWarnBeforeMarkDone: Boolean = false,
)