package com.example.gymnotes;

import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Ordered local operations; history and draft removal commit atomically. */
public final class SessionRepository {
    public static final ExecutorService IO = Executors.newSingleThreadExecutor();
    private final AppDatabase db;
    private final Gson gson = new Gson();

    public SessionRepository(AppDatabase db) { this.db = db; }

    public SessionSnapshot load(String uid, int workoutId) {
        SessionDraft draft = db.sessionDraftDao().get(uid, workoutId);
        if (draft != null) {
            SessionSnapshot snapshot = gson.fromJson(draft.payload, SessionSnapshot.class);
            validate(snapshot);
            return snapshot;
        }
        WorkoutEntity workout = db.workoutDao().getWorkoutById(workoutId);
        if (workout == null || !uid.equals(workout.getUserId()))
            throw new IllegalStateException("Workout unavailable");
        SessionSnapshot snapshot = new SessionSnapshot();
        snapshot.title = workout.getTitle();
        for (ExerciseEntity exercise : db.exerciseDao().getExercisesByWorkout(workoutId)) {
            SessionSnapshot.Entry entry = new SessionSnapshot.Entry();
            entry.id = exercise.id;
            entry.name = exercise.name;
            entry.plannedSets = exercise.sets;
            entry.reps = exercise.reps;
            entry.weight = exercise.weight;
            for (int i = 0; i < exercise.sets; i++) {
                SessionSnapshot.SetEntry set = new SessionSnapshot.SetEntry();
                set.weight = exercise.weight;
                set.reps = exercise.reps;
                entry.sets.add(set);
            }
            snapshot.entries.add(entry);
        }
        save(uid, workoutId, snapshot);
        return snapshot;
    }

    public void save(String uid, int workoutId, SessionSnapshot snapshot) {
        validate(snapshot);
        SessionDraft draft = new SessionDraft();
        draft.userId = uid;
        draft.workoutId = workoutId;
        draft.payload = gson.toJson(snapshot);
        draft.updatedAt = System.currentTimeMillis();
        db.runInTransaction(() -> {
            if (db.completedWorkoutDao().countSession(snapshot.sessionId) > 0) return;
            SessionDraft current = db.sessionDraftDao().get(uid, workoutId);
            if (current != null && !snapshot.sessionId.equals(gson.fromJson(current.payload, SessionSnapshot.class).sessionId))
                throw new IllegalStateException("Another session is active");
            db.sessionDraftDao().put(draft);
        });
    }

    public void finish(String uid, int workoutId, SessionSnapshot snapshot) {
        validate(snapshot);
        if (snapshot.completedExercises() == 0) throw new IllegalArgumentException("No completed sets");
        db.runInTransaction(() -> {
            if (db.completedWorkoutDao().countSession(snapshot.sessionId) > 0) return;
            SessionDraft current = db.sessionDraftDao().get(uid, workoutId);
            if (current == null || !snapshot.sessionId.equals(gson.fromJson(current.payload, SessionSnapshot.class).sessionId))
                throw new IllegalStateException("Session unavailable");
            CompletedWorkoutEntity workout = new CompletedWorkoutEntity(workoutId, uid, snapshot.title,
                    Math.max(1, (snapshot.seconds + 59) / 60), snapshot.volume(),
                    snapshot.completedExercises(), System.currentTimeMillis());
            workout.sourceSession = snapshot.sessionId;
            long id = db.completedWorkoutDao().insertCompletedWorkout(workout);
            List<CompletedExerciseEntity> entries = new ArrayList<>();
            for (SessionSnapshot.Entry e : snapshot.entries) {
                for (SessionSnapshot.SetEntry set : e.sets) {
                    if (!set.done) continue;
                    CompletedExerciseEntity completed = new CompletedExerciseEntity((int) id, e.name, 1, set.reps, set.weight);
                    completed.warmup = set.warmup;
                    entries.add(completed);
                }
            }
            db.completedExerciseDao().insertAll(entries);
            db.sessionDraftDao().delete(uid, workoutId);
        });
    }

    private void validate(SessionSnapshot snapshot) {
        if (snapshot == null || snapshot.version != 1 || snapshot.entries == null || snapshot.sessionId == null)
            throw new IllegalStateException("Unsupported session snapshot");
        for (SessionSnapshot.Entry e : snapshot.entries) {
            if (e == null || e.sets == null) throw new IllegalStateException("Invalid exercise");
            for (SessionSnapshot.SetEntry set : e.sets) {
                if (set == null || !Double.isFinite(set.weight) || set.weight < 0 || set.reps < 1)
                    throw new IllegalArgumentException("Invalid set");
            }
        }
    }
}
