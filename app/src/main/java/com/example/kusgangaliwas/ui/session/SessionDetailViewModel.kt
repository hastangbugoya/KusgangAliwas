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
import com.example.kusgangaliwas.domain.gymremote.GymRemoteCursor
import com.example.kusgangaliwas.domain.gymremote.GymRemoteEffect
import com.example.kusgangaliwas.domain.gymremote.GymRemoteExerciseNode
import com.example.kusgangaliwas.domain.gymremote.GymRemoteField
import com.example.kusgangaliwas.domain.gymremote.GymRemoteInput
import com.example.kusgangaliwas.domain.gymremote.GymRemoteInputBus
import com.example.kusgangaliwas.domain.gymremote.GymRemoteMediaControlBus
import com.example.kusgangaliwas.domain.gymremote.GymRemoteMediaControlCommand
import com.example.kusgangaliwas.domain.gymremote.GymRemoteSessionTree
import com.example.kusgangaliwas.domain.gymremote.GymRemoteSetNode
import com.example.kusgangaliwas.domain.gymremote.GymRemoteTreeReducer
import com.example.kusgangaliwas.domain.gymremote.GymVoiceBus
import com.example.kusgangaliwas.domain.repository.ExerciseRepository
import com.example.kusgangaliwas.domain.repository.SessionRepository
import com.example.kusgangaliwas.domain.usecase.session.AddExerciseLogToSessionUseCase
import com.example.kusgangaliwas.domain.usecase.session.CreateSplitTemplateFromActualSessionUseCase
import com.example.kusgangaliwas.domain.usecase.session.UpdateSplitTemplateFromActualSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import android.content.Context
import android.content.Intent
import com.example.kusgangaliwas.widget.GymSessionWidgetProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import com.example.kusgangaliwas.data.local.entity.ExercisePaceProfileEntity
import com.example.kusgangaliwas.domain.repository.ExercisePaceProfileRepository
import com.example.kusgangaliwas.domain.repository.SplitTemplateRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

data class SessionDetailUiState(
    val session: ActualSessionEntity? = null,
    val exerciseLogs: List<SessionExerciseLogUiState> = emptyList(),
    val cardioLogs: List<SessionCardioLogUiState> = emptyList(),
    val sessionItems: List<SessionDetailItemUiState> = emptyList(),
    val availableExercises: List<ExerciseEntity> = emptyList(),
    val titleText: String = "Session",
    val focusedExerciseLogId: Long? = null,
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
    val previousCardioText: String? = null,
    val previousDistance: Double? = null,
    val previousDistanceUnit: String? = null,
    val previousDurationSeconds: Long? = null,
    val previousIncline: Double? = null,
    val previousResistance: Double? = null,
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
    val previousMaxText: String? = null,
)

private data class ExerciseLogWithSets(
    val log: ActualExerciseLogEntity,
    val sets: List<ActualExerciseSetLogEntity>,
)

private data class PaceCueSetKey(
    val exerciseLogId: Long,
    val setLogId: Long,
)

@HiltViewModel
class SessionDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val sessionRepository: SessionRepository,
    private val exerciseRepository: ExerciseRepository,
    private val addExerciseLogToSessionUseCase: AddExerciseLogToSessionUseCase,
    private val gymRemoteInputBus: GymRemoteInputBus,
    private val gymVoiceBus: GymVoiceBus,
    private val gymRemoteMediaControlBus: GymRemoteMediaControlBus,
    private val createSplitTemplateFromActualSessionUseCase: CreateSplitTemplateFromActualSessionUseCase,
    private val updateSplitTemplateFromActualSessionUseCase: UpdateSplitTemplateFromActualSessionUseCase,
    @ApplicationContext
    private val applicationContext: Context,
    private val exercisePaceProfileRepository: ExercisePaceProfileRepository,
    private val splitTemplateRepository: SplitTemplateRepository,
) : ViewModel() {

    val actualSessionId: Long = checkNotNull(
        savedStateHandle.get<Long>("actualSessionId")
    ) {
        "Missing actualSessionId."
    }

    private val gymRemoteTreeReducer = GymRemoteTreeReducer()

    private var gymRemoteCursor: GymRemoteCursor = GymRemoteCursor.SessionRoot

    private val focusedExerciseLogId = MutableStateFlow<Long?>(null)

    private var paceCueJob: Job? = null
    private var activePaceCueSetKey: PaceCueSetKey? = null


    private enum class PaceCuePhase {
        NONE,
        PREP,
        WORK,
        REST,
        WAITING_FOR_NEXT_SET_DECISION,
    }

    private var paceCuePhase: PaceCuePhase = PaceCuePhase.NONE

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
            focusedExerciseLogId,
        ) { session, logsWithSets, cardioLogs, exercises, focusedExerciseLogId ->
            val exerciseById = exercises.associateBy { it.id }

            val exerciseItems = logsWithSets.map { item ->
                val exerciseName = item.log.exerciseId
                    ?.let { exerciseById[it]?.name }
                    ?: item.log.freeTextName
                    ?: "Loose exercise note"

                val suggestion = item.log.exerciseId
                    ?.let { exerciseId ->
                        sessionRepository.getLatestWeightSuggestionForExercise(exerciseId)
                    }

                SessionExerciseLogUiState(
                    log = item.log,
                    exerciseName = exerciseName,
                    sets = item.sets,
                    previousMaxText = suggestion?.let {
                        buildPreviousMaxText(
                            exerciseName = exerciseName,
                            weight = it.suggestedWeight,
                            reps = it.suggestedReps,
                        )
                    } ?: "No previous $exerciseName log.",
                )
            }

            val cardioItems = cardioLogs.map { log ->
                val cardioName = log.exerciseId
                    ?.let { exerciseById[it]?.name }
                    ?: log.freeTextName
                    ?: "Cardio"

                val suggestion = log.exerciseId
                    ?.let { exerciseId ->
                        sessionRepository.getLatestCardioSuggestionForExercise(exerciseId)
                    }

                SessionCardioLogUiState(
                    log = log,
                    cardioName = cardioName,
                    previousCardioText = suggestion?.let {
                        buildPreviousCardioText(
                            cardioName = cardioName,
                            distance = it.distance,
                            distanceUnit = it.distanceUnit,
                            durationSeconds = it.durationSeconds,
                            incline = it.averageInclinePercent,
                            resistance = it.averageResistance,
                        )
                    } ?: "No previous $cardioName log.",
                    previousDistance = suggestion?.distance,
                    previousDistanceUnit = suggestion?.distanceUnit,
                    previousDurationSeconds = suggestion?.durationSeconds,
                    previousIncline = suggestion?.averageInclinePercent,
                    previousResistance = suggestion?.averageResistance,
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
                titleText = buildSessionTitle(session),
                focusedExerciseLogId = focusedExerciseLogId,
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
                // Only the post-rest decision reminder should stop from any remote action.
                // Do not cancel PREP / WORK / REST, because some remotes emit extra key events.
                if (paceCuePhase == PaceCuePhase.WAITING_FOR_NEXT_SET_DECISION) {
                    activePaceCueSetKey?.let { cueKey ->
                        gymRemoteCursor = GymRemoteCursor.AddSetPrompt(
                            exerciseLogId = cueKey.exerciseLogId,
                        )
                    }
                    stopPaceCueLoop()
                }
                val tree = currentUiState.toGymRemoteSessionTree()
                val previousCursor = gymRemoteCursor

                val result = gymRemoteTreeReducer.reduce(
                    tree = tree,
                    cursor = gymRemoteCursor,
                    input = input,
                )

                gymRemoteCursor = result.nextCursor

                Log.d(
                    GYM_REMOTE_LOG_TAG,
                    "TREE BEFORE: $previousCursor",
                )
                Log.d(
                    GYM_REMOTE_LOG_TAG,
                    "TREE AFTER: ${result.nextCursor}",
                )

                val paceStartEffect = result.effects
                    .filterIsInstance<GymRemoteEffect.YieldMediaControl>()
                    .firstOrNull()

                val shouldLetPaceOwnStartSpeech =
                    paceStartEffect?.let { effect ->
                        hasPaceProfileForExerciseLog(
                            exerciseLogId = effect.exerciseLogId,
                            currentUiState = currentUiState,
                        )
                    } == true

                val spokeFromEffect =
                    result.effects.any { effect ->
                        effect is GymRemoteEffect.Speak &&
                                !shouldSuppressReducerStartSetSpeech(
                                    effect = effect,
                                    shouldLetPaceOwnStartSpeech = shouldLetPaceOwnStartSpeech,
                                )
                    } || shouldLetPaceOwnStartSpeech

                result.effects.forEach { effect ->
                    if (
                        effect is GymRemoteEffect.Speak &&
                        shouldSuppressReducerStartSetSpeech(
                            effect = effect,
                            shouldLetPaceOwnStartSpeech = shouldLetPaceOwnStartSpeech,
                        )
                    ) {
                        Log.d(
                            GYM_REMOTE_LOG_TAG,
                            "Suppressing reducer Start set. Pace cue will own it.",
                        )
                    } else {
                        applyGymRemoteEffect(
                            effect = effect,
                            currentUiState = currentUiState,
                        )
                    }
                }

                val handledFieldEdit = handleGymRemoteFieldEditIfNeeded(
                    input = input,
                    cursor = gymRemoteCursor,
                    currentUiState = currentUiState,
                )

                if (!spokeFromEffect && !handledFieldEdit) {
                    speakForCursorIfUseful(
                        cursor = gymRemoteCursor,
                        tree = tree,
                        sessionTitle = currentUiState.titleText,
                    )
                }
            }.onFailure { error ->
                error.printStackTrace()
            }
        }
    }

    private suspend fun applyGymRemoteEffect(
        effect: GymRemoteEffect,
        currentUiState: SessionDetailUiState,
    ) {
        when (effect) {
            is GymRemoteEffect.DebugLog -> {
                Log.d(GYM_REMOTE_LOG_TAG, effect.message)
            }

            is GymRemoteEffect.Speak -> {
                Log.d(GYM_REMOTE_LOG_TAG, "SPEAK: ${effect.text}")
                gymVoiceBus.speak(effect.text)
            }

            is GymRemoteEffect.SelectRemoteExercise -> {
                if (effect.exerciseLogId != focusedExerciseLogId.value) {
                    stopPaceCueLoop()
                }

                focusedExerciseLogId.value = effect.exerciseLogId
            }

            is GymRemoteEffect.AddSetToExercise -> {
                stopPaceCueLoop()

                addSetFromGymRemote(
                    actualExerciseLogId = effect.exerciseLogId,
                    currentUiState = currentUiState,
                )
            }

            is GymRemoteEffect.YieldMediaControl -> {
                Log.d(
                    GYM_REMOTE_LOG_TAG,
                    "Yielding media control for exerciseLogId=${effect.exerciseLogId}, setLogId=${effect.setLogId}",
                )

                gymRemoteMediaControlBus.emit(
                    GymRemoteMediaControlCommand.YieldToExternalMedia
                )

                startPaceCueLoop(
                    exerciseLogId = effect.exerciseLogId,
                    setLogId = effect.setLogId,
                    currentUiState = currentUiState,
                )
            }

            is GymRemoteEffect.AnnounceFocus -> {
                Log.d(
                    GYM_REMOTE_LOG_TAG,
                    "Ignoring legacy AnnounceFocus effect in tree remote mode: ${effect.focus}",
                )
            }

            is GymRemoteEffect.UpdateWeight -> {
                Log.d(
                    GYM_REMOTE_LOG_TAG,
                    "Ignoring legacy UpdateWeight effect in tree remote mode.",
                )
            }

            is GymRemoteEffect.UpdateReps -> {
                Log.d(
                    GYM_REMOTE_LOG_TAG,
                    "Ignoring legacy UpdateReps effect in tree remote mode.",
                )
            }

            is GymRemoteEffect.DuplicateSet -> {
                Log.d(
                    GYM_REMOTE_LOG_TAG,
                    "Ignoring legacy DuplicateSet effect in tree remote mode.",
                )
            }
        }
    }

    private fun stopPaceCueLoop() {
        paceCueJob?.cancel()
        paceCueJob = null
        activePaceCueSetKey = null
        paceCuePhase = PaceCuePhase.NONE
    }

    private suspend fun startPaceCueLoop(
        exerciseLogId: Long,
        setLogId: Long,
        currentUiState: SessionDetailUiState,
    ) {
        stopPaceCueLoop()

        val exercise = currentUiState.exerciseLogs
            .firstOrNull { it.log.id == exerciseLogId }
            ?: return

        val profile = resolvePaceProfileForExerciseLog(
            exercise = exercise,
            session = currentUiState.session,
        )?.takeIf { it.isEnabled }
            ?: return

        val cueKey = PaceCueSetKey(
            exerciseLogId = exerciseLogId,
            setLogId = setLogId,
        )

        activePaceCueSetKey = cueKey

        paceCueJob = viewModelScope.launch {
            val prepSeconds = profile.prepLeadSeconds.coerceAtLeast(0)
            val workSeconds = profile.expectedWorkSeconds.coerceAtLeast(0)
            val restSeconds = profile.expectedRestSeconds.coerceAtLeast(0)
            val nextSetWarningSeconds = profile.nextSetWarningSeconds.coerceAtLeast(0)
            val idleReminderSeconds = profile.idleReminderIntervalSeconds.coerceAtLeast(0)

            Log.d(
                GYM_REMOTE_LOG_TAG,
                "Pace profile=${profile.name}, prep=$prepSeconds, work=$workSeconds, rest=$restSeconds, warning=$nextSetWarningSeconds, idleReminder=$idleReminderSeconds",
            )

            if (prepSeconds > 0) {
                paceCuePhase = PaceCuePhase.PREP

                gymVoiceBus.speak(
                    "${exercise.exerciseName}. Prep ${formatSecondsForSpeech(prepSeconds)}."
                )

                delaySeconds(prepSeconds)

                if (activePaceCueSetKey != cueKey) return@launch
            }

            paceCuePhase = PaceCuePhase.WORK
            gymVoiceBus.speak("Start set.")

            if (workSeconds > 0) {
                delaySeconds(workSeconds)

                if (activePaceCueSetKey != cueKey) return@launch
            }

            if (restSeconds > 0) {
                paceCuePhase = PaceCuePhase.REST

                gymVoiceBus.speak(
                    "Set complete. Rest ${formatSecondsForSpeech(restSeconds)}."
                )

                if (
                    nextSetWarningSeconds > 0 &&
                    nextSetWarningSeconds < restSeconds
                ) {
                    delaySeconds(restSeconds - nextSetWarningSeconds)

                    if (activePaceCueSetKey != cueKey) return@launch

                    gymVoiceBus.speak(
                        "${formatSecondsForSpeech(nextSetWarningSeconds)} left."
                    )

                    delaySeconds(nextSetWarningSeconds)
                } else {
                    delaySeconds(restSeconds)
                }

                if (activePaceCueSetKey != cueKey) return@launch
            }

            if (activePaceCueSetKey != cueKey) return@launch

            gymRemoteCursor = GymRemoteCursor.AddSetPrompt(
                exerciseLogId = exerciseLogId,
            )

            paceCuePhase = PaceCuePhase.WAITING_FOR_NEXT_SET_DECISION

            val readySpeech = buildReadyForNextSetSpeech(profile)

            gymVoiceBus.speak(readySpeech)

            if (profile.idleReminderEnabled && idleReminderSeconds > 0) {
                while (activePaceCueSetKey == cueKey) {
                    delaySeconds(idleReminderSeconds)

                    if (activePaceCueSetKey == cueKey) {
                        gymVoiceBus.speak(readySpeech)
                    }
                }
            }

        }
    }

    private suspend fun hasPaceProfileForExerciseLog(
        exerciseLogId: Long,
        currentUiState: SessionDetailUiState,
    ): Boolean {
        val exercise = currentUiState.exerciseLogs
            .firstOrNull { it.log.id == exerciseLogId }
            ?: return false

        return resolvePaceProfileForExerciseLog(
            exercise = exercise,
            session = currentUiState.session,
        )?.isEnabled == true
    }

    private suspend fun resolvePaceProfileForExerciseLog(
        exercise: SessionExerciseLogUiState,
        session: ActualSessionEntity?,
    ): ExercisePaceProfileEntity? {
        val exerciseId = exercise.log.exerciseId ?: return null

        val splitProfileId = session?.splitTemplateId
            ?.let { splitTemplateId ->
                val splitExercises = splitTemplateRepository
                    .getExercisesForSplit(splitTemplateId)

                val expectedSuggestedOrder = exercise.log.logOrder - 1

                val matchedSplitExercise =
                    splitExercises.firstOrNull { splitExercise ->
                        splitExercise.exerciseId == exerciseId &&
                                splitExercise.suggestedOrder == expectedSuggestedOrder
                    } ?: splitExercises.firstOrNull { splitExercise ->
                        splitExercise.exerciseId == exerciseId
                    }

                matchedSplitExercise?.paceProfileId
            }

        val splitProfile = splitProfileId
            ?.let { profileId ->
                exercisePaceProfileRepository.getProfileById(profileId)
            }
            ?.takeIf { profile ->
                profile.exerciseId == exerciseId && profile.isEnabled
            }

        if (splitProfile != null) {
            return splitProfile
        }

        return exercisePaceProfileRepository
            .getDefaultProfileForExercise(exerciseId)
            ?.takeIf { it.isEnabled }
    }

    private fun shouldSuppressReducerStartSetSpeech(
        effect: GymRemoteEffect.Speak,
        shouldLetPaceOwnStartSpeech: Boolean,
    ): Boolean {
        return shouldLetPaceOwnStartSpeech &&
                effect.text.trim().equals(
                    other = "Start set.",
                    ignoreCase = true,
                )
    }

    private fun buildReadyForNextSetSpeech(
        profile: ExercisePaceProfileEntity,
    ): String {
        return if (profile.etiquetteReminderEnabled) {
            "Ready. Add another set, or choose another exercise. Clear the station if you are done."
        } else {
            "Ready. Add another set, or choose another exercise."
        }
    }

    private suspend fun delaySeconds(
        seconds: Int,
    ) {
        if (seconds <= 0) return

        delay(seconds * 1_000L)
    }

    private fun formatSecondsForSpeech(
        seconds: Int,
    ): String {
        val safeSeconds = seconds.coerceAtLeast(0)
        val minutes = safeSeconds / 60
        val remainingSeconds = safeSeconds % 60

        return when {
            safeSeconds == 1 -> "1 second"

            minutes == 0 -> "$safeSeconds seconds"

            remainingSeconds == 0 -> {
                if (minutes == 1) {
                    "1 minute"
                } else {
                    "$minutes minutes"
                }
            }

            minutes == 1 -> {
                "1 minute $remainingSeconds seconds"
            }

            else -> {
                "$minutes minutes $remainingSeconds seconds"
            }
        }
    }

    private suspend fun addSetFromGymRemote(
        actualExerciseLogId: Long,
        currentUiState: SessionDetailUiState,
    ) {
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

        val newSetId = sessionRepository.insertSet(
            ActualExerciseSetLogEntity(
                actualExerciseLogId = actualExerciseLogId,
                setOrder = nextOrder,
                reps = suggestion?.suggestedReps,
                weight = suggestion?.suggestedWeight,
                notes = suggestion?.let {
                    buildString {
                        append("From previous ")
                        append(
                            exerciseLog?.exerciseId
                                ?.let { exerciseId ->
                                    currentUiState.availableExercises
                                        .firstOrNull { it.id == exerciseId }
                                        ?.name
                                }
                                ?: "exercise"
                        )
                        append(" session max")

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

        gymRemoteCursor = GymRemoteCursor.SetField(
            exerciseLogId = actualExerciseLogId,
            setLogId = newSetId,
            field = GymRemoteField.WEIGHT,
        )

        focusedExerciseLogId.value = actualExerciseLogId

        val weightText = suggestion?.suggestedWeight
            ?.let { formatWeight(it) }
            ?: "no"

        gymVoiceBus.speak("Set $nextOrder. $weightText pounds.")
        refreshGymSessionWidget()
    }

    private suspend fun handleGymRemoteFieldEditIfNeeded(
        input: GymRemoteInput,
        cursor: GymRemoteCursor,
        currentUiState: SessionDetailUiState,
    ): Boolean {
        if (cursor !is GymRemoteCursor.SetField) return false

        val exercise = currentUiState.exerciseLogs
            .firstOrNull { it.log.id == cursor.exerciseLogId }
            ?: return false

        val set = exercise.sets
            .firstOrNull { it.id == cursor.setLogId }
            ?: return false

        return when (input) {
            GymRemoteInput.Increment -> {
                updateSetFieldByRemote(
                    set = set,
                    field = cursor.field,
                    direction = 1,
                )
                true
            }

            GymRemoteInput.Decrement -> {
                updateSetFieldByRemote(
                    set = set,
                    field = cursor.field,
                    direction = -1,
                )
                true
            }

            else -> {
                false
            }
        }
    }

    private suspend fun updateSetFieldByRemote(
        set: ActualExerciseSetLogEntity,
        field: GymRemoteField,
        direction: Int,
    ) {
        when (field) {
            GymRemoteField.WEIGHT -> {
                val newWeight = ((set.weight ?: 0.0) + (WEIGHT_STEP * direction))
                    .coerceAtLeast(0.0)

                sessionRepository.updateSet(
                    set.copy(
                        weight = newWeight,
                    )
                )
                refreshGymSessionWidget()
                gymVoiceBus.speak("${formatWeight(newWeight)} pounds")
            }

            GymRemoteField.REPS -> {
                val newReps = ((set.reps ?: 0) + direction)
                    .coerceAtLeast(0)

                sessionRepository.updateSet(
                    set.copy(
                        reps = newReps,
                    )
                )
                refreshGymSessionWidget()
                gymVoiceBus.speak("$newReps reps")
            }

            GymRemoteField.DURATION,
            GymRemoteField.DISTANCE,
            GymRemoteField.REST_TIMER -> {
                Log.d(
                    GYM_REMOTE_LOG_TAG,
                    "Remote edit ignored for unsupported field: $field",
                )
            }
        }
    }

    private suspend fun speakForCursorIfUseful(
        cursor: GymRemoteCursor,
        tree: GymRemoteSessionTree,
        sessionTitle: String,
    ) {
        when (cursor) {
            GymRemoteCursor.SessionRoot -> {
                gymVoiceBus.speak("$sessionTitle. Press play to go to exercises.")
            }

            is GymRemoteCursor.ExerciseList -> {
                tree.exerciseById(cursor.selectedExerciseLogId)
                    ?.let { exercise ->
                        gymVoiceBus.speak(exercise.exerciseName)
                    }
            }

            is GymRemoteCursor.SetList -> {
                val exercise = tree.exerciseById(cursor.exerciseLogId)
                    ?: return

                val set = cursor.selectedSetLogId
                    ?.let { exercise.setById(it) }
                    ?: return

                gymVoiceBus.speak("Set ${set.setOrder}.")
            }

            is GymRemoteCursor.SetField -> {
                val exercise = tree.exerciseById(cursor.exerciseLogId)
                    ?: return

                val set = exercise.setById(cursor.setLogId)
                    ?: return

                when (cursor.field) {
                    GymRemoteField.WEIGHT -> {
                        val weight = set.weight?.let { formatWeight(it) } ?: "no"
                        gymVoiceBus.speak("Set ${set.setOrder}. $weight pounds.")
                    }

                    GymRemoteField.REPS -> {
                        val reps = set.reps?.toString() ?: "no"
                        gymVoiceBus.speak("Set ${set.setOrder}. $reps reps.")
                    }

                    GymRemoteField.DURATION,
                    GymRemoteField.DISTANCE,
                    GymRemoteField.REST_TIMER -> Unit
                }
            }

            is GymRemoteCursor.AddSetPrompt -> {
                tree.exerciseById(cursor.exerciseLogId)
                    ?.let { exercise ->
                        gymVoiceBus.speak("${exercise.exerciseName}. Press play to add next set.")
                    }
            }

            is GymRemoteCursor.MusicYield -> Unit
        }
    }

    private fun SessionDetailUiState.toGymRemoteSessionTree(): GymRemoteSessionTree {
        return GymRemoteSessionTree(
            exercises = exerciseLogs.map { exercise ->
                GymRemoteExerciseNode(
                    exerciseLogId = exercise.log.id,
                    exerciseName = exercise.exerciseName,
                    logOrder = exercise.log.logOrder,
                    sets = exercise.sets
                        .sortedForGymRemote()
                        .map { set ->
                            GymRemoteSetNode(
                                setLogId = set.id,
                                setOrder = set.setOrder,
                                weight = set.weight,
                                reps = set.reps,
                            )
                        },
                )
            },
        )
    }

    fun toggleRemoteFocus(
        actualExerciseLogId: Long,
    ) {
        stopPaceCueLoop()
        focusedExerciseLogId.value =
            if (focusedExerciseLogId.value == actualExerciseLogId) {
                gymRemoteCursor = GymRemoteCursor.SessionRoot
                null
            } else {
                gymRemoteCursor = GymRemoteCursor.ExerciseList(
                    selectedExerciseLogId = actualExerciseLogId,
                )
                actualExerciseLogId
            }
    }

    fun addSet(actualExerciseLogId: Long) {
        stopPaceCueLoop()
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
                                append("From previous ")
                                append(
                                    exerciseLog?.exerciseId
                                        ?.let { exerciseId ->
                                            uiState.value.availableExercises
                                                .firstOrNull { it.id == exerciseId }
                                                ?.name
                                        }
                                        ?: "exercise"
                                )
                                append(" session max")

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

    private fun refreshGymSessionWidget() {
        applicationContext.sendBroadcast(
            Intent(applicationContext, GymSessionWidgetProvider::class.java).apply {
                action = "com.example.kusgangaliwas.widget.ACTION_REFRESH"
            }
        )
    }

    fun addCardio(
        exerciseId: Long? = null,
    ) {
        viewModelScope.launch {
            runCatching {
                val nextOrder = getNextSessionItemLogOrder()

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
                        distance = null,
                        distanceUnit = suggestion?.distanceUnit ?: "mi",
                        durationSeconds = null,
                        averageInclinePercent = null,
                        averageResistance = null,
                        notes = null,
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
                val nextOrder = getNextSessionItemLogOrder()

                val exerciseName = uiState.value.availableExercises
                    .firstOrNull { it.id == exerciseId }
                    ?.name

                sessionRepository.insertActualExerciseLog(
                    ActualExerciseLogEntity(
                        actualSessionId = actualSessionId,
                        exerciseId = exerciseId,
                        logOrder = nextOrder,
                        logType = "plannedExercise",
                        freeTextName = exerciseName,
                        notes = null,
                        performedAtEpochMillis = System.currentTimeMillis(),
                    )
                )
            }.onFailure { error ->
                error.printStackTrace()
            }
        }
    }

    private suspend fun getNextSessionItemLogOrder(): Int {
        val exerciseLogs = sessionRepository.getLogsForSession(actualSessionId)
        val cardioLogs = sessionRepository.getCardioLogsForSession(actualSessionId)

        return (
                exerciseLogs.map { it.logOrder } +
                        cardioLogs.map { it.logOrder }
                )
            .maxOrNull()
            ?.plus(1)
            ?: 1
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

    fun updateSavedSplitFromSession() {
        viewModelScope.launch {
            runCatching {
                updateSplitTemplateFromActualSessionUseCase(
                    actualSessionId = actualSessionId,
                )
            }.onFailure { error ->
                error.printStackTrace()
            }
        }
    }

    fun createSavedSplitFromSession(
        splitName: String,
        splitNotes: String? = null,
    ) {
        viewModelScope.launch {
            runCatching {
                val splitId = createSplitTemplateFromActualSessionUseCase(
                    actualSessionId = actualSessionId,
                    splitName = splitName,
                    splitNotes = splitNotes,
                )

                val session = sessionRepository.getActualSessionById(actualSessionId)
                    ?: return@runCatching

                sessionRepository.updateActualSession(
                    session.copy(
                        splitTemplateId = splitId,
                        title = splitName,
                        updatedAtEpochMillis = System.currentTimeMillis(),
                    )
                )
            }.onFailure { error ->
                error.printStackTrace()
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
        stopPaceCueLoop()
        viewModelScope.launch {
            runCatching {
                sessionRepository.deleteSet(setId)
            }.onFailure { it.printStackTrace() }
        }
    }

    fun duplicateSet(set: ActualExerciseSetLogEntity) {
        stopPaceCueLoop()
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
        stopPaceCueLoop()
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

    private fun buildPreviousCardioText(
        cardioName: String,
        distance: Double?,
        distanceUnit: String?,
        durationSeconds: Long?,
        incline: Double?,
        resistance: Double?,
    ): String {
        val parts = buildList {
            distance?.let { value ->
                distanceUnit?.let { unit ->
                    add("${formatWeight(value)} $unit")
                }
            }

            durationSeconds?.let { seconds ->
                add("${seconds / 60}m")
            }

            incline?.let { value ->
                add("${formatWeight(value)}% incline")
            }

            resistance?.let { value ->
                add("resistance ${formatWeight(value)}")
            }
        }

        return if (parts.isEmpty()) {
            "No previous $cardioName log."
        } else {
            "Previous $cardioName session: ${parts.joinToString(" • ")}"
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

    private fun List<ActualExerciseSetLogEntity>.sortedForGymRemote(): List<ActualExerciseSetLogEntity> {
        return sortedWith(
            compareBy<ActualExerciseSetLogEntity> { it.setOrder }
                .thenBy { it.id }
        )
    }

    private fun buildPreviousMaxText(
        exerciseName: String,
        weight: Double,
        reps: Int?,
    ): String {
        return buildString {
            append("Previous ")
            append(exerciseName)
            append(" max: ")
            append(formatWeight(weight))

            reps?.let {
                append(" × ")
                append(it)
            }
        }
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

    private fun buildSessionTitle(
        session: ActualSessionEntity?,
    ): String {
        if (session == null) return "Session"

        val dateText = LocalDate
            .ofEpochDay(session.performedDateEpochDay)
            .format(DateTimeFormatter.ofPattern("MMM d"))

        return "${session.title} · $dateText"
    }

    init {
        viewModelScope.launch {
            gymRemoteInputBus.inputs.collect { input ->
                Log.d(GYM_REMOTE_LOG_TAG, "Collected input: $input")
                handleGymRemoteInput(input)
            }
        }
    }

    override fun onCleared() {
        stopPaceCueLoop()
        super.onCleared()
    }
    private companion object {
        const val GYM_REMOTE_LOG_TAG = "KA_GYM_REMOTE_REAL"
        const val WEIGHT_STEP = 2.5
    }
}