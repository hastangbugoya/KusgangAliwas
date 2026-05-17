package com.example.kusgangaliwas.data.local.db

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Minimal development seed callback.
 *
 * Intentionally tiny:
 * - only inserts baseline sample rows
 * - avoids large fake datasets
 * - safe to expand later table-by-table
 */
class DatabaseSeedCallback : RoomDatabase.Callback() {

    override fun onCreate(
        db: SupportSQLiteDatabase,
    ) {
        super.onCreate(db)

        val now = System.currentTimeMillis()

        db.execSQL(
            """
            INSERT INTO exercise (name,exerciseType,sActive,createdAtEpochMillis,updatedAtEpochMillis)
            VALUES ('Barbell Bicep Curls','STRENGTH',1,$now,$now)
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO exercise (name,exerciseType,sActive,createdAtEpochMillis,updatedAtEpochMillis)
            VALUES ('Bench Press','STRENGTH',1,$now,$now)
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO exercise (name,exerciseType,sActive,createdAtEpochMillis,updatedAtEpochMillis)
            VALUES ('Cable Row','STRENGTH',1,$now,$now)
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO exercise (name,exerciseType,sActive,createdAtEpochMillis,updatedAtEpochMillis)
            VALUES ('Calf Raise','STRENGTH',1,$now,$now)
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO exercise (name,exerciseType,sActive,createdAtEpochMillis,updatedAtEpochMillis)
            VALUES ('Chest Fly Machine','STRENGTH',1,$now,$now)
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO exercise (name,exerciseType,sActive,createdAtEpochMillis,updatedAtEpochMillis)
            VALUES ('Dips','STRENGTH',1,$now,$now)
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO exercise (name,exerciseType,sActive,createdAtEpochMillis,updatedAtEpochMillis)
            VALUES ('Dumbbell Bench Press','STRENGTH',1,$now,$now)
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO exercise (name,exerciseType,sActive,createdAtEpochMillis,updatedAtEpochMillis)
            VALUES ('Dumbbell Bicep Alternating Curls','STRENGTH',1,$now,$now)
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO exercise (name,exerciseType,sActive,createdAtEpochMillis,updatedAtEpochMillis)
            VALUES ('Dumbbell Inclined Press','STRENGTH',1,$now,$now)
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO exercise (name,exerciseType,sActive,createdAtEpochMillis,updatedAtEpochMillis)
            VALUES ('Dumbbell Lateral Raises','STRENGTH',1,$now,$now)
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO exercise (name,exerciseType,sActive,createdAtEpochMillis,updatedAtEpochMillis)
            VALUES ('Dumbbell Romanian Deadlift','STRENGTH',1,$now,$now)
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO exercise (name,exerciseType,sActive,createdAtEpochMillis,updatedAtEpochMillis)
            VALUES ('Lat Pulldown Narrow','STRENGTH',1,$now,$now)
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO exercise (name,exerciseType,sActive,createdAtEpochMillis,updatedAtEpochMillis)
            VALUES ('Lat Pulldown Wide','STRENGTH',1,$now,$now)
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO exercise (name,exerciseType,sActive,createdAtEpochMillis,updatedAtEpochMillis)
            VALUES ('Leg Curl Machine','STRENGTH',1,$now,$now)
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO exercise (name,exerciseType,sActive,createdAtEpochMillis,updatedAtEpochMillis)
            VALUES ('Leg Extension','STRENGTH',1,$now,$now)
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO exercise (name,exerciseType,sActive,createdAtEpochMillis,updatedAtEpochMillis)
            VALUES ('Leg Press Machine High Narrow','STRENGTH',1,$now,$now)
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO exercise (name,exerciseType,sActive,createdAtEpochMillis,updatedAtEpochMillis)
            VALUES ('Leg Press Machine High Wide','STRENGTH',1,$now,$now)
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO exercise (name,exerciseType,sActive,createdAtEpochMillis,updatedAtEpochMillis)
            VALUES ('Leg Press Machine Low Narrow','STRENGTH',1,$now,$now)
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO exercise (name,exerciseType,sActive,createdAtEpochMillis,updatedAtEpochMillis)
            VALUES ('Leg Press Machine Low Wide','STRENGTH',1,$now,$now)
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO exercise (name,exerciseType,sActive,createdAtEpochMillis,updatedAtEpochMillis)
            VALUES ('Leg Press Machine Middle','STRENGTH',1,$now,$now)
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO exercise (name,exerciseType,sActive,createdAtEpochMillis,updatedAtEpochMillis)
            VALUES ('Push-Ups','STRENGTH',1,$now,$now)
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO exercise (name,exerciseType,sActive,createdAtEpochMillis,updatedAtEpochMillis)
            VALUES ('Chest Fly Machine','STRENGTH',1,$now,$now)
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO exercise (name,exerciseType,sActive,createdAtEpochMillis,updatedAtEpochMillis)
            VALUES ('Tricep Pushdowns Bar','STRENGTH',1,$now,$now)
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO exercise (name,exerciseType,sActive,createdAtEpochMillis,updatedAtEpochMillis)
            VALUES ('Tricep Pushdowns Rope','STRENGTH',1,$now,$now)
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO exercise (name,exerciseType,sActive,createdAtEpochMillis,updatedAtEpochMillis)
            VALUES ('Cable Overhead Tricep Extension','STRENGTH',1,$now,$now)
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO exercise (name,exerciseType,sActive,createdAtEpochMillis,updatedAtEpochMillis)
            VALUES ('Dumbbell Tricep Extensions','STRENGTH',1,$now,$now)
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO exercise (name,exerciseType,sActive,createdAtEpochMillis,updatedAtEpochMillis)
            VALUES ('Cable Overhead Tricep Extension','STRENGTH',1,$now,$now)
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO exercise (name,exerciseType,sActive,createdAtEpochMillis,updatedAtEpochMillis)
            VALUES ('Shrugs','STRENGTH',1,$now,$now)
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO exercise (name,exerciseType,sActive,createdAtEpochMillis,updatedAtEpochMillis)
            VALUES ('Face Pulls','STRENGTH',1,$now,$now)
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO exercise (name,exerciseType,sActive,createdAtEpochMillis,updatedAtEpochMillis)
            VALUES ('Front Raises Plates','STRENGTH',1,$now,$now)
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO exercise (name,exerciseType,sActive,createdAtEpochMillis,updatedAtEpochMillis)
            VALUES ('Alternating Dumbbell Front Raises','STRENGTH',1,$now,$now)
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO exercise (name,exerciseType,isActive,createdAtEpochMillis,updatedAtEpochMillis)
            VALUES ('Treadmill','CARDIO',1,$now,$now)
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO exercise (name,exerciseType,isActive,createdAtEpochMillis,updatedAtEpochMillis)
            VALUES ('StairMasters','CARDIO',1,$now,$now)
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO exercise (name,exerciseType,isActive,createdAtEpochMillis,updatedAtEpochMillis)
            VALUES ('Upright Bike','CARDIO',1,$now,$now)
            """.trimIndent()
        )
    }
}