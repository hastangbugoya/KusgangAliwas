package com.example.kusgangaliwas.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import com.example.kusgangaliwas.MainActivity
import com.example.kusgangaliwas.R
import com.example.kusgangaliwas.data.local.entity.ActualExerciseLogEntity
import com.example.kusgangaliwas.data.local.entity.ActualExerciseSetLogEntity
import com.example.kusgangaliwas.domain.repository.ExerciseRepository
import com.example.kusgangaliwas.domain.repository.SessionRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.time.LocalDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first

/**
 * Minimal classic Android widget for gym-session traversal.
 *
 * Rationale:
 * Bluetooth/media-button ownership is not reliable enough to make remote mode
 * the only hands-free path. This widget is the reliable fallback: it lets the
 * user step through the current session without opening the full app.
 *
 * Design goals for V1:
 * - very few moving parts,
 * - no Glance dependency yet,
 * - no Compose widget dependency,
 * - no polished UI requirements,
 * - reuse repository data directly,
 * - persist only a tiny cursor in SharedPreferences.
 *
 * This is intentionally not a full workout editor.
 * It is a small session cursor/controller.
 */
class GymSessionWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)

        appWidgetIds.forEach { appWidgetId ->
            updateWidgetAsync(
                context = context,
                appWidgetId = appWidgetId,
            )
        }
    }

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        super.onReceive(context, intent)

        when (intent.action) {
            ACTION_PREVIOUS,
            ACTION_CONFIRM,
            ACTION_NEXT,
            ACTION_ADD_SET,
            ACTION_REFRESH -> {
                handleWidgetActionAsync(
                    context = context,
                    action = intent.action,
                    appWidgetId = intent.getIntExtra(
                        AppWidgetManager.EXTRA_APPWIDGET_ID,
                        AppWidgetManager.INVALID_APPWIDGET_ID,
                    ),
                )
            }
        }
    }

    private fun handleWidgetActionAsync(
        context: Context,
        action: String?,
        appWidgetId: Int,
    ) {
        widgetScope.launch {
            val safeWidgetId = appWidgetId.takeIf {
                it != AppWidgetManager.INVALID_APPWIDGET_ID
            }

            val entryPoint = EntryPointAccessors.fromApplication(
                context.applicationContext,
                GymSessionWidgetEntryPoint::class.java,
            )

            val snapshot = loadTodaySessionSnapshot(
                sessionRepository = entryPoint.sessionRepository(),
                exerciseRepository = entryPoint.exerciseRepository(),
            )

            val cursorStore = WidgetCursorStore(context)

            val currentCursor = cursorStore.load()
                .reconcile(snapshot)

            val nextCursor = when (action) {
                ACTION_PREVIOUS -> currentCursor.previous(snapshot)
                ACTION_CONFIRM -> currentCursor.confirm(snapshot)
                ACTION_NEXT -> currentCursor.next(snapshot)
                ACTION_ADD_SET -> {
                    addSetForCursor(
                        sessionRepository = entryPoint.sessionRepository(),
                        snapshot = snapshot,
                        cursor = currentCursor,
                    )
                }

                else -> currentCursor
            }.reconcile(snapshot)

            cursorStore.save(nextCursor)

            withContext(Dispatchers.Main) {
                if (safeWidgetId != null) {
                    updateWidget(
                        context = context,
                        appWidgetId = safeWidgetId,
                        snapshot = snapshot,
                        cursor = nextCursor,
                    )
                } else {
                    updateAllWidgets(context)
                }
            }
        }
    }

    private fun updateWidgetAsync(
        context: Context,
        appWidgetId: Int,
    ) {
        widgetScope.launch {
            val entryPoint = EntryPointAccessors.fromApplication(
                context.applicationContext,
                GymSessionWidgetEntryPoint::class.java,
            )

            val snapshot = loadTodaySessionSnapshot(
                sessionRepository = entryPoint.sessionRepository(),
                exerciseRepository = entryPoint.exerciseRepository(),
            )

            val cursor = WidgetCursorStore(context)
                .load()
                .reconcile(snapshot)

            withContext(Dispatchers.Main) {
                updateWidget(
                    context = context,
                    appWidgetId = appWidgetId,
                    snapshot = snapshot,
                    cursor = cursor,
                )
            }
        }
    }

    private fun updateWidget(
        context: Context,
        appWidgetId: Int,
        snapshot: WidgetSessionSnapshot,
        cursor: WidgetCursor,
    ) {
        val views = RemoteViews(
            context.packageName,
            R.layout.widget_gym_session,
        )

        views.setTextViewText(
            R.id.widgetSessionTitle,
            snapshot.title,
        )

        views.setTextViewText(
            R.id.widgetPrimaryText,
            cursor.primaryText(snapshot),
        )

        views.setTextViewText(
            R.id.widgetSecondaryText,
            cursor.secondaryText(snapshot),
        )

        views.setOnClickPendingIntent(
            R.id.widgetPreviousButton,
            pendingIntentForAction(
                context = context,
                appWidgetId = appWidgetId,
                action = ACTION_PREVIOUS,
            ),
        )

        views.setOnClickPendingIntent(
            R.id.widgetConfirmButton,
            pendingIntentForAction(
                context = context,
                appWidgetId = appWidgetId,
                action = ACTION_CONFIRM,
            ),
        )

        views.setOnClickPendingIntent(
            R.id.widgetNextButton,
            pendingIntentForAction(
                context = context,
                appWidgetId = appWidgetId,
                action = ACTION_NEXT,
            ),
        )

        views.setOnClickPendingIntent(
            R.id.widgetAddSetButton,
            pendingIntentForAction(
                context = context,
                appWidgetId = appWidgetId,
                action = ACTION_ADD_SET,
            ),
        )

        views.setOnClickPendingIntent(
            R.id.widgetOpenButton,
            openAppPendingIntent(
                context = context,
                snapshot = snapshot,
            ),
        )

        AppWidgetManager.getInstance(context)
            .updateAppWidget(appWidgetId, views)
    }

    private fun updateAllWidgets(
        context: Context,
    ) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val componentName = ComponentName(
            context,
            GymSessionWidgetProvider::class.java,
        )

        appWidgetManager.getAppWidgetIds(componentName).forEach { appWidgetId ->
            updateWidgetAsync(
                context = context,
                appWidgetId = appWidgetId,
            )
        }
    }

    private suspend fun loadTodaySessionSnapshot(
        sessionRepository: SessionRepository,
        exerciseRepository: ExerciseRepository,
    ): WidgetSessionSnapshot {
        val today = LocalDate.now().toEpochDay()
        val sessions = sessionRepository.getActualSessionsBetweenDates(
            startEpochDay = today,
            endEpochDay = today + 1,
        )

        val session = sessions
            .sortedByDescending { it.id }
            .firstOrNull()
            ?: return WidgetSessionSnapshot.Empty

        val exercises = exerciseRepository.observeActiveExercises()
            .firstValue()

        val exerciseNameById = exercises.associateBy { it.id }

        val logs = sessionRepository.getLogsForSession(session.id)
            .sortedWith(
                compareBy<ActualExerciseLogEntity> { it.logOrder }
                    .thenBy { it.id }
            )

        val exerciseNodes = logs.map { log ->
            val exerciseName = log.exerciseId
                ?.let { exerciseNameById[it]?.name }
                ?: log.freeTextName
                ?: "Exercise"

            WidgetExerciseNode(
                exerciseLogId = log.id,
                exerciseName = exerciseName,
                sets = sessionRepository.getSetsForExercise(log.id)
                    .sortedWith(
                        compareBy<ActualExerciseSetLogEntity> { it.setOrder }
                            .thenBy { it.id }
                    )
                    .map { set ->
                        WidgetSetNode(
                            setLogId = set.id,
                            setOrder = set.setOrder,
                            weight = set.weight,
                            reps = set.reps,
                        )
                    },
            )
        }

        return WidgetSessionSnapshot.Ready(
            sessionId = session.id,
            title = session.title.ifBlank { "Gym session" },
            exercises = exerciseNodes,
        )
    }

    private suspend fun addSetForCursor(
        sessionRepository: SessionRepository,
        snapshot: WidgetSessionSnapshot,
        cursor: WidgetCursor,
    ): WidgetCursor {
        val exerciseLogId = cursor.exerciseLogId(snapshot)
            ?: return cursor

        val existingSets = sessionRepository.getSetsForExercise(exerciseLogId)
        val nextOrder = existingSets.size + 1

        val newSetId = sessionRepository.insertSet(
            ActualExerciseSetLogEntity(
                actualExerciseLogId = exerciseLogId,
                setOrder = nextOrder,
                notes = "Created from gym widget",
            )
        )

        return WidgetCursor.SetField(
            exerciseLogId = exerciseLogId,
            setLogId = newSetId,
            field = WidgetField.WEIGHT,
        )
    }

    private fun pendingIntentForAction(
        context: Context,
        appWidgetId: Int,
        action: String,
    ): PendingIntent {
        val intent = Intent(context, GymSessionWidgetProvider::class.java).apply {
            this.action = action
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }

        return PendingIntent.getBroadcast(
            context,
            action.hashCode() + appWidgetId,
            intent,
            pendingIntentFlags(),
        )
    }

    private fun openAppPendingIntent(
        context: Context,
        snapshot: WidgetSessionSnapshot,
    ): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)

        if (snapshot is WidgetSessionSnapshot.Ready) {
            intent.action = MainActivity.ACTION_OPEN_SESSION_DETAIL_FROM_WIDGET
            intent.putExtra(
                MainActivity.EXTRA_ACTUAL_SESSION_ID,
                snapshot.sessionId,
            )
        }

        return PendingIntent.getActivity(
            context,
            REQUEST_CODE_OPEN_APP,
            intent,
            pendingIntentFlags(),
        )
    }

    private fun pendingIntentFlags(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface GymSessionWidgetEntryPoint {
        fun sessionRepository(): SessionRepository
        fun exerciseRepository(): ExerciseRepository
    }

    private companion object {
        const val ACTION_PREVIOUS = "com.example.kusgangaliwas.widget.ACTION_PREVIOUS"
        const val ACTION_CONFIRM = "com.example.kusgangaliwas.widget.ACTION_CONFIRM"
        const val ACTION_NEXT = "com.example.kusgangaliwas.widget.ACTION_NEXT"
        const val ACTION_ADD_SET = "com.example.kusgangaliwas.widget.ACTION_ADD_SET"
        const val ACTION_REFRESH = "com.example.kusgangaliwas.widget.ACTION_REFRESH"

        const val REQUEST_CODE_OPEN_APP = 4201

        val widgetScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }
}

private sealed interface WidgetSessionSnapshot {
    val title: String

    data object Empty : WidgetSessionSnapshot {
        override val title: String = "No gym session"
    }

    data class Ready(
        val sessionId: Long,
        override val title: String,
        val exercises: List<WidgetExerciseNode>,
    ) : WidgetSessionSnapshot
}

private data class WidgetExerciseNode(
    val exerciseLogId: Long,
    val exerciseName: String,
    val sets: List<WidgetSetNode>,
)

private data class WidgetSetNode(
    val setLogId: Long,
    val setOrder: Int,
    val weight: Double?,
    val reps: Int?,
)

private enum class WidgetField {
    WEIGHT,
    REPS,
}

private sealed interface WidgetCursor {

    data object Root : WidgetCursor

    data class Exercise(
        val exerciseLogId: Long,
    ) : WidgetCursor

    data class SetField(
        val exerciseLogId: Long,
        val setLogId: Long,
        val field: WidgetField,
    ) : WidgetCursor

    fun reconcile(
        snapshot: WidgetSessionSnapshot,
    ): WidgetCursor {
        if (snapshot !is WidgetSessionSnapshot.Ready) return Root

        return when (this) {
            Root -> Root

            is Exercise -> {
                if (snapshot.exercises.any { it.exerciseLogId == exerciseLogId }) {
                    this
                } else {
                    snapshot.exercises.firstOrNull()
                        ?.let { Exercise(it.exerciseLogId) }
                        ?: Root
                }
            }

            is SetField -> {
                val exercise = snapshot.exercises
                    .firstOrNull { it.exerciseLogId == exerciseLogId }
                    ?: return snapshot.exercises.firstOrNull()
                        ?.let { Exercise(it.exerciseLogId) }
                        ?: Root

                if (exercise.sets.any { it.setLogId == setLogId }) {
                    this
                } else {
                    exercise.sets.firstOrNull()
                        ?.let {
                            SetField(
                                exerciseLogId = exercise.exerciseLogId,
                                setLogId = it.setLogId,
                                field = WidgetField.WEIGHT,
                            )
                        }
                        ?: Exercise(exercise.exerciseLogId)
                }
            }
        }
    }

    fun previous(
        snapshot: WidgetSessionSnapshot,
    ): WidgetCursor {
        if (snapshot !is WidgetSessionSnapshot.Ready) return Root

        return when (this) {
            Root -> Root

            is Exercise -> {
                val index = snapshot.exercises.indexOfFirst {
                    it.exerciseLogId == exerciseLogId
                }

                snapshot.exercises
                    .getOrNull(index - 1)
                    ?.let { Exercise(it.exerciseLogId) }
                    ?: this
            }

            is SetField -> {
                if (field == WidgetField.REPS) {
                    copy(field = WidgetField.WEIGHT)
                } else {
                    val exercise = snapshot.exercises.firstOrNull {
                        it.exerciseLogId == exerciseLogId
                    } ?: return this

                    val index = exercise.sets.indexOfFirst {
                        it.setLogId == setLogId
                    }

                    exercise.sets
                        .getOrNull(index - 1)
                        ?.let {
                            SetField(
                                exerciseLogId = exerciseLogId,
                                setLogId = it.setLogId,
                                field = WidgetField.REPS,
                            )
                        }
                        ?: Exercise(exerciseLogId)
                }
            }
        }
    }

    fun next(
        snapshot: WidgetSessionSnapshot,
    ): WidgetCursor {
        if (snapshot !is WidgetSessionSnapshot.Ready) return Root

        return when (this) {
            Root -> Root

            is Exercise -> {
                val index = snapshot.exercises.indexOfFirst {
                    it.exerciseLogId == exerciseLogId
                }

                snapshot.exercises
                    .getOrNull(index + 1)
                    ?.let { Exercise(it.exerciseLogId) }
                    ?: this
            }

            is SetField -> {
                if (field == WidgetField.WEIGHT) {
                    copy(field = WidgetField.REPS)
                } else {
                    val exercise = snapshot.exercises.firstOrNull {
                        it.exerciseLogId == exerciseLogId
                    } ?: return this

                    val index = exercise.sets.indexOfFirst {
                        it.setLogId == setLogId
                    }

                    exercise.sets
                        .getOrNull(index + 1)
                        ?.let {
                            SetField(
                                exerciseLogId = exerciseLogId,
                                setLogId = it.setLogId,
                                field = WidgetField.WEIGHT,
                            )
                        }
                        ?: this
                }
            }
        }
    }

    fun confirm(
        snapshot: WidgetSessionSnapshot,
    ): WidgetCursor {
        if (snapshot !is WidgetSessionSnapshot.Ready) return Root

        return when (this) {
            Root -> {
                snapshot.exercises.firstOrNull()
                    ?.let { Exercise(it.exerciseLogId) }
                    ?: Root
            }

            is Exercise -> {
                val exercise = snapshot.exercises.firstOrNull {
                    it.exerciseLogId == exerciseLogId
                } ?: return Root

                exercise.sets.firstOrNull()
                    ?.let {
                        SetField(
                            exerciseLogId = exercise.exerciseLogId,
                            setLogId = it.setLogId,
                            field = WidgetField.WEIGHT,
                        )
                    }
                    ?: this
            }

            is SetField -> {
                this
            }
        }
    }

    fun exerciseLogId(
        snapshot: WidgetSessionSnapshot,
    ): Long? {
        return when (this) {
            Root -> {
                if (snapshot is WidgetSessionSnapshot.Ready) {
                    snapshot.exercises.firstOrNull()?.exerciseLogId
                } else {
                    null
                }
            }

            is Exercise -> exerciseLogId

            is SetField -> exerciseLogId
        }
    }

    fun primaryText(
        snapshot: WidgetSessionSnapshot,
    ): String {
        if (snapshot !is WidgetSessionSnapshot.Ready) return "No session found for today."

        return when (this) {
            Root -> "Ready"
            is Exercise -> {
                snapshot.exercises
                    .firstOrNull { it.exerciseLogId == exerciseLogId }
                    ?.exerciseName
                    ?: "Exercise"
            }

            is SetField -> {
                val exercise = snapshot.exercises
                    .firstOrNull { it.exerciseLogId == exerciseLogId }

                exercise?.exerciseName ?: "Exercise"
            }
        }
    }

    fun secondaryText(
        snapshot: WidgetSessionSnapshot,
    ): String {
        if (snapshot !is WidgetSessionSnapshot.Ready) {
            return "Open KA to start or log a session."
        }

        return when (this) {
            Root -> "Tap ▶ to go to exercises."

            is Exercise -> {
                val exercise = snapshot.exercises
                    .firstOrNull { it.exerciseLogId == exerciseLogId }

                if (exercise == null) {
                    "Exercise unavailable."
                } else if (exercise.sets.isEmpty()) {
                    "No sets. Tap + Set."
                } else {
                    "${exercise.sets.size} set(s). Tap ▶ to enter."
                }
            }

            is SetField -> {
                val exercise = snapshot.exercises
                    .firstOrNull { it.exerciseLogId == exerciseLogId }

                val set = exercise?.sets
                    ?.firstOrNull { it.setLogId == setLogId }

                if (set == null) {
                    "Set unavailable."
                } else {
                    when (field) {
                        WidgetField.WEIGHT -> {
                            "Set ${set.setOrder}: ${set.weight?.formatWidgetNumber() ?: "no"} lb"
                        }

                        WidgetField.REPS -> {
                            "Set ${set.setOrder}: ${set.reps ?: "no"} reps"
                        }
                    }
                }
            }
        }
    }
}

private class WidgetCursorStore(
    context: Context,
) {

    private val preferences = context.getSharedPreferences(
        "gym_session_widget_cursor",
        Context.MODE_PRIVATE,
    )

    fun load(): WidgetCursor {
        return when (preferences.getString(KEY_TYPE, TYPE_ROOT)) {
            TYPE_EXERCISE -> {
                WidgetCursor.Exercise(
                    exerciseLogId = preferences.getLong(KEY_EXERCISE_LOG_ID, 0L),
                )
            }

            TYPE_SET_FIELD -> {
                WidgetCursor.SetField(
                    exerciseLogId = preferences.getLong(KEY_EXERCISE_LOG_ID, 0L),
                    setLogId = preferences.getLong(KEY_SET_LOG_ID, 0L),
                    field = preferences.getString(KEY_FIELD, WidgetField.WEIGHT.name)
                        ?.let { runCatching { WidgetField.valueOf(it) }.getOrNull() }
                        ?: WidgetField.WEIGHT,
                )
            }

            else -> WidgetCursor.Root
        }
    }

    fun save(cursor: WidgetCursor) {
        preferences.edit().apply {
            when (cursor) {
                WidgetCursor.Root -> {
                    putString(KEY_TYPE, TYPE_ROOT)
                    remove(KEY_EXERCISE_LOG_ID)
                    remove(KEY_SET_LOG_ID)
                    remove(KEY_FIELD)
                }

                is WidgetCursor.Exercise -> {
                    putString(KEY_TYPE, TYPE_EXERCISE)
                    putLong(KEY_EXERCISE_LOG_ID, cursor.exerciseLogId)
                    remove(KEY_SET_LOG_ID)
                    remove(KEY_FIELD)
                }

                is WidgetCursor.SetField -> {
                    putString(KEY_TYPE, TYPE_SET_FIELD)
                    putLong(KEY_EXERCISE_LOG_ID, cursor.exerciseLogId)
                    putLong(KEY_SET_LOG_ID, cursor.setLogId)
                    putString(KEY_FIELD, cursor.field.name)
                }
            }
        }.apply()
    }

    private companion object {
        const val KEY_TYPE = "type"
        const val KEY_EXERCISE_LOG_ID = "exercise_log_id"
        const val KEY_SET_LOG_ID = "set_log_id"
        const val KEY_FIELD = "field"

        const val TYPE_ROOT = "root"
        const val TYPE_EXERCISE = "exercise"
        const val TYPE_SET_FIELD = "set_field"
    }
}

private suspend fun <T> kotlinx.coroutines.flow.Flow<List<T>>.firstValue(): List<T> {
    return first()
}

private fun Double.formatWidgetNumber(): String {
    return if (this % 1.0 == 0.0) {
        toInt().toString()
    } else {
        toString()
    }
}