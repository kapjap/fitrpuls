package com.example.gymnotes;

import androidx.annotation.NonNull;
import androidx.room.Entity;

/** Durable snapshot, isolated by account and template. Never uploaded implicitly. */
@Entity(tableName = "session_drafts", primaryKeys = {"userId", "workoutId"})
public class SessionDraft {
    @NonNull public String userId = "";
    public int workoutId;
    @NonNull public String payload = "";
    public long updatedAt;
}
