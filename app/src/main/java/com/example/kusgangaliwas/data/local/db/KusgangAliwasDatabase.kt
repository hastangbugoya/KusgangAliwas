package com.example.kusgangaliwas.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.kusgangaliwas.data.local.dao.ActualCardioLogDao
import com.example.kusgangaliwas.data.local.dao.ActualExerciseLogDao
import com.example.kusgangaliwas.data.local.dao.ActualExerciseSetLogDao
import com.example.kusgangaliwas.data.local.dao.ActualSessionDao
import com.example.kusgangaliwas.data.local.dao.CycleCalendarAnchorDao
import com.example.kusgangaliwas.data.local.dao.ExerciseDao
import com.example.kusgangaliwas.data.local.dao.ExerciseMuscleGroupDao
import com.example.kusgangaliwas.data.local.dao.ExerciseSubstitutionDao
import com.example.kusgangaliwas.data.local.dao.MuscleGroupDao
import com.example.kusgangaliwas.data.local.dao.PlannedSessionDao
import com.example.kusgangaliwas.data.local.dao.PlannedSessionExerciseDao
import com.example.kusgangaliwas.data.local.dao.ProgramDao
import com.example.kusgangaliwas.data.local.dao.SplitScheduleDao
import com.example.kusgangaliwas.data.local.dao.SplitTemplateDao
import com.example.kusgangaliwas.data.local.dao.SplitTemplateExerciseDao
import com.example.kusgangaliwas.data.local.dao.TrainingCycleDao
import com.example.kusgangaliwas.data.local.dao.TrainingCycleStepDao
import com.example.kusgangaliwas.data.local.dao.ExercisePrDao
import com.example.kusgangaliwas.data.local.entity.ExercisePrEntity
import com.example.kusgangaliwas.data.local.entity.ActualCardioLogEntity
import com.example.kusgangaliwas.data.local.entity.ActualExerciseLogEntity
import com.example.kusgangaliwas.data.local.entity.ActualExerciseSetLogEntity
import com.example.kusgangaliwas.data.local.entity.ActualSessionEntity
import com.example.kusgangaliwas.data.local.entity.CycleCalendarAnchorEntity
import com.example.kusgangaliwas.data.local.entity.ExerciseEntity
import com.example.kusgangaliwas.data.local.entity.ExerciseMuscleGroupCrossRef
import com.example.kusgangaliwas.data.local.entity.ExerciseSubstitutionEntity
import com.example.kusgangaliwas.data.local.entity.MuscleGroupEntity
import com.example.kusgangaliwas.data.local.entity.PlannedSessionEntity
import com.example.kusgangaliwas.data.local.entity.PlannedSessionExerciseEntity
import com.example.kusgangaliwas.data.local.entity.ProgramEntity
import com.example.kusgangaliwas.data.local.entity.SplitScheduleEntity
import com.example.kusgangaliwas.data.local.entity.SplitTemplateEntity
import com.example.kusgangaliwas.data.local.entity.SplitTemplateExerciseEntity
import com.example.kusgangaliwas.data.local.entity.TrainingCycleEntity
import com.example.kusgangaliwas.data.local.entity.TrainingCycleStepEntity
import com.example.kusgangaliwas.data.local.dao.SplitTemplateMuscleGroupDao
import com.example.kusgangaliwas.data.local.dao.TrainingCycleActivationDao
import com.example.kusgangaliwas.data.local.entity.SplitTemplateMuscleGroupCrossRef
import com.example.kusgangaliwas.data.local.dao.TrainingCycleProgressEventDao
import com.example.kusgangaliwas.data.local.entity.TrainingCycleProgressEventEntity
import com.example.kusgangaliwas.data.local.entity.TrainingCycleActivationEntity

/**
 * Main Room database for Kusgang Aliwas.
 *
 * DB-1 intentionally contains only the foundation tables:
 * - exercise library
 * - muscle groups
 * - exercise ↔ muscle group mapping
 * - exercise substitutions
 * - split templates
 * - split template exercises
 *
 * Cycle, calendar, planned session, and actual logging tables were added
 * in later DB milestones.
 *
 * DB-4 adds simple week-horizon split scheduling:
 * - optional programs
 * - split schedule rules
 *
 * DB-5 adds mixed session cardio logging:
 * - cardio blocks can be shown beside strength logs in one ordered session list
 *
 * DB-7 adds cardio distance estimation metadata:
 * - isEstimatedDistance
 * - intensityLevel
 *
 * DB-10 simplifies training cycle steps into an ordered split queue and adds
 * optional cycle linkage snapshots to actual sessions.
 *
 * DB-11 adds explicit non-session cycle progress events for actions like
 * marking a split done without creating a workout session.
 *
 * DB-12 adds training cycle activation runs:
 * - active cycles remain active until manually deactivated
 * - restarting as a new run creates a new activation window
 *
 */
@Database(
    entities = [
        ExerciseEntity::class,
        MuscleGroupEntity::class,
        ExerciseMuscleGroupCrossRef::class,
        ExerciseSubstitutionEntity::class,
        SplitTemplateEntity::class,
        SplitTemplateExerciseEntity::class,
        SplitTemplateMuscleGroupCrossRef::class,
        TrainingCycleEntity::class,
        TrainingCycleStepEntity::class,
        TrainingCycleProgressEventEntity::class,
        CycleCalendarAnchorEntity::class,
        PlannedSessionEntity::class,
        PlannedSessionExerciseEntity::class,
        ActualSessionEntity::class,
        ActualExerciseLogEntity::class,
        ActualExerciseSetLogEntity::class,
        ActualCardioLogEntity::class,
        ProgramEntity::class,
        SplitScheduleEntity::class,
        ExercisePrEntity::class,
        TrainingCycleActivationEntity::class,
    ],
    version = 12,
    exportSchema = true,
)
@TypeConverters(DatabaseConverters::class)
abstract class KusgangAliwasDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun muscleGroupDao(): MuscleGroupDao
    abstract fun exerciseMuscleGroupDao(): ExerciseMuscleGroupDao
    abstract fun exerciseSubstitutionDao(): ExerciseSubstitutionDao
    abstract fun splitTemplateDao(): SplitTemplateDao
    abstract fun splitTemplateExerciseDao(): SplitTemplateExerciseDao
    abstract fun splitTemplateMuscleGroupDao(): SplitTemplateMuscleGroupDao
    abstract fun trainingCycleDao(): TrainingCycleDao
    abstract fun trainingCycleStepDao(): TrainingCycleStepDao
    abstract fun cycleCalendarAnchorDao(): CycleCalendarAnchorDao
    abstract fun plannedSessionDao(): PlannedSessionDao
    abstract fun plannedSessionExerciseDao(): PlannedSessionExerciseDao
    abstract fun actualSessionDao(): ActualSessionDao
    abstract fun actualExerciseLogDao(): ActualExerciseLogDao
    abstract fun actualExerciseSetLogDao(): ActualExerciseSetLogDao
    abstract fun actualCardioLogDao(): ActualCardioLogDao
    abstract fun programDao(): ProgramDao
    abstract fun splitScheduleDao(): SplitScheduleDao
    abstract fun exercisePrDao(): ExercisePrDao
    abstract fun trainingCycleProgressEventDao(): TrainingCycleProgressEventDao
    abstract fun trainingCycleActivationDao(): TrainingCycleActivationDao
}