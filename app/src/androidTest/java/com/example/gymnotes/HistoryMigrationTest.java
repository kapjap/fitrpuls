package com.example.gymnotes;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class HistoryMigrationTest {
    @Test public void versionSixHistorySurvivesMigrationAndFinishIsIdempotent() {
        Context context=ApplicationProvider.getApplicationContext();String name="migration-test";context.deleteDatabase(name);
        SQLiteDatabase old=context.openOrCreateDatabase(name,0,null);
        old.execSQL("CREATE TABLE workouts (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,title TEXT,exerciseCount INTEGER NOT NULL,estimatedTime INTEGER NOT NULL,restSeconds INTEGER NOT NULL,user_id TEXT,created_at INTEGER NOT NULL)");
        old.execSQL("CREATE TABLE ExerciseEntity (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,workoutId INTEGER NOT NULL,name TEXT,sets INTEGER NOT NULL,reps INTEGER NOT NULL,weight REAL NOT NULL)");
        old.execSQL("CREATE TABLE completed_workouts (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,workout_id INTEGER NOT NULL,user_id TEXT,title TEXT,duration_minutes INTEGER NOT NULL,total_weight REAL NOT NULL,exercise_count INTEGER NOT NULL,completed_at INTEGER NOT NULL)");
        old.execSQL("CREATE TABLE completed_exercises (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,completed_workout_id INTEGER NOT NULL,name TEXT,sets INTEGER NOT NULL,reps INTEGER NOT NULL,weight REAL NOT NULL)");
        old.execSQL("INSERT INTO completed_workouts VALUES(1,1,'owner','Legacy',20,1000,1,1234)");
        old.execSQL("INSERT INTO completed_exercises VALUES(1,1,'Squat',2,10,50)");
        old.execSQL("INSERT INTO workouts VALUES(1,'Plan',1,30,60,'owner',1234)");
        old.execSQL("INSERT INTO ExerciseEntity VALUES(1,1,'Squat',3,10,50)");
        old.setVersion(6);old.close();
        AppDatabase db=Room.databaseBuilder(context,AppDatabase.class,name).addMigrations(DatabaseProvider.MIGRATION_6_7).build();
        try {
            assertEquals(1,db.completedWorkoutDao().getCompletedWorkoutCountByUser("owner"));
            assertEquals(1000,db.completedWorkoutDao().getTotalCompletedWeightByUser("owner"),0);
            assertFalse(db.completedExerciseDao().getExercisesByCompletedWorkout(1).get(0).warmup);
            SessionRepository repo=new SessionRepository(db);SessionSnapshot s=repo.load("owner",1);
            s.entries.get(0).sets.get(1).done=true;repo.save("owner",1,s);
            assertTrue(repo.load("owner",1).entries.get(0).sets.get(1).done);
            assertNull(db.sessionDraftDao().get("other",1));
            repo.finish("owner",1,s);repo.finish("owner",1,s);
            repo.save("owner",1,s);
            assertNull(db.sessionDraftDao().get("owner",1));
            assertEquals(2,db.completedWorkoutDao().getCompletedWorkoutCountByUser("owner"));
            assertEquals(1500,db.completedWorkoutDao().getTotalCompletedWeightByUser("owner"),0);
        } finally {db.close();context.deleteDatabase(name);}
    }
}
