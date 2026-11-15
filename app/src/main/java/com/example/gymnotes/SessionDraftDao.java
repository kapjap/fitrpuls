package com.example.gymnotes;

import androidx.room.*;
@Dao
public interface SessionDraftDao {
    @Query("SELECT * FROM session_drafts WHERE userId = :uid ORDER BY updatedAt DESC LIMIT 1")
    androidx.lifecycle.LiveData<SessionDraft> observeLatest(String uid);
    @Query("SELECT * FROM session_drafts WHERE userId = :uid AND workoutId = :id")
    SessionDraft get(String uid, int id);
    @Query("SELECT workoutId FROM session_drafts WHERE userId = :uid")
    java.util.List<Integer> getWorkoutIds(String uid);
    @Insert(onConflict = OnConflictStrategy.REPLACE) void put(SessionDraft draft);
    @Query("DELETE FROM session_drafts WHERE userId = :uid AND workoutId = :id")
    void delete(String uid, int id);
    @Query("DELETE FROM session_drafts WHERE userId = :uid") void deleteByUser(String uid);
}
