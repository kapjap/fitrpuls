package com.example.gymnotes;

import com.google.gson.Gson;
import java.util.ArrayList;

/** Saved-instance-state copy, scoped to the signed-in owner; never written over a stored plan. */
final class WorkoutFormDraft {
    String owner;
    ArrayList<ExerciseEntity> exercises;
    static String encode(String owner, ArrayList<ExerciseEntity> exercises) {
        WorkoutFormDraft draft=new WorkoutFormDraft();draft.owner=owner;draft.exercises=exercises;
        return new Gson().toJson(draft);
    }
    static ArrayList<ExerciseEntity> restore(String json,String owner) {
        if(json==null)return new ArrayList<>();
        WorkoutFormDraft draft=new Gson().fromJson(json,WorkoutFormDraft.class);
        if(draft==null||owner==null||!owner.equals(draft.owner)||draft.exercises==null)return new ArrayList<>();
        return draft.exercises;
    }
}
