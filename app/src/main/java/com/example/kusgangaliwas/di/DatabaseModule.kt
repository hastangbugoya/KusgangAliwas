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
            .addMigrations(
                MIGRATION_1_2,
                MIGRATION_2_3,
                MIGRATION_3_4,
                MIGRATION_4_5,
                MIGRATION_5_6,
                MIGRATION_6_7,
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
}