package com.example.gymnotes;
import org.junit.Test;
import static org.junit.Assert.*;
public class SessionSnapshotTest {
    private SessionSnapshot sample() {
        SessionSnapshot snapshot=new SessionSnapshot();SessionSnapshot.Entry e=new SessionSnapshot.Entry();
        for(int i=0;i<3;i++){SessionSnapshot.SetEntry s=new SessionSnapshot.SetEntry();s.weight=50;s.reps=10;s.done=i!=1;e.sets.add(s);}
        snapshot.entries.add(e);return snapshot;
    }
    @Test public void onlyCompletedSetsContributeToVolume(){assertEquals(1000,sample().volume(),0.001);}
    @Test public void countsExercisesNotSets(){assertEquals(1,sample().completedExercises());}
    @Test public void unmarkedSessionHasNoCompletion(){SessionSnapshot s=sample();for(SessionSnapshot.SetEntry set:s.entries.get(0).sets)set.done=false;assertEquals(0,s.volume(),0);assertEquals(0,s.completedExercises());}
    @Test public void warmupsContributeToWorkVolume(){SessionSnapshot s=sample();s.entries.get(0).sets.get(0).warmup=true;assertEquals(1000,s.volume(),0);}
    @Test public void individualSetValuesAreUsed(){SessionSnapshot s=sample();s.entries.get(0).sets.get(2).weight=60;s.entries.get(0).sets.get(2).reps=5;assertEquals(800,s.volume(),0);}
}
