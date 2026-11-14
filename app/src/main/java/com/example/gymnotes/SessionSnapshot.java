package com.example.gymnotes;

import java.util.ArrayList;
import java.util.List;

/** Versioned snapshot of actual work; independent from later template edits. */
public class SessionSnapshot {
    public int version = 1;
    public String sessionId = java.util.UUID.randomUUID().toString();
    public String title;
    public int seconds;
    public int restSeconds;
    public boolean paused;
    public long savedAt;
    public List<Entry> entries = new ArrayList<>();
    public static class Entry {
        public int id;
        public String name;
        public int plannedSets;
        public int completedSets;
        public int reps;
        public double weight;
        public List<SetEntry> sets = new ArrayList<>();
        public int doneCount() { int n=0; for(SetEntry s:sets) if(s.done) n++; return n; }
    }
    public static class SetEntry {
        public int reps;
        public double weight;
        public boolean done;
        public boolean warmup;
    }
    public double volume() {
        double sum = 0;
        for (Entry e : entries) for(SetEntry s : e.sets) if(s.done) sum += s.weight * s.reps;
        return sum;
    }
    public int completedExercises() {
        int count = 0;
        for (Entry e : entries) if (e.doneCount() > 0) count++;
        return count;
    }
}
