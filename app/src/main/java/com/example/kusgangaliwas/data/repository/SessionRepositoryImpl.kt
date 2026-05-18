package com.example.kusgangaliwas.data.repository

import com.example.kusgangaliwas.data.local.dao.ActualCardioLogDao
import com.example.kusgangaliwas.data.local.dao.ActualExerciseLogDao
import com.example.kusgangaliwas.data.local.dao.ActualExerciseSetLogDao
import com.example.kusgangaliwas.data.local.dao.ActualSessionDao
import com.example.kusgangaliwas.data.local.dao.PlannedSessionDao
import com.example.kusgangaliwas.data.local.dao.PlannedSessionExerciseDao
import com.example.kusgangaliwas.data.local.entity.ActualCardioLogEntity
import com.example.kusgangaliwas.data.local.entity.ActualExerciseLogEntity
import com.example.kusgangaliwas.data.local.entity.ActualExerciseSetLogEntity
import com.example.kusgangaliwas.data.local.entity.ActualSessionEntity
import com.example.kusgangaliwas.data.local.entity.PlannedSessionEntity
import com.example.kusgangaliwas.data.local.entity.PlannedSessionExerciseEntity
import com.example.kusgangaliwas.data.local.model.CardioSuggestion
import com.example.kusgangaliwas.data.local.model.ExerciseWeightSuggestion
import com.example.kusgangaliwas.domain.model.session.ActualSessionStatus
import com.example.kusgangaliwas.domain.repository.SessionRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

/**
 * Room-backed implementation of SessionRepository.
 *
 * Combines planned + actual session data, exercise/set logs, and cardio logs.
 */
class SessionRepositoryImpl @Inject constructor(
    private val plannedSessionDao: PlannedSessionDao,
    private val plannedSessionExerciseDao: PlannedSessionExerciseDao,
    private val actualSessionDao: ActualSessionDao,
    private val actualExerciseLogDao: ActualExerciseLogDao,
    private val actualExerciseSetLogDao: ActualExerciseSetLogDao,
    private val actualCardioLogDao: ActualCardioLogDao,
) : SessionRepository {

    override fun observeSessionsBetweenDates(
        startEpochDay: Long,
        endEpochDay: Long,
    ): Flow<List<PlannedSessionEntity>> {
        return plannedSessionDao.observeSessionsBetweenDates(startEpochDay, endEpochDay)
    }

    override fun observeSessionsForDate(
        epochDay: Long,
    ): Flow<List<PlannedSessionEntity>> {
        return plannedSessionDao.observeSessionsForDate(epochDay)
    }

    override suspend fun getPlannedSessionById(
        plannedSessionId: Long,
    ): PlannedSessionEntity? {
        return plannedSessionDao.getPlannedSessionById(plannedSessionId)
    }

    override suspend fun insertPlannedSession(entity: PlannedSessionEntity): Long {
        return plannedSessionDao.insertPlannedSession(entity)
    }

    override suspend fun updatePlannedSession(entity: PlannedSessionEntity) {
        plannedSessionDao.updatePlannedSession(entity)
    }

    override suspend fun updatePlannedSessionStatus(
        plannedSessionId: Long,
        status: String,
        updatedAtEpochMillis: Long,
    ) {
        plannedSessionDao.updateStatus(
            plannedSessionId = plannedSessionId,
            status = status,
            updatedAtEpochMillis = updatedAtEpochMillis,
        )
    }

    override suspend fun deletePlannedSession(plannedSessionId: Long) {
        plannedSessionDao.deletePlannedSession(plannedSessionId)
    }

    override fun observeExercisesForPlannedSession(
        plannedSessionId: Long,
    ): Flow<List<PlannedSessionExerciseEntity>> {
        return plannedSessionExerciseDao.observeExercisesForPlannedSession(plannedSessionId)
    }

    override suspend fun getExercisesForPlannedSession(
        plannedSessionId: Long,
    ): List<PlannedSessionExerciseEntity> {
        return plannedSessionExerciseDao.getExercisesForPlannedSession(plannedSessionId)
    }

    override suspend fun getCarryForwardCandidates(): List<PlannedSessionExerciseEntity> {
        return plannedSessionExerciseDao.getCarryForwardCandidates()
    }

    override suspend fun insertPlannedSessionExercise(
        entity: PlannedSessionExerciseEntity,
    ): Long {
        return plannedSessionExerciseDao.insertPlannedSessionExercise(entity)
    }

    override suspend fun insertPlannedSessionExercises(
        entities: List<PlannedSessionExerciseEntity>,
    ) {
        plannedSessionExerciseDao.insertPlannedSessionExercises(entities)
    }

    override suspend fun updatePlannedSessionExercise(
        entity: PlannedSessionExerciseEntity,
    ) {
        plannedSessionExerciseDao.updatePlannedSessionExercise(entity)
    }

    override suspend fun updatePlannedSessionExercises(
        entities: List<PlannedSessionExerciseEntity>,
    ) {
        plannedSessionExerciseDao.updatePlannedSessionExercises(entities)
    }

    override suspend fun updatePlannedSessionExerciseStatus(
        plannedSessionExerciseId: Long,
        status: String,
    ) {
        plannedSessionExerciseDao.updateStatus(
            plannedSessionExerciseId = plannedSessionExerciseId,
            status = status,
        )
    }

    override suspend fun deletePlannedSessionExercise(
        plannedSessionExerciseId: Long,
    ) {
        plannedSessionExerciseDao.deletePlannedSessionExercise(plannedSessionExerciseId)
    }

    override suspend fun deleteAllPlannedExercisesForSession(
        plannedSessionId: Long,
    ) {
        plannedSessionExerciseDao.deleteAllForPlannedSession(plannedSessionId)
    }

    override fun observeAllActualSessions(): Flow<List<ActualSessionEntity>> {
        return actualSessionDao.observeAllSessions()
    }

    override fun observeActualSessionsBetweenDates(
        startEpochDay: Long,
        endEpochDay: Long,
    ): Flow<List<ActualSessionEntity>> {
        return actualSessionDao.observeSessionsBetweenDates(startEpochDay, endEpochDay)
    }

    override suspend fun getActualSessionsBetweenDates(
        startEpochDay: Long,
        endEpochDay: Long,
    ): List<ActualSessionEntity> {
        return actualSessionDao.getSessionsBetweenDates(
            startEpochDay = startEpochDay,
            endEpochDay = endEpochDay,
        )
    }

    override fun observeActualSessionById(
        actualSessionId: Long,
    ): Flow<ActualSessionEntity?> {
        return actualSessionDao.observeById(actualSessionId)
    }

    override suspend fun getActualSessionById(
        actualSessionId: Long,
    ): ActualSessionEntity? {
        return actualSessionDao.getById(actualSessionId)
    }

    override suspend fun getLatestActualForPlannedSession(
        plannedSessionId: Long,
    ): ActualSessionEntity? {
        return actualSessionDao.getLatestForPlannedSession(plannedSessionId)
    }

    override suspend fun insertActualSession(entity: ActualSessionEntity): Long {
        return actualSessionDao.insertActualSession(entity)
    }

    override suspend fun updateActualSession(entity: ActualSessionEntity) {
        actualSessionDao.updateActualSession(entity)
    }

    override suspend fun updateActualSessionStatus(
        actualSessionId: Long,
        status: ActualSessionStatus,
        updatedAtEpochMillis: Long,
    ) {
        actualSessionDao.updateStatus(
            actualSessionId = actualSessionId,
            status = status,
            updatedAtEpochMillis = updatedAtEpochMillis,
        )
    }

    override suspend fun deleteActualSession(actualSessionId: Long) {
        actualSessionDao.deleteActualSession(actualSessionId)
    }

    // ----------------------------
    // Training Cycle Progress
    // ----------------------------

    override suspend fun getLatestCompletedCycleSession(
        trainingCycleId: Long,
    ): ActualSessionEntity? {
        return actualSessionDao.getLatestCompletedCycleSession(
            trainingCycleId = trainingCycleId,
        )
    }

    override suspend fun getCompletedCycleSessions(
        trainingCycleId: Long,
    ): List<ActualSessionEntity> {
        return actualSessionDao.getCompletedCycleSessions(
            trainingCycleId = trainingCycleId,
        )
    }

    override suspend fun getCompletedSessionsForCycleStep(
        trainingCycleId: Long,
        trainingCycleStepId: Long,
    ): List<ActualSessionEntity> {
        return actualSessionDao.getCompletedSessionsForCycleStep(
            trainingCycleId = trainingCycleId,
            trainingCycleStepId = trainingCycleStepId,
        )
    }

    override fun observeLogsForSession(
        actualSessionId: Long,
    ): Flow<List<ActualExerciseLogEntity>> {
        return actualExerciseLogDao.observeLogsForSession(actualSessionId)
    }

    override suspend fun getLogsForSession(
        actualSessionId: Long,
    ): List<ActualExerciseLogEntity> {
        return actualExerciseLogDao.getLogsForSession(actualSessionId)
    }

    override suspend fun getLogsForExercise(
        exerciseId: Long,
    ): List<ActualExerciseLogEntity> {
        return actualExerciseLogDao.getLogsForExercise(exerciseId)
    }

    override suspend fun insertActualExerciseLog(
        entity: ActualExerciseLogEntity,
    ): Long {
        return actualExerciseLogDao.insertLog(entity)
    }

    override suspend fun insertActualExerciseLogs(
        entities: List<ActualExerciseLogEntity>,
    ) {
        actualExerciseLogDao.insertLogs(entities)
    }

    override suspend fun updateActualExerciseLog(
        entity: ActualExerciseLogEntity,
    ) {
        actualExerciseLogDao.updateLog(entity)
    }

    override suspend fun updateActualExerciseLogs(
        entities: List<ActualExerciseLogEntity>,
    ) {
        actualExerciseLogDao.updateLogs(entities)
    }

    override suspend fun deleteActualExerciseLog(actualExerciseLogId: Long) {
        actualExerciseLogDao.deleteLog(actualExerciseLogId)
    }

    override suspend fun deleteAllLogsForSession(actualSessionId: Long) {
        actualExerciseLogDao.deleteAllForSession(actualSessionId)
    }

    override fun observeCardioLogsForSession(
        actualSessionId: Long,
    ): Flow<List<ActualCardioLogEntity>> {
        return actualCardioLogDao.observeCardioLogsForSession(actualSessionId)
    }

    override suspend fun getCardioLogsForSession(
        actualSessionId: Long,
    ): List<ActualCardioLogEntity> {
        return actualCardioLogDao.getCardioLogsForSession(actualSessionId)
    }

    override suspend fun insertCardioLog(entity: ActualCardioLogEntity): Long {
        return actualCardioLogDao.insertCardioLog(entity)
    }

    override suspend fun insertCardioLogs(entities: List<ActualCardioLogEntity>) {
        actualCardioLogDao.insertCardioLogs(entities)
    }

    override suspend fun updateCardioLog(entity: ActualCardioLogEntity) {
        actualCardioLogDao.updateCardioLog(entity)
    }

    override suspend fun updateCardioLogs(entities: List<ActualCardioLogEntity>) {
        actualCardioLogDao.updateCardioLogs(entities)
    }

    override suspend fun deleteCardioLog(cardioLogId: Long) {
        actualCardioLogDao.deleteCardioLog(cardioLogId)
    }

    override suspend fun deleteAllCardioLogsForSession(actualSessionId: Long) {
        actualCardioLogDao.deleteAllCardioLogsForSession(actualSessionId)
    }

    override fun observeSetsForExercise(
        actualExerciseLogId: Long,
    ): Flow<List<ActualExerciseSetLogEntity>> {
        return actualExerciseSetLogDao.observeSetsForExercise(actualExerciseLogId)
    }

    override suspend fun getSetsForExercise(
        actualExerciseLogId: Long,
    ): List<ActualExerciseSetLogEntity> {
        return actualExerciseSetLogDao.getSetsForExercise(actualExerciseLogId)
    }

    override suspend fun insertSet(entity: ActualExerciseSetLogEntity): Long {
        return actualExerciseSetLogDao.insertSet(entity)
    }

    override suspend fun insertSets(entities: List<ActualExerciseSetLogEntity>) {
        actualExerciseSetLogDao.insertSets(entities)
    }

    override suspend fun updateSet(entity: ActualExerciseSetLogEntity) {
        actualExerciseSetLogDao.updateSet(entity)
    }

    override suspend fun updateSets(entities: List<ActualExerciseSetLogEntity>) {
        actualExerciseSetLogDao.updateSets(entities)
    }

    override suspend fun deleteSet(setId: Long) {
        actualExerciseSetLogDao.deleteSet(setId)
    }

    override suspend fun deleteAllSetsForExercise(actualExerciseLogId: Long) {
        actualExerciseSetLogDao.deleteAllForExercise(actualExerciseLogId)
    }

    override suspend fun getLatestWeightSuggestionForExercise(
        exerciseId: Long,
    ): ExerciseWeightSuggestion? {
        val logs = actualExerciseLogDao.getLogsForExercise(exerciseId)

        logs.forEach { log ->
            val sets = actualExerciseSetLogDao.getSetsForExercise(log.id)

            val suggestedSet = sets
                .filter { it.weight != null }
                .maxWithOrNull(
                    compareBy<ActualExerciseSetLogEntity> { it.weight ?: 0.0 }
                        .thenBy { it.reps ?: 0 }
                        .thenBy { it.setOrder }
                )

            if (suggestedSet != null) {
                val session = actualSessionDao.getById(log.actualSessionId)
                    ?: return@forEach

                return ExerciseWeightSuggestion(
                    exerciseId = exerciseId,
                    exerciseName = null,
                    sourceActualSessionId = session.id,
                    sourcePerformedDateEpochDay = session.performedDateEpochDay,
                    suggestedWeight = suggestedSet.weight ?: return@forEach,
                    suggestedReps = suggestedSet.reps,
                )
            }
        }

        return null
    }

    override suspend fun getLatestCardioSuggestionForExercise(
        exerciseId: Long,
    ): CardioSuggestion? {

        val cardioLogs = actualCardioLogDao
            .getLogsForExercise(exerciseId)

        val latestLog = cardioLogs.firstOrNull()
            ?: return null

        val session = actualSessionDao.getById(latestLog.actualSessionId)
            ?: return null

        return CardioSuggestion(
            exerciseId = exerciseId,
            exerciseName = null,
            sourceActualSessionId = session.id,
            sourcePerformedDateEpochDay = session.performedDateEpochDay,
            distance = latestLog.distance,
            distanceUnit = latestLog.distanceUnit,
            durationSeconds = latestLog.durationSeconds,
            averageInclinePercent = latestLog.averageInclinePercent,
            averageResistance = latestLog.averageResistance,
        )
    }
}