package com.example.gymnotes;
import org.junit.Test;
import java.util.*;
import static org.junit.Assert.*;
public class TrainingXpTest {
    private CompletedWorkoutEntity w(long day){return new CompletedWorkoutEntity(1,"u","Test",10,100,1,day*86400000L+1);}
    @Test public void sameDayAndDuplicateRecordsDoNotMultiplyXp(){assertEquals(100,TrainingXp.total(Arrays.asList(w(1),w(1),w(1))));}
    @Test public void distinctDaysAccumulate(){assertEquals(200,TrainingXp.total(Arrays.asList(w(1),w(2))));}
    @Test public void levelBoundaries(){assertEquals(1,TrainingXp.level(0));assertEquals(1,TrainingXp.level(499));assertEquals(2,TrainingXp.level(500));}
    @Test public void noActivityNoXp(){assertEquals(0,TrainingXp.total(Collections.emptyList()));}
}
