package com.example.kusgangaliwas.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.kusgangaliwas.data.local.dao.ActualCardioLogDao
import com.example.kusgangaliwas.data.local.dao.ActualExerciseLogDao
import com.example.kusgangaliwas.data.local.dao.ActualExerciseSetLogDao
import com.example.kusgangaliwas.data.local.dao.ActualSessionDao
import com.example.kusgangaliwas.data.local.dao.CycleCalendarAnchorDao
import com.example.kusgangaliwas.data.local.dao.ExerciseDao
import com.example.kusgangaliwas.data.local.dao.ExerciseMotivationalGoalDao
import com.example.kusgangaliwas.data.local.dao.ExerciseMuscleGroupDao
import com.example.kusgangaliwas.data.local.dao.ExercisePaceProfileDao
import com.example.kusgangaliwas.data.local.dao.ExercisePrDao
import com.example.kusgangaliwas.data.local.dao.ExerciseSubstitutionDao
import com.example.kusgangaliwas.data.local.dao.MuscleGroupDao
import com.example.kusgangaliwas.data.local.dao.PlannedSessionDao
import com.example.kusgangaliwas.data.local.dao.PlannedSessionExerciseDao
import com.example.kusgangaliwas.data.local.dao.ProgramDao
import com.example.kusgangaliwas.data.local.dao.SplitScheduleDao
import com.example.kusgangaliwas.data.local.dao.SplitTemplateDao
import com.example.kusgangaliwas.data.local.dao.SplitTemplateExerciseDao
import com.example.kusgangaliwas.data.local.dao.SplitTemplateMuscleGroupDao
import com.example.kusgangaliwas.data.local.dao.TrainingCycleActivationDao
import com.example.kusgangaliwas.data.local.dao.TrainingCycleDao
import com.example.kusgangaliwas.data.local.dao.TrainingCycleProgressEventDao
import com.example.kusgangaliwas.data.local.dao.TrainingCycleStepDao
import com.example.kusgangaliwas.data.local.db.DatabaseSeedCallback
import com.example.kusgangaliwas.data.local.db.KusgangAliwasDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Provides the Room database and DAO dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                    ALTER TABLE split_template_exercise
                    ADD COLUMN targetSets INTEGER
                    """.trimIndent()
            )

            db.execSQL(
                """
                    ALTER TABLE split_template_exercise
                    ADD COLUMN targetRepsMin INTEGER
                    """.trimIndent()
            )

            db.execSQL(
                """
                    ALTER TABLE split_template_exercise
                    ADD COLUMN targetRepsMax INTEGER
                    """.trimIndent()
            )
        }
    }

    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {

            // Exercise reference URL
            db.execSQL(
                """
                    ALTER TABLE exercise
                    ADD COLUMN referenceUrl TEXT
                    """.trimIndent()
            )

            // Session novelty rating
            db.execSQL(
                """
                    ALTER TABLE actual_session
                    ADD COLUMN rating INTEGER
                    """.trimIndent()
            )
        }
    }

    private val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                    CREATE TABLE IF NOT EXISTS program (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        notes TEXT NOT NULL,
                        startEpochDay INTEGER,
                        horizonWeeks INTEGER NOT NULL,
                        isActive INTEGER NOT NULL,
                        createdAtEpochMillis INTEGER NOT NULL,
                        updatedAtEpochMillis INTEGER NOT NULL
                    )
                    """.trimIndent()
            )

            db.execSQL(
                """
                    CREATE TABLE IF NOT EXISTS split_schedule (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        programId INTEGER,
                        splitTemplateId INTEGER NOT NULL,
                        title TEXT NOT NULL,
                        startEpochDay INTEGER NOT NULL,
                        horizonWeeks INTEGER NOT NULL,
                        scheduleMode TEXT NOT NULL,
                        daysOfWeekMask INTEGER NOT NULL,
                        cycleOrder INTEGER NOT NULL,
                        restDaysAfter INTEGER NOT NULL,
                        isActive INTEGER NOT NULL,
                        createdAtEpochMillis INTEGER NOT NULL,
                        updatedAtEpochMillis INTEGER NOT NULL
                    )
                    """.trimIndent()
            )

            db.execSQL(
                """
                    CREATE INDEX IF NOT EXISTS index_split_schedule_programId
                    ON split_schedule(programId)
                    """.trimIndent()
            )

            db.execSQL(
                """
                    CREATE INDEX IF NOT EXISTS index_split_schedule_splitTemplateId
                    ON split_schedule(splitTemplateId)
                    """.trimIndent()
            )
        }
    }

    private val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {

            db.execSQL(
                """
                    CREATE TABLE IF NOT EXISTS actual_cardio_log (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        actualSessionId INTEGER NOT NULL,
                        exerciseId INTEGER,
                        logOrder INTEGER NOT NULL,
                        logType TEXT NOT NULL,
                        freeTextName TEXT,
                        distance REAL,
                        distanceUnit TEXT,
                        durationSeconds INTEGER,
                        averageInclinePercent REAL,
                        averageResistance REAL,
                        notes TEXT,
                        performedAtEpochMillis INTEGER,
                        createdAtEpochMillis INTEGER NOT NULL,
                        updatedAtEpochMillis INTEGER NOT NULL,
                        FOREIGN KEY(actualSessionId)
                            REFERENCES actual_session(id)
                            ON DELETE CASCADE,
                        FOREIGN KEY(exerciseId)
                            REFERENCES exercise(id)
                            ON DELETE SET NULL
                    )
                    """.trimIndent()
            )

            db.execSQL(
                """
                    CREATE INDEX IF NOT EXISTS index_actual_cardio_log_actualSessionId
                    ON actual_cardio_log(actualSessionId)
                    """.trimIndent()
            )

            db.execSQL(
                """
                    CREATE INDEX IF NOT EXISTS index_actual_cardio_log_exerciseId
                    ON actual_cardio_log(exerciseId)
                    """.trimIndent()
            )

            db.execSQL(
                """
                    CREATE INDEX IF NOT EXISTS index_actual_cardio_log_logOrder
                    ON actual_cardio_log(logOrder)
                    """.trimIndent()
            )
        }
    }

    private val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                    ALTER TABLE exercise
                    ADD COLUMN exerciseType TEXT NOT NULL DEFAULT 'STRENGTH'
                    """.trimIndent()
            )
        }
    }

    private val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(db: SupportSQLiteDatabase) {

            db.execSQL(
                """
                    ALTER TABLE actual_cardio_log
                    ADD COLUMN isEstimatedDistance INTEGER NOT NULL DEFAULT 0
                    """.trimIndent()
            )

            db.execSQL(
                """
                    ALTER TABLE actual_cardio_log
                    ADD COLUMN intensityLevel INTEGER
                    """.trimIndent()
            )
        }
    }

    private val MIGRATION_7_8 = object : Migration(7, 8) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                    CREATE TABLE IF NOT EXISTS exercise_pr (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        exerciseId INTEGER NOT NULL,
                        prType TEXT NOT NULL,
                        value REAL NOT NULL,
                        secondaryValue REAL,
                        achievedAtEpochMillis INTEGER NOT NULL,
                        sourceSetLogId INTEGER,
                        notes TEXT,
                        createdAtEpochMillis INTEGER NOT NULL,
                        updatedAtEpochMillis INTEGER NOT NULL,
                        FOREIGN KEY(exerciseId)
                            REFERENCES exercise(id)
                            ON DELETE CASCADE
                    )
                    """.trimIndent()
            )

            db.execSQL(
                """
                    CREATE INDEX IF NOT EXISTS index_exercise_pr_exerciseId
                    ON exercise_pr(exerciseId)
                    """.trimIndent()
            )

            db.execSQL(
                """
                    CREATE UNIQUE INDEX IF NOT EXISTS index_exercise_pr_exerciseId_prType
                    ON exercise_pr(exerciseId, prType)
                    """.trimIndent()
            )
        }
    }

    private val MIGRATION_8_9 = object : Migration(8, 9) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                    CREATE TABLE IF NOT EXISTS split_template_muscle_group (
                        splitTemplateId INTEGER NOT NULL,
                        muscleGroupId INTEGER NOT NULL,
                        PRIMARY KEY(splitTemplateId, muscleGroupId),
                        FOREIGN KEY(splitTemplateId)
                            REFERENCES split_template(id)
                            ON DELETE CASCADE,
                        FOREIGN KEY(muscleGroupId)
                            REFERENCES muscle_group(id)
                            ON DELETE CASCADE
                    )
                    """.trimIndent()
            )

            db.execSQL(
                """
                    CREATE INDEX IF NOT EXISTS index_split_template_muscle_group_splitTemplateId
                    ON split_template_muscle_group(splitTemplateId)
                    """.trimIndent()
            )

            db.execSQL(
                """
                    CREATE INDEX IF NOT EXISTS index_split_template_muscle_group_muscleGroupId
                    ON split_template_muscle_group(muscleGroupId)
                    """.trimIndent()
            )

            db.execSQL(
                """
                    ALTER TABLE split_template_exercise
                    ADD COLUMN targetDistance REAL
                    """.trimIndent()
            )

            db.execSQL(
                """
                    ALTER TABLE split_template_exercise
                    ADD COLUMN targetDistanceUnit TEXT
                    """.trimIndent()
            )

            db.execSQL(
                """
                    ALTER TABLE split_template_exercise
                    ADD COLUMN targetDurationMinutes INTEGER
                    """.trimIndent()
            )
        }
    }

    private val MIGRATION_9_10 = object : Migration(9, 10) {
        override fun migrate(db: SupportSQLiteDatabase) {

            // -----------------------------------------------------------------
            // training_cycle_step simplification
            // -----------------------------------------------------------------

            db.execSQL(
                """
                    CREATE TABLE IF NOT EXISTS training_cycle_step_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        cycleId INTEGER NOT NULL,
                        stepOrder INTEGER NOT NULL,
                        splitTemplateId INTEGER NOT NULL,
                        warnBeforeMarkDone INTEGER NOT NULL DEFAULT 0,
                        notes TEXT,
                        FOREIGN KEY(cycleId)
                            REFERENCES training_cycle(id)
                            ON DELETE CASCADE,
                        FOREIGN KEY(splitTemplateId)
                            REFERENCES split_template(id)
                            ON DELETE CASCADE
                    )
                    """.trimIndent()
            )

            db.execSQL(
                """
                    CREATE INDEX IF NOT EXISTS index_training_cycle_step_new_cycleId
                    ON training_cycle_step_new(cycleId)
                    """.trimIndent()
            )

            db.execSQL(
                """
                    CREATE INDEX IF NOT EXISTS index_training_cycle_step_new_splitTemplateId
                    ON training_cycle_step_new(splitTemplateId)
                    """.trimIndent()
            )

            db.execSQL(
                """
                    CREATE UNIQUE INDEX IF NOT EXISTS
                    index_training_cycle_step_new_cycleId_stepOrder
                    ON training_cycle_step_new(cycleId, stepOrder)
                    """.trimIndent()
            )

            db.execSQL(
                """
                    CREATE UNIQUE INDEX IF NOT EXISTS
                    index_training_cycle_step_new_cycleId_splitTemplateId
                    ON training_cycle_step_new(cycleId, splitTemplateId)
                    """.trimIndent()
            )

            // Keep only rows that actually reference splits.
            db.execSQL(
                """
                    INSERT INTO training_cycle_step_new (
                        id,
                        cycleId,
                        stepOrder,
                        splitTemplateId,
                        warnBeforeMarkDone,
                        notes
                    )
                    SELECT
                        id,
                        cycleId,
                        stepOrder,
                        splitTemplateId,
                        0,
                        notes
                    FROM training_cycle_step
                    WHERE splitTemplateId IS NOT NULL
                    """.trimIndent()
            )

            db.execSQL(
                """
                    DROP TABLE training_cycle_step
                    """.trimIndent()
            )

            db.execSQL(
                """
                    ALTER TABLE training_cycle_step_new
                    RENAME TO training_cycle_step
                    """.trimIndent()
            )

            // -----------------------------------------------------------------
            // actual_session cycle linkage
            // -----------------------------------------------------------------

            db.execSQL(
                """
                    CREATE TABLE IF NOT EXISTS actual_session_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        plannedSessionId INTEGER,
                        performedDateEpochDay INTEGER NOT NULL,
                        splitTemplateId INTEGER,
                        trainingCycleId INTEGER,
                        trainingCycleStepId INTEGER,
                        trainingCycleStepOrderSnapshot INTEGER,
                        title TEXT NOT NULL,
                        status TEXT NOT NULL,
                        startedAtEpochMillis INTEGER,
                        completedAtEpochMillis INTEGER,
                        notes TEXT,
                        rating INTEGER,
                        createdAtEpochMillis INTEGER NOT NULL,
                        updatedAtEpochMillis INTEGER NOT NULL,
                        FOREIGN KEY(plannedSessionId)
                            REFERENCES planned_session(id)
                            ON DELETE SET NULL,
                        FOREIGN KEY(splitTemplateId)
                            REFERENCES split_template(id)
                            ON DELETE SET NULL,
                        FOREIGN KEY(trainingCycleId)
                            REFERENCES training_cycle(id)
                            ON DELETE SET NULL
                    )
                    """.trimIndent()
            )

            db.execSQL(
                """
                    INSERT INTO actual_session_new (
                        id,
                        plannedSessionId,
                        performedDateEpochDay,
                        splitTemplateId,
                        trainingCycleId,
                        trainingCycleStepId,
                        trainingCycleStepOrderSnapshot,
                        title,
                        status,
                        startedAtEpochMillis,
                        completedAtEpochMillis,
                        notes,
                        rating,
                        createdAtEpochMillis,
                        updatedAtEpochMillis
                    )
                    SELECT
                        id,
                        plannedSessionId,
                        performedDateEpochDay,
                        splitTemplateId,
                        NULL,
                        NULL,
                        NULL,
                        title,
                        status,
                        startedAtEpochMillis,
                        completedAtEpochMillis,
                        notes,
                        rating,
                        createdAtEpochMillis,
                        updatedAtEpochMillis
                    FROM actual_session
                    """.trimIndent()
            )

            db.execSQL("DROP TABLE actual_session")

            db.execSQL(
                """
                    ALTER TABLE actual_session_new
                    RENAME TO actual_session
                    """.trimIndent()
            )

            db.execSQL(
                """
                    CREATE INDEX IF NOT EXISTS index_actual_session_plannedSessionId
                    ON actual_session(plannedSessionId)
                    """.trimIndent()
            )

            db.execSQL(
                """
                    CREATE INDEX IF NOT EXISTS index_actual_session_performedDateEpochDay
                    ON actual_session(performedDateEpochDay)
                    """.trimIndent()
            )

            db.execSQL(
                """
                    CREATE INDEX IF NOT EXISTS index_actual_session_splitTemplateId
                    ON actual_session(splitTemplateId)
                    """.trimIndent()
            )

            db.execSQL(
                """
                    CREATE INDEX IF NOT EXISTS index_actual_session_trainingCycleId
                    ON actual_session(trainingCycleId)
                    """.trimIndent()
            )

            db.execSQL(
                """
                    CREATE INDEX IF NOT EXISTS index_actual_session_trainingCycleStepId
                    ON actual_session(trainingCycleStepId)
                    """.trimIndent()
            )

            db.execSQL(
                """
                    CREATE INDEX IF NOT EXISTS index_actual_session_trainingCycleId_performedDateEpochDay
                    ON actual_session(trainingCycleId, performedDateEpochDay)
                    """.trimIndent()
            )
        }
    }

    private val MIGRATION_10_11 = object : Migration(10, 11) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                    CREATE TABLE IF NOT EXISTS training_cycle_progress_event (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        trainingCycleId INTEGER NOT NULL,
                        trainingCycleStepId INTEGER NOT NULL,
                        eventType TEXT NOT NULL,
                        eventDateEpochDay INTEGER NOT NULL,
                        notes TEXT,
                        createdAtEpochMillis INTEGER NOT NULL,
                        FOREIGN KEY(trainingCycleId)
                            REFERENCES training_cycle(id)
                            ON DELETE CASCADE,
                        FOREIGN KEY(trainingCycleStepId)
                            REFERENCES training_cycle_step(id)
                            ON DELETE CASCADE
                    )
                    """.trimIndent()
            )

            db.execSQL(
                """
                    CREATE INDEX IF NOT EXISTS index_training_cycle_progress_event_trainingCycleId
                    ON training_cycle_progress_event(trainingCycleId)
                    """.trimIndent()
            )

            db.execSQL(
                """
                    CREATE INDEX IF NOT EXISTS index_training_cycle_progress_event_trainingCycleStepId
                    ON training_cycle_progress_event(trainingCycleStepId)
                    """.trimIndent()
            )

            db.execSQL(
                """
                    CREATE INDEX IF NOT EXISTS index_training_cycle_progress_event_trainingCycleId_eventDateEpochDay
                    ON training_cycle_progress_event(trainingCycleId, eventDateEpochDay)
                    """.trimIndent()
            )
        }
    }

    private val MIGRATION_11_12 = object : Migration(11, 12) {
        override fun migrate(db: SupportSQLiteDatabase) {

            db.execSQL(
                """
                    CREATE TABLE IF NOT EXISTS training_cycle_activation (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        cycleId INTEGER NOT NULL,
                        activatedDateEpochDay INTEGER NOT NULL,
                        deactivatedDateEpochDay INTEGER,
                        notes TEXT,
                        createdAtEpochMillis INTEGER NOT NULL,
                        updatedAtEpochMillis INTEGER NOT NULL,
                        FOREIGN KEY(cycleId)
                            REFERENCES training_cycle(id)
                            ON DELETE CASCADE
                    )
                    """.trimIndent()
            )

            db.execSQL(
                """
                    CREATE INDEX IF NOT EXISTS
                    index_training_cycle_activation_cycleId
                    ON training_cycle_activation(cycleId)
                    """.trimIndent()
            )

            db.execSQL(
                """
                    CREATE INDEX IF NOT EXISTS
                    index_training_cycle_activation_cycleId_activatedDateEpochDay
                    ON training_cycle_activation(
                        cycleId,
                        activatedDateEpochDay
                    )
                    """.trimIndent()
            )

            db.execSQL(
                """
                    CREATE INDEX IF NOT EXISTS
                    index_training_cycle_activation_cycleId_deactivatedDateEpochDay
                    ON training_cycle_activation(
                        cycleId,
                        deactivatedDateEpochDay
                    )
                    """.trimIndent()
            )
        }
    }

    private val MIGRATION_12_13 = object : Migration(12, 13) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                    CREATE TABLE IF NOT EXISTS exercise_pace_profile (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        exerciseId INTEGER NOT NULL,
                        name TEXT NOT NULL,
                        isDefault INTEGER NOT NULL,
                        isEnabled INTEGER NOT NULL,
                        prepLeadSeconds INTEGER NOT NULL,
                        expectedWorkSeconds INTEGER NOT NULL,
                        expectedRestSeconds INTEGER NOT NULL,
                        nextSetWarningSeconds INTEGER NOT NULL,
                        idleReminderIntervalSeconds INTEGER NOT NULL,
                        idleReminderEnabled INTEGER NOT NULL,
                        etiquetteReminderEnabled INTEGER NOT NULL,
                        createdAtEpochMillis INTEGER NOT NULL,
                        updatedAtEpochMillis INTEGER NOT NULL,
                        FOREIGN KEY(exerciseId)
                            REFERENCES exercise(id)
                            ON DELETE CASCADE
                    )
                    """.trimIndent()
            )

            db.execSQL(
                """
                    CREATE INDEX IF NOT EXISTS index_exercise_pace_profile_exerciseId
                    ON exercise_pace_profile(exerciseId)
                    """.trimIndent()
            )

            db.execSQL(
                """
                    CREATE UNIQUE INDEX IF NOT EXISTS index_exercise_pace_profile_exerciseId_name
                    ON exercise_pace_profile(exerciseId, name)
                    """.trimIndent()
            )

            db.execSQL(
                """
                    CREATE TABLE IF NOT EXISTS split_template_exercise_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        splitTemplateId INTEGER NOT NULL,
                        exerciseId INTEGER NOT NULL,
                        paceProfileId INTEGER,
                        suggestedOrder INTEGER NOT NULL,
                        notes TEXT,
                        targetSets INTEGER,
                        targetRepsMin INTEGER,
                        targetRepsMax INTEGER,
                        targetDistance REAL,
                        targetDistanceUnit TEXT,
                        targetDurationMinutes INTEGER,
                        isOptional INTEGER NOT NULL,
                        FOREIGN KEY(splitTemplateId)
                            REFERENCES split_template(id)
                            ON DELETE CASCADE,
                        FOREIGN KEY(exerciseId)
                            REFERENCES exercise(id)
                            ON DELETE RESTRICT,
                        FOREIGN KEY(paceProfileId)
                            REFERENCES exercise_pace_profile(id)
                            ON DELETE SET NULL
                    )
                    """.trimIndent()
            )

            db.execSQL(
                """
                    INSERT INTO split_template_exercise_new (
                        id,
                        splitTemplateId,
                        exerciseId,
                        paceProfileId,
                        suggestedOrder,
                        notes,
                        targetSets,
                        targetRepsMin,
                        targetRepsMax,
                        targetDistance,
                        targetDistanceUnit,
                        targetDurationMinutes,
                        isOptional
                    )
                    SELECT
                        id,
                        splitTemplateId,
                        exerciseId,
                        NULL,
                        suggestedOrder,
                        notes,
                        targetSets,
                        targetRepsMin,
                        targetRepsMax,
                        targetDistance,
                        targetDistanceUnit,
                        targetDurationMinutes,
                        isOptional
                    FROM split_template_exercise
                    """.trimIndent()
            )

            db.execSQL(
                """
                    DROP TABLE split_template_exercise
                    """.trimIndent()
            )

            db.execSQL(
                """
                    ALTER TABLE split_template_exercise_new
                    RENAME TO split_template_exercise
                    """.trimIndent()
            )

            db.execSQL(
                """
                    CREATE INDEX IF NOT EXISTS index_split_template_exercise_splitTemplateId
                    ON split_template_exercise(splitTemplateId)
                    """.trimIndent()
            )

            db.execSQL(
                """
                    CREATE INDEX IF NOT EXISTS index_split_template_exercise_exerciseId
                    ON split_template_exercise(exerciseId)
                    """.trimIndent()
            )

            db.execSQL(
                """
                    CREATE INDEX IF NOT EXISTS index_split_template_exercise_paceProfileId
                    ON split_template_exercise(paceProfileId)
                    """.trimIndent()
            )

            db.execSQL(
                """
                    CREATE UNIQUE INDEX IF NOT EXISTS index_split_template_exercise_splitTemplateId_exerciseId
                    ON split_template_exercise(splitTemplateId, exerciseId)
                    """.trimIndent()
            )
        }
    }

    private val MIGRATION_13_14 = object : Migration(13, 14) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS planned_session_exercise_new (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    plannedSessionId INTEGER NOT NULL,
                    plannedExerciseId INTEGER NOT NULL,
                    paceProfileId INTEGER,
                    sourceSplitTemplateExerciseId INTEGER,
                    sourcePlannedSessionExerciseId INTEGER,
                    originType TEXT NOT NULL,
                    suggestedOrder INTEGER NOT NULL,
                    status TEXT NOT NULL,
                    notes TEXT,
                    FOREIGN KEY(plannedSessionId)
                        REFERENCES planned_session(id)
                        ON DELETE CASCADE,
                    FOREIGN KEY(plannedExerciseId)
                        REFERENCES exercise(id)
                        ON DELETE RESTRICT,
                    FOREIGN KEY(sourceSplitTemplateExerciseId)
                        REFERENCES split_template_exercise(id)
                        ON DELETE SET NULL,
                    FOREIGN KEY(paceProfileId)
                        REFERENCES exercise_pace_profile(id)
                        ON DELETE SET NULL
                )
                """.trimIndent()
            )

            db.execSQL(
                """
                INSERT INTO planned_session_exercise_new (
                    id,
                    plannedSessionId,
                    plannedExerciseId,
                    paceProfileId,
                    sourceSplitTemplateExerciseId,
                    sourcePlannedSessionExerciseId,
                    originType,
                    suggestedOrder,
                    status,
                    notes
                )
                SELECT
                    id,
                    plannedSessionId,
                    plannedExerciseId,
                    NULL,
                    sourceSplitTemplateExerciseId,
                    sourcePlannedSessionExerciseId,
                    originType,
                    suggestedOrder,
                    status,
                    notes
                FROM planned_session_exercise
                """.trimIndent()
            )

            db.execSQL(
                """
                DROP TABLE planned_session_exercise
                """.trimIndent()
            )

            db.execSQL(
                """
                ALTER TABLE planned_session_exercise_new
                RENAME TO planned_session_exercise
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE INDEX IF NOT EXISTS index_planned_session_exercise_plannedSessionId
                ON planned_session_exercise(plannedSessionId)
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE INDEX IF NOT EXISTS index_planned_session_exercise_plannedExerciseId
                ON planned_session_exercise(plannedExerciseId)
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE INDEX IF NOT EXISTS index_planned_session_exercise_sourceSplitTemplateExerciseId
                ON planned_session_exercise(sourceSplitTemplateExerciseId)
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE INDEX IF NOT EXISTS index_planned_session_exercise_sourcePlannedSessionExerciseId
                ON planned_session_exercise(sourcePlannedSessionExerciseId)
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE INDEX IF NOT EXISTS index_planned_session_exercise_paceProfileId
                ON planned_session_exercise(paceProfileId)
                """.trimIndent()
            )
        }
    }

    private val MIGRATION_14_15 = object : Migration(14, 15) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                    CREATE TABLE IF NOT EXISTS exercise_motivational_goal (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        exerciseId INTEGER NOT NULL,
                        goalType TEXT NOT NULL,
                        targetWeight REAL,
                        targetReps INTEGER,
                        targetOneRepMax REAL,
                        targetDistance REAL,
                        targetDistanceUnit TEXT,
                        targetDurationSeconds INTEGER,
                        title TEXT NOT NULL,
                        notes TEXT,
                        isActive INTEGER NOT NULL,
                        isMotivationalOnly INTEGER NOT NULL,
                        sourceGoalId INTEGER,
                        createdAtEpochMillis INTEGER NOT NULL,
                        updatedAtEpochMillis INTEGER NOT NULL,
                        FOREIGN KEY(exerciseId)
                            REFERENCES exercise(id)
                            ON DELETE CASCADE,
                        FOREIGN KEY(sourceGoalId)
                            REFERENCES exercise_motivational_goal(id)
                            ON DELETE SET NULL
                    )
                    """.trimIndent()
            )

            db.execSQL(
                """
                    CREATE INDEX IF NOT EXISTS index_exercise_motivational_goal_exerciseId
                    ON exercise_motivational_goal(exerciseId)
                    """.trimIndent()
            )

            db.execSQL(
                """
                    CREATE INDEX IF NOT EXISTS index_exercise_motivational_goal_goalType
                    ON exercise_motivational_goal(goalType)
                    """.trimIndent()
            )

            db.execSQL(
                """
                    CREATE INDEX IF NOT EXISTS index_exercise_motivational_goal_sourceGoalId
                    ON exercise_motivational_goal(sourceGoalId)
                    """.trimIndent()
            )

            db.execSQL(
                """
                    CREATE INDEX IF NOT EXISTS index_exercise_motivational_goal_exerciseId_isActive
                    ON exercise_motivational_goal(exerciseId, isActive)
                    """.trimIndent()
            )

            db.execSQL(
                """
                    CREATE TABLE IF NOT EXISTS exercise_motivational_goal_assignment (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        goalId INTEGER NOT NULL,
                        scopeType TEXT NOT NULL,
                        splitTemplateId INTEGER,
                        splitTemplateExerciseId INTEGER,
                        trainingCycleId INTEGER,
                        trainingCycleStepId INTEGER,
                        splitScheduleId INTEGER,
                        programId INTEGER,
                        startEpochDay INTEGER,
                        targetEpochDay INTEGER,
                        horizonWeeks INTEGER,
                        sourceAssignmentId INTEGER,
                        isActive INTEGER NOT NULL,
                        createdAtEpochMillis INTEGER NOT NULL,
                        updatedAtEpochMillis INTEGER NOT NULL,
                        FOREIGN KEY(goalId)
                            REFERENCES exercise_motivational_goal(id)
                            ON DELETE CASCADE,
                        FOREIGN KEY(splitTemplateId)
                            REFERENCES split_template(id)
                            ON DELETE CASCADE,
                        FOREIGN KEY(splitTemplateExerciseId)
                            REFERENCES split_template_exercise(id)
                            ON DELETE CASCADE,
                        FOREIGN KEY(trainingCycleId)
                            REFERENCES training_cycle(id)
                            ON DELETE CASCADE,
                        FOREIGN KEY(trainingCycleStepId)
                            REFERENCES training_cycle_step(id)
                            ON DELETE CASCADE,
                        FOREIGN KEY(splitScheduleId)
                            REFERENCES split_schedule(id)
                            ON DELETE CASCADE,
                        FOREIGN KEY(programId)
                            REFERENCES program(id)
                            ON DELETE CASCADE,
                        FOREIGN KEY(sourceAssignmentId)
                            REFERENCES exercise_motivational_goal_assignment(id)
                            ON DELETE SET NULL
                    )
                    """.trimIndent()
            )

            db.execSQL(
                """
                    CREATE INDEX IF NOT EXISTS index_exercise_motivational_goal_assignment_goalId
                    ON exercise_motivational_goal_assignment(goalId)
                    """.trimIndent()
            )

            db.execSQL(
                """
                    CREATE INDEX IF NOT EXISTS index_exercise_motivational_goal_assignment_scopeType
                    ON exercise_motivational_goal_assignment(scopeType)
                    """.trimIndent()
            )

            db.execSQL(
                """
                    CREATE INDEX IF NOT EXISTS index_exercise_motivational_goal_assignment_splitTemplateId
                    ON exercise_motivational_goal_assignment(splitTemplateId)
                    """.trimIndent()
            )

            db.execSQL(
                """
                    CREATE INDEX IF NOT EXISTS index_exercise_motivational_goal_assignment_splitTemplateExerciseId
                    ON exercise_motivational_goal_assignment(splitTemplateExerciseId)
                    """.trimIndent()
            )

            db.execSQL(
                """
                    CREATE INDEX IF NOT EXISTS index_exercise_motivational_goal_assignment_trainingCycleId
                    ON exercise_motivational_goal_assignment(trainingCycleId)
                    """.trimIndent()
            )

            db.execSQL(
                """
                    CREATE INDEX IF NOT EXISTS index_exercise_motivational_goal_assignment_trainingCycleStepId
                    ON exercise_motivational_goal_assignment(trainingCycleStepId)
                    """.trimIndent()
            )

            db.execSQL(
                """
                    CREATE INDEX IF NOT EXISTS index_exercise_motivational_goal_assignment_splitScheduleId
                    ON exercise_motivational_goal_assignment(splitScheduleId)
                    """.trimIndent()
            )

            db.execSQL(
                """
                    CREATE INDEX IF NOT EXISTS index_exercise_motivational_goal_assignment_programId
                    ON exercise_motivational_goal_assignment(programId)
                    """.trimIndent()
            )

            db.execSQL(
                """
                    CREATE INDEX IF NOT EXISTS index_exercise_motivational_goal_assignment_sourceAssignmentId
                    ON exercise_motivational_goal_assignment(sourceAssignmentId)
                    """.trimIndent()
            )

            db.execSQL(
                """
                    CREATE INDEX IF NOT EXISTS index_exercise_motivational_goal_assignment_goalId_scopeType
                    ON exercise_motivational_goal_assignment(goalId, scopeType)
                    """.trimIndent()
            )

            db.execSQL(
                """
                    CREATE INDEX IF NOT EXISTS index_exercise_motivational_goal_assignment_scopeType_isActive
                    ON exercise_motivational_goal_assignment(scopeType, isActive)
                    """.trimIndent()
            )
        }
    }

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): KusgangAliwasDatabase {
        return Room.databaseBuilder(
            context,
            KusgangAliwasDatabase::class.java,
            "kusgang_aliwas.db",
        )
            .addCallback(DatabaseSeedCallback())
            .addMigrations(
                MIGRATION_1_2,
                MIGRATION_2_3,
                MIGRATION_3_4,
                MIGRATION_4_5,
                MIGRATION_5_6,
                MIGRATION_6_7,
                MIGRATION_7_8,
                MIGRATION_8_9,
                MIGRATION_9_10,
                MIGRATION_10_11,
                MIGRATION_11_12,
                MIGRATION_12_13,
                MIGRATION_13_14,
                MIGRATION_14_15,
            )
            .build()
    }

    @Provides
    fun provideExerciseDao(database: KusgangAliwasDatabase): ExerciseDao =
        database.exerciseDao()

    @Provides
    fun provideMuscleGroupDao(database: KusgangAliwasDatabase): MuscleGroupDao =
        database.muscleGroupDao()

    @Provides
    fun provideExerciseMuscleGroupDao(database: KusgangAliwasDatabase): ExerciseMuscleGroupDao =
        database.exerciseMuscleGroupDao()

    @Provides
    fun provideExerciseSubstitutionDao(database: KusgangAliwasDatabase): ExerciseSubstitutionDao =
        database.exerciseSubstitutionDao()

    @Provides
    fun provideExercisePaceProfileDao(database: KusgangAliwasDatabase): ExercisePaceProfileDao =
        database.exercisePaceProfileDao()

    @Provides
    fun provideExerciseMotivationalGoalDao(database: KusgangAliwasDatabase): ExerciseMotivationalGoalDao =
        database.exerciseMotivationalGoalDao()

    @Provides
    fun provideSplitTemplateDao(database: KusgangAliwasDatabase): SplitTemplateDao =
        database.splitTemplateDao()

    @Provides
    fun provideSplitTemplateExerciseDao(database: KusgangAliwasDatabase): SplitTemplateExerciseDao =
        database.splitTemplateExerciseDao()

    @Provides
    fun provideTrainingCycleDao(database: KusgangAliwasDatabase): TrainingCycleDao =
        database.trainingCycleDao()

    @Provides
    fun provideTrainingCycleStepDao(database: KusgangAliwasDatabase): TrainingCycleStepDao =
        database.trainingCycleStepDao()

    @Provides
    fun provideTrainingCycleProgressEventDao(
        database: KusgangAliwasDatabase,
    ): TrainingCycleProgressEventDao =
        database.trainingCycleProgressEventDao()

    @Provides
    fun provideCycleCalendarAnchorDao(database: KusgangAliwasDatabase): CycleCalendarAnchorDao =
        database.cycleCalendarAnchorDao()

    @Provides
    fun providePlannedSessionDao(database: KusgangAliwasDatabase): PlannedSessionDao =
        database.plannedSessionDao()

    @Provides
    fun providePlannedSessionExerciseDao(database: KusgangAliwasDatabase): PlannedSessionExerciseDao =
        database.plannedSessionExerciseDao()

    @Provides
    fun provideActualSessionDao(database: KusgangAliwasDatabase): ActualSessionDao =
        database.actualSessionDao()

    @Provides
    fun provideActualExerciseLogDao(database: KusgangAliwasDatabase): ActualExerciseLogDao =
        database.actualExerciseLogDao()

    @Provides
    fun provideActualExerciseSetLogDao(database: KusgangAliwasDatabase): ActualExerciseSetLogDao =
        database.actualExerciseSetLogDao()

    @Provides
    fun provideActualCardioLogDao(database: KusgangAliwasDatabase): ActualCardioLogDao =
        database.actualCardioLogDao()

    @Provides
    fun provideProgramDao(database: KusgangAliwasDatabase): ProgramDao =
        database.programDao()

    @Provides
    fun provideSplitScheduleDao(database: KusgangAliwasDatabase): SplitScheduleDao =
        database.splitScheduleDao()

    @Provides
    fun provideExercisePrDao(database: KusgangAliwasDatabase): ExercisePrDao =
        database.exercisePrDao()

    @Provides
    fun provideSplitTemplateMuscleGroupDao(database: KusgangAliwasDatabase): SplitTemplateMuscleGroupDao =
        database.splitTemplateMuscleGroupDao()

    @Provides
    fun provideTrainingCycleActivationDao(database: KusgangAliwasDatabase): TrainingCycleActivationDao =
        database.trainingCycleActivationDao()
}