package com.example.kusgangaliwas.ui.session

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kusgangaliwas.data.local.entity.ActualCardioLogEntity
import com.example.kusgangaliwas.data.local.entity.ActualExerciseLogEntity
import com.example.kusgangaliwas.data.local.entity.ActualExerciseSetLogEntity
import com.example.kusgangaliwas.data.local.entity.ActualSessionEntity
import com.example.kusgangaliwas.data.local.entity.ExerciseEntity
import com.example.kusgangaliwas.domain.gymremote.GymRemoteEffect
import com.example.kusgangaliwas.domain.gymremote.GymRemoteFocus
import com.example.kusgangaliwas.domain.gymremote.GymRemoteInput
import com.example.kusgangaliwas.domain.gymremote.GymRemoteInputBus
import com.example.kusgangaliwas.domain.gymremote.GymRemoteReducer
import com.example.kusgangaliwas.domain.gymremote.GymRemoteSetState
import com.example.kusgangaliwas.domain.gymremote.GymRemoteState
import com.example.kusgangaliwas.domain.gymremote.GymVoiceBus
import com.example.kusgangaliwas.domain.gymremote.debugLabel
import com.example.kusgangaliwas.domain.repository.ExerciseRepository
import com.example.kusgangaliwas.domain.repository.SessionRepository
import com.example.kusgangaliwas.domain.usecase.session.AddExerciseLogToSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SessionDetailUiState(
    val session: ActualSessionEntity? = null,
    val exerciseLogs: List<SessionExerciseLogUiState> = emptyList(),
    val cardioLogs: List<SessionCardioLogUiState> = emptyList(),
    val sessionItems: List<SessionDetailItemUiState> = emptyList(),
    val availableExercises: List<ExerciseEntity> = emptyList(),
)

sealed interface SessionDetailItemUiState {
    val logOrder: Int

    data class Strength(
        val item: SessionExerciseLogUiState,
    ) : SessionDetailItemUiState {
        override val logOrder: Int = item.log.logOrder
    }

    data class Cardio(
        val item: SessionCardioLogUiState,
    ) : SessionDetailItemUiState {
        override val logOrder: Int = item.log.logOrder
    }
}

data class SessionCardioLogUiState(
    val log: ActualCardioLogEntity,
    val cardioName: String,
)

data class SessionExerciseLogUiState(
    val log: ActualExerciseLogEntity,
    val exerciseName: String,
    val sets: List<ActualExerciseSetLogEntity> = emptyList(),

    /**
     * Suggested starting weight derived from previous exercise history.
     *
     * V1:
     * - latest session
     * - maximum logged weight from that session
     *
     * Null means:
     * - no prior history
     * - or no logged weights yet
     */
    val suggestedWeight: Double? = null,
)

private data class ExerciseLogWithSets(
    val log: ActualExerciseLogEntity,
    val sets: List<ActualExerciseSetLogEntity>,
)

@HiltViewModel
class SessionDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val sessionRepository: SessionRepository,
    exerciseRepository: ExerciseRepository,
    private val addExerciseLogToSessionUseCase: AddExerciseLogToSessionUseCase,
    private val gymRemoteInputBus: GymRemoteInputBus,
    private val gymVoiceBus: GymVoiceBus,
) : ViewModel() {

    private val actualSessionId: Long = checkNotNull(
        savedStateHandle.get<Long>("actualSessionId")
    ) {
        "Missing actualSessionId."
    }

    private val gymRemoteReducer = GymRemoteReducer()

    private var gymRemoteFocus: GymRemoteFocus = GymRemoteFocus.None

    private val exerciseLogsWithSets =
        sessionRepository.observeLogsForSession(actualSessionId)
            .flatMapLatest { logs ->
                if (logs.isEmpty()) {
                    flowOf(emptyList())
                } else {
                    combine(
                        logs.map { log ->
                            sessionRepository.observeSetsForExercise(log.id)
                                .map { sets ->
                                    ExerciseLogWithSets(
                                        log = log,
                                        sets = sets,
                                    )
                                }
                        }
                    ) { items ->
                        items.toList()
                    }
                }
            }

    val uiState: StateFlow<SessionDetailUiState> =
        combine(
            sessionRepository.observeActualSessionById(actualSessionId),
            exerciseLogsWithSets,
            sessionRepository.observeCardioLogsForSession(actualSessionId),
            exerciseRepository.observeActiveExercises(),
        ) { session, logsWithSets, cardioLogs, exercises ->
            val exerciseById = exercises.associateBy { it.id }

            val exerciseItems = logsWithSets.map { item ->
                SessionExerciseLogUiState(
                    log = item.log,
                    exerciseName = item.log.exerciseId
                        ?.let { exerciseById[it]?.name }
                        ?: item.log.freeTextName
                        ?: "Loose exercise note",
                    sets = item.sets,
                )
            }

            val cardioItems = cardioLogs.map { log ->
                SessionCardioLogUiState(
                    log = log,
                    cardioName = log.exerciseId
                        ?.let { exerciseById[it]?.name }
                        ?: log.freeTextName
                        ?: "Cardio",
                )
            }

            SessionDetailUiState(
                session = session,
                exerciseLogs = exerciseItems,
                cardioLogs = cardioItems,
                sessionItems = buildMixedSessionItems(
                    exerciseLogs = exerciseItems,
                    cardioLogs = cardioItems,
                ),
                availableExercises = exercises,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SessionDetailUiState(),
        )

    fun handleGymRemoteInput(
        input: GymRemoteInput,
    ) {
        viewModelScope.launch {
            runCatching {
                val currentUiState = uiState.value
                val activeExercise = currentUiState.exerciseLogs
                    .firstOrNull { it.sets.isNotEmpty() }

                if (activeExercise == null) {
                    Log.d(GYM_REMOTE_LOG_TAG, "No exercise with sets available.")
                    return@runCatching
                }

                val sortedSets = activeExercise.sets.sortedForGymRemote()

                val currentState = GymRemoteState(
                    exerciseName = activeExercise.exerciseName,
                    focus = gymRemoteFocus,
                    sets = sortedSets.mapIndexed { index, set ->
                        GymRemoteSetState(
                            setIndex = index,
                            weight = set.weight,
                            reps = set.reps,
                        )
                    },
                )

                val result = gymRemoteReducer.reduce(
                    state = currentState,
                    input = input,
                )

                gymRemoteFocus = result.nextState.focus

                Log.d(
                    GYM_REMOTE_LOG_TAG,
                    "REAL BEFORE: ${currentState.debugLabel()}",
                )
                Log.d(
                    GYM_REMOTE_LOG_TAG,
                    "REAL AFTER: ${result.nextState.debugLabel()}",
                )

                var workingSets = sortedSets

                result.effects.forEach { effect ->
                    workingSets = applyGymRemoteEffect(
                        effect = effect,
                        activeExerciseLogId = activeExercise.log.id,
                        sortedSets = workingSets,
                    )
                }
            }.onFailure { error ->
                error.printStackTrace()
            }
        }
    }

    private suspend fun applyGymRemoteEffect(
        effect: GymRemoteEffect,
        activeExerciseLogId: Long,
        sortedSets: List<ActualExerciseSetLogEntity>,
    ): List<ActualExerciseSetLogEntity> {
        return when (effect) {
            is GymRemoteEffect.DebugLog -> {
                Log.d(GYM_REMOTE_LOG_TAG, effect.message)
                sortedSets
            }

            is GymRemoteEffect.Speak -> {
                Log.d(GYM_REMOTE_LOG_TAG, "SPEAK: ${effect.text}")
                gymVoiceBus.speak(effect.text)
                sortedSets
            }

            is GymRemoteEffect.AnnounceFocus -> {
                val message = buildFocusSpeech(
                    focus = effect.focus,
                    sortedSets = sortedSets,
                )

                Log.d(GYM_REMOTE_LOG_TAG, "ANNOUNCE FOCUS: $message")
                gymVoiceBus.speak(message)
                sortedSets
            }

            is GymRemoteEffect.UpdateWeight -> {
                val set = sortedSets.getOrNull(effect.setIndex)
                    ?: return sortedSets

                val updatedSet = set.copy(
                    weight = effect.weight,
                )

                sessionRepository.updateSet(updatedSet)

                sortedSets
                    .replaceSetAt(
                        index = effect.setIndex,
                        set = updatedSet,
                    )
                    .sortedForGymRemote()
            }

            is GymRemoteEffect.UpdateReps -> {
                val set = sortedSets.getOrNull(effect.setIndex)
                    ?: return sortedSets

                val updatedSet = set.copy(
                    reps = effect.reps,
                )

                sessionRepository.updateSet(updatedSet)

                sortedSets
                    .replaceSetAt(
                        index = effect.setIndex,
                        set = updatedSet,
                    )
                    .sortedForGymRemote()
            }

            is GymRemoteEffect.DuplicateSet -> {
                val sourceSet = sortedSets.getOrNull(effect.sourceSetIndex)
                    ?: return sortedSets

                val existingSets = sessionRepository.getSetsForExercise(activeExerciseLogId)
                val nextOrder = existingSets.size + 1

                val newSet = sourceSet.copy(
                    id = 0L,
                    actualExerciseLogId = activeExerciseLogId,
                    setOrder = nextOrder,
                    notes = "Auto-created by gym remote",
                )

                sessionRepository.insertSet(newSet)

                sortedSets
                    .plus(
                        newSet.copy(
                            id = -nextOrder.toLong(),
                        )
                    )
                    .sortedForGymRemote()
            }
        }
    }

    fun addSet(actualExerciseLogId: Long) {
        viewModelScope.launch {
            runCatching {
                val existingSets = sessionRepository.getSetsForExercise(actualExerciseLogId)
                val nextOrder = existingSets.size + 1

                val exerciseLog = sessionRepository
                    .getLogsForSession(actualSessionId)
                    .firstOrNull { it.id == actualExerciseLogId }

                val suggestion =
                    if (nextOrder == 1) {
                        exerciseLog?.exerciseId
                            ?.let { exerciseId ->
                                sessionRepository
                                    .getLatestWeightSuggestionForExercise(exerciseId)
                            }
                    } else {
                        null
                    }

                sessionRepository.insertSet(
                    ActualExerciseSetLogEntity(
                        actualExerciseLogId = actualExerciseLogId,
                        setOrder = nextOrder,
                        reps = suggestion?.suggestedReps,
                        weight = suggestion?.suggestedWeight,
                        notes = suggestion?.let {
                            buildString {
                                append("From previous session max")

                                it.suggestedWeight.let { weight ->
                                    append(" (")
                                    append(formatWeight(weight))

                                    it.suggestedReps?.let { reps ->
                                        append(" × ")
                                        append(reps)
                                    }

                                    append(")")
                                }
                            }
                        },
                    )
                )
            }.onFailure { error ->
                error.printStackTrace()
            }
        }
    }

    fun addCardio(
        exerciseId: Long? = null,
    ) {
        viewModelScope.launch {
            runCatching {
                val exerciseLogs = sessionRepository.getLogsForSession(actualSessionId)
                val cardioLogs = sessionRepository.getCardioLogsForSession(actualSessionId)

                val nextOrder = (
                        exerciseLogs.map { it.logOrder } +
                                cardioLogs.map { it.logOrder }
                        )
                    .maxOrNull()
                    ?.plus(1)
                    ?: 1

                val suggestion =
                    exerciseId?.let {
                        sessionRepository
                            .getLatestCardioSuggestionForExercise(it)
                    }

                val exerciseName = uiState.value.availableExercises
                    .firstOrNull { it.id == exerciseId }
                    ?.name

                val now = System.currentTimeMillis()

                sessionRepository.insertCardioLog(
                    ActualCardioLogEntity(
                        actualSessionId = actualSessionId,
                        exerciseId = exerciseId,
                        logOrder = nextOrder,
                        logType = "steadyState",
                        freeTextName = exerciseName ?: "Cardio",
                        distance = suggestion?.distance,
                        distanceUnit = suggestion?.distanceUnit ?: "mi",
                        durationSeconds = suggestion?.durationSeconds,
                        averageInclinePercent = suggestion?.averageInclinePercent,
                        averageResistance = suggestion?.averageResistance,
                        notes = suggestion?.let {
                            "From previous cardio session"
                        },
                        createdAtEpochMillis = now,
                        updatedAtEpochMillis = now,
                    )
                )
            }.onFailure { error ->
                error.printStackTrace()
            }
        }
    }

    fun addExercise(exerciseId: Long) {
        viewModelScope.launch {
            runCatching {
                addExerciseLogToSessionUseCase(
                    actualSessionId = actualSessionId,
                    exerciseId = exerciseId,
                )
            }.onFailure { error ->
                error.printStackTrace()
            }
        }
    }

    fun updateCardioLog(
        cardioLog: ActualCardioLogEntity,
    ) {
        viewModelScope.launch {
            runCatching {
                sessionRepository.updateCardioLog(cardioLog)
            }.onFailure { error ->
                error.printStackTrace()
            }
        }
    }

    fun deleteCardioLog(
        cardioLogId: Long,
    ) {
        viewModelScope.launch {
            runCatching {
                sessionRepository.deleteCardioLog(cardioLogId)
            }.onFailure { error ->
                error.printStackTrace()
            }
        }
    }

    fun moveSessionItemUp(
        item: SessionDetailItemUiState,
    ) {
        moveSessionItem(
            item = item,
            direction = -1,
        )
    }

    fun moveSessionItemDown(
        item: SessionDetailItemUiState,
    ) {
        moveSessionItem(
            item = item,
            direction = 1,
        )
    }

    private fun moveSessionItem(
        item: SessionDetailItemUiState,
        direction: Int,
    ) {
        viewModelScope.launch {
            runCatching {
                val items = uiState.value.sessionItems

                val currentIndex = items.indexOfFirst { candidate ->
                    candidate.itemIdentity() == item.itemIdentity()
                }

                if (currentIndex == -1) {
                    return@runCatching
                }

                val targetIndex = currentIndex + direction

                if (targetIndex !in items.indices) {
                    return@runCatching
                }

                val currentItem = items[currentIndex]
                val targetItem = items[targetIndex]

                updateSessionItemLogOrder(
                    item = currentItem,
                    newLogOrder = targetItem.logOrder,
                )

                updateSessionItemLogOrder(
                    item = targetItem,
                    newLogOrder = currentItem.logOrder,
                )
            }.onFailure { error ->
                error.printStackTrace()
            }
        }
    }

    private suspend fun updateSessionItemLogOrder(
        item: SessionDetailItemUiState,
        newLogOrder: Int,
    ) {
        when (item) {
            is SessionDetailItemUiState.Strength -> {
                sessionRepository.updateActualExerciseLog(
                    item.item.log.copy(
                        logOrder = newLogOrder,
                    )
                )
            }

            is SessionDetailItemUiState.Cardio -> {
                sessionRepository.updateCardioLog(
                    item.item.log.copy(
                        logOrder = newLogOrder,
                        updatedAtEpochMillis = System.currentTimeMillis(),
                    )
                )
            }
        }
    }

    private fun SessionDetailItemUiState.itemIdentity(): String {
        return when (this) {
            is SessionDetailItemUiState.Strength -> {
                "strength:${item.log.id}"
            }

            is SessionDetailItemUiState.Cardio -> {
                "cardio:${item.log.id}"
            }
        }
    }

    fun updateSet(
        set: ActualExerciseSetLogEntity,
        weight: Double?,
        reps: Int?,
    ) {
        viewModelScope.launch {
            runCatching {
                sessionRepository.updateSet(
                    set.copy(
                        weight = weight,
                        reps = reps,
                    )
                )
            }.onFailure { error ->
                error.printStackTrace()
            }
        }
    }

    fun deleteSet(setId: Long) {
        viewModelScope.launch {
            runCatching {
                sessionRepository.deleteSet(setId)
            }.onFailure { it.printStackTrace() }
        }
    }

    fun duplicateSet(set: ActualExerciseSetLogEntity) {
        viewModelScope.launch {
            runCatching {
                val existing = sessionRepository.getSetsForExercise(set.actualExerciseLogId)
                val nextOrder = existing.size + 1

                sessionRepository.insertSet(
                    set.copy(
                        id = 0L,
                        setOrder = nextOrder,
                    )
                )
            }.onFailure { it.printStackTrace() }
        }
    }

    fun updateSessionRating(rating: Int?) {
        viewModelScope.launch {
            runCatching {
                val session = sessionRepository.getActualSessionById(actualSessionId)
                    ?: return@runCatching

                sessionRepository.updateActualSession(
                    session.copy(
                        rating = rating,
                        updatedAtEpochMillis = System.currentTimeMillis(),
                    )
                )
            }.onFailure { error ->
                error.printStackTrace()
            }
        }
    }

    fun deleteExerciseLogIfEmpty(
        actualExerciseLogId: Long,
    ) {
        viewModelScope.launch {
            runCatching {
                val sets = sessionRepository.getSetsForExercise(actualExerciseLogId)

                if (sets.isEmpty()) {
                    sessionRepository.deleteActualExerciseLog(actualExerciseLogId)
                }
            }.onFailure { error ->
                error.printStackTrace()
            }
        }
    }

    fun deleteSession() {
        viewModelScope.launch {
            runCatching {
                sessionRepository.deleteActualSession(actualSessionId)
            }.onFailure { error ->
                error.printStackTrace()
            }
        }
    }

    private fun buildMixedSessionItems(
        exerciseLogs: List<SessionExerciseLogUiState>,
        cardioLogs: List<SessionCardioLogUiState>,
    ): List<SessionDetailItemUiState> {
        return buildList {
            exerciseLogs.forEach { item ->
                add(SessionDetailItemUiState.Strength(item))
            }

            cardioLogs.forEach { item ->
                add(SessionDetailItemUiState.Cardio(item))
            }
        }.sortedWith(
            compareBy<SessionDetailItemUiState> { it.logOrder }
                .thenBy { item ->
                    when (item) {
                        is SessionDetailItemUiState.Strength -> 0
                        is SessionDetailItemUiState.Cardio -> 1
                    }
                }
                .thenBy { item ->
                    when (item) {
                        is SessionDetailItemUiState.Strength -> item.item.log.id
                        is SessionDetailItemUiState.Cardio -> item.item.log.id
                    }
                }
        )
    }

    private fun buildFocusSpeech(
        focus: GymRemoteFocus,
        sortedSets: List<ActualExerciseSetLogEntity>,
    ): String {
        return when (focus) {
            GymRemoteFocus.None -> {
                "No set selected."
            }

            is GymRemoteFocus.Weight -> {
                val set = sortedSets.getOrNull(focus.setIndex)
                val weight = set?.weight?.let { formatWeight(it) } ?: "no"
                "Set ${focus.setIndex + 1}, $weight pounds."
            }

            is GymRemoteFocus.Reps -> {
                val set = sortedSets.getOrNull(focus.setIndex)
                val reps = set?.reps?.toString() ?: "no"
                "Set ${focus.setIndex + 1}, $reps reps."
            }
        }
    }

    private fun List<ActualExerciseSetLogEntity>.replaceSetAt(
        index: Int,
        set: ActualExerciseSetLogEntity,
    ): List<ActualExerciseSetLogEntity> {
        return mapIndexed { itemIndex, item ->
            if (itemIndex == index) {
                set
            } else {
                item
            }
        }
    }

    private fun List<ActualExerciseSetLogEntity>.sortedForGymRemote(): List<ActualExerciseSetLogEntity> {
        return sortedWith(
            compareBy<ActualExerciseSetLogEntity> { it.setOrder }
                .thenBy { it.id }
        )
    }

    private fun formatWeight(
        value: Double,
    ): String {
        return if (value % 1.0 == 0.0) {
            value.toInt().toString()
        } else {
            value.toString()
        }
    }

    init {
        viewModelScope.launch {
            gymRemoteInputBus.inputs.collect { input ->
                Log.d(GYM_REMOTE_LOG_TAG, "Collected input: $input")
                handleGymRemoteInput(input)
            }
        }
    }

    private companion object {
        const val GYM_REMOTE_LOG_TAG = "KA_GYM_REMOTE_REAL"
    }
}