package com.example.gymnotes;
import java.util.*;
/** Idempotent derived XP: 100 per distinct UTC training day, never by load. */
public final class TrainingXp {
    public static long total(List<CompletedWorkoutEntity> history){
        Set<Long> days=new HashSet<>();
        for(CompletedWorkoutEntity w:history)if(w.getExerciseCount()>0 && w.getCompletedAt()>0)
            days.add(w.getCompletedAt()/86400000L);
        return days.size()*100L;
    }
    public static int level(long xp){return (int)Math.min(Integer.MAX_VALUE,1+Math.max(0,xp)/500);}
    private TrainingXp(){}
}
