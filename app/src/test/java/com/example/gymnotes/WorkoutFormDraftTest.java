package com.example.gymnotes;
import org.junit.Test;
import java.util.ArrayList;
import static org.junit.Assert.*;
public class WorkoutFormDraftTest {
    @Test public void recreationPreservesExerciseOrderAndFractionalWeight() {
        ArrayList<ExerciseEntity> source=new ArrayList<>();
        source.add(new ExerciseEntity(0,"Press",3,10,12.5));
        source.add(new ExerciseEntity(0,"Row",4,8,20));
        ArrayList<ExerciseEntity> restored=WorkoutFormDraft.restore(WorkoutFormDraft.encode("owner",source),"owner");
        assertEquals(2,restored.size());assertEquals("Press",restored.get(0).name);
        assertEquals(12.5,restored.get(0).weight,0);assertEquals(3,restored.get(0).sets);
        assertEquals("Row",restored.get(1).name);assertEquals(8,restored.get(1).reps);
    }
    @Test public void anotherAccountDoesNotReceiveUnsavedExercises() {
        ArrayList<ExerciseEntity> source=new ArrayList<>();source.add(new ExerciseEntity(0,"Private",3,8,10));
        assertTrue(WorkoutFormDraft.restore(WorkoutFormDraft.encode("first",source),"second").isEmpty());
        assertTrue(WorkoutFormDraft.restore(null,"first").isEmpty());
    }
}
