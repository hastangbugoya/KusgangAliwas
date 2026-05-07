package com.example.kusgangaliwas.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
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
import com.example.kusgangaliwas.data.local.dao.SplitTemplateDao
import com.example.kusgangaliwas.data.local.dao.SplitTemplateExerciseDao
import com.example.kusgangaliwas.data.local.dao.TrainingCycleDao
import com.example.kusgangaliwas.data.local.dao.TrainingCycleStepDao
import com.example.kusgangaliwas.data.local.entity.ExerciseEntity
import com.example.kusgangaliwas.data.local.entity.ExerciseMuscleGroupCrossRef
import com.example.kusgangaliwas.data.local.entity.ExerciseSubstitutionEntity
import com.example.kusgangaliwas.data.local.entity.MuscleGroupEntity
import com.example.kusgangaliwas.data.local.entity.SplitTemplateEntity
import com.example.kusgangaliwas.data.local.entity.SplitTemplateExerciseEntity
import com.example.kusgangaliwas.data.local.entity.TrainingCycleEntity
import com.example.kusgangaliwas.data.local.entity.TrainingCycleStepEntity
import com.example.kusgangaliwas.data.local.entity.CycleCalendarAnchorEntity
import com.example.kusgangaliwas.data.local.entity.PlannedSessionEntity
import com.example.kusgangaliwas.data.local.entity.PlannedSessionExerciseEntity
import com.example.kusgangaliwas.data.local.entity.ActualSessionEntity
import com.example.kusgangaliwas.data.local.entity.ActualExerciseLogEntity
import com.example.kusgangaliwas.data.local.entity.ActualExerciseSetLogEntity

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
 * Cycle, calendar, planned session, and actual logging tables will be added
 * in later DB milestones.
 */
@Database(
    entities = [
        ExerciseEntity::class,
        MuscleGroupEntity::class,
        ExerciseMuscleGroupCrossRef::class,
        ExerciseSubstitutionEntity::class,
        SplitTemplateEntity::class,
        SplitTemplateExerciseEntity::class,
        TrainingCycleEntity::class,
        TrainingCycleStepEntity::class,
        CycleCalendarAnchorEntity::class,
        PlannedSessionEntity::class,
        PlannedSessionExerciseEntity::class,
        ActualSessionEntity::class,
        ActualExerciseLogEntity::class,
        ActualExerciseSetLogEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class KusgangAliwasDatabase : RoomDatabase(){
    abstract fun exerciseDao(): ExerciseDao
    abstract fun muscleGroupDao(): MuscleGroupDao
    abstract fun exerciseMuscleGroupDao(): ExerciseMuscleGroupDao
    abstract fun exerciseSubstitutionDao(): ExerciseSubstitutionDao
    abstract fun splitTemplateDao(): SplitTemplateDao
    abstract fun splitTemplateExerciseDao(): SplitTemplateExerciseDao
    abstract fun trainingCycleDao(): TrainingCycleDao
    abstract fun trainingCycleStepDao(): TrainingCycleStepDao
    abstract fun cycleCalendarAnchorDao(): CycleCalendarAnchorDao
    abstract fun plannedSessionDao(): PlannedSessionDao
    abstract fun plannedSessionExerciseDao(): PlannedSessionExerciseDao
    abstract fun actualSessionDao(): ActualSessionDao
    abstract fun actualExerciseLogDao(): ActualExerciseLogDao
    abstract fun actualExerciseSetLogDao(): ActualExerciseSetLogDao
}