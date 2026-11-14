package com.example.gymnotes;

import android.content.Context;
import androidx.room.Room;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

/** One database per process; unsupported versions fail closed, never erase history. */
public final class DatabaseProvider {
    private static volatile AppDatabase instance;
    public static final Migration MIGRATION_6_7 = new Migration(6, 7) {
        @Override public void migrate(SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE completed_workouts ADD COLUMN source_session TEXT");
            db.execSQL("CREATE UNIQUE INDEX index_completed_workouts_source_session ON completed_workouts(source_session)");
            db.execSQL("ALTER TABLE completed_exercises ADD COLUMN warmup INTEGER NOT NULL DEFAULT 0");
            db.execSQL("CREATE TABLE IF NOT EXISTS session_drafts (userId TEXT NOT NULL, workoutId INTEGER NOT NULL, payload TEXT NOT NULL, updatedAt INTEGER NOT NULL, PRIMARY KEY(userId, workoutId))");
        }
    };
    public static AppDatabase get(Context context) {
        if (instance == null) synchronized (DatabaseProvider.class) {
            if (instance == null) instance = Room.databaseBuilder(context.getApplicationContext(),
                    AppDatabase.class, "fitpulse_database").addMigrations(MIGRATION_6_7).build();
        }
        return instance;
    }
    private DatabaseProvider() {}
}
