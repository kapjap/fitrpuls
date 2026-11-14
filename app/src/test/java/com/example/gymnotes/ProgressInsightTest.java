package com.example.gymnotes;
import org.junit.Test;
import java.util.*;
import static org.junit.Assert.*;
public class ProgressInsightTest {
    @Test public void needsTwoDifferentSessions(){
        assertTrue(ProgressInsight.explain(Arrays.asList(new CompletedExerciseEntity(1,"Press",1,10,20),new CompletedExerciseEntity(1,"Press",1,10,25))).startsWith("Для сравнения"));
    }
    @Test public void doesNotCompareDifferentExercises(){
        assertTrue(ProgressInsight.explain(Arrays.asList(new CompletedExerciseEntity(2,"Squat",1,10,100),new CompletedExerciseEntity(1,"Press",1,10,20))).startsWith("Для сравнения"));
    }
    @Test public void explainsEvidenceAndLimits(){
        String text=ProgressInsight.explain(Arrays.asList(new CompletedExerciseEntity(2,"Press",1,10,25),new CompletedExerciseEntity(1,"Press",1,10,20)));
        assertTrue(text.contains("Press"));assertTrue(text.contains("не рекомендация"));
    }
    @Test public void ignoresWarmups(){
        CompletedExerciseEntity warmup=new CompletedExerciseEntity(2,"Press",1,10,10);warmup.warmup=true;
        assertTrue(ProgressInsight.explain(Arrays.asList(warmup,new CompletedExerciseEntity(1,"Press",1,10,20))).startsWith("Для сравнения"));
    }
}
