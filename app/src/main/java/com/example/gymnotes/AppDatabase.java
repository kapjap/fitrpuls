package com.example.gymnotes;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(
        entities = {
                WorkoutEntity.class,
                ExerciseEntity.class,
                CompletedWorkoutEntity.class,
                CompletedExerciseEntity.class,
                SessionDraft.class
        },
        version = 7
)
public abstract class AppDatabase extends RoomDatabase {
    public abstract SessionDraftDao sessionDraftDao();
    public abstract WorkoutDao workoutDao();
    public abstract ExerciseDao exerciseDao();
    public abstract CompletedWorkoutDao completedWorkoutDao();
    public abstract CompletedExerciseDao completedExerciseDao();
}