package com.example.gymnotes;

import org.junit.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;

public class WelcomeBackEventTest {
    @Test public void restoredSessionAndNewInstanceDoNotProduceLoginEvent() {
        assertFalse(new WelcomeBackEvent().consume("user"));
    }
    @Test public void loginSuccessIsConsumedOnceForCorrectUser() {
        WelcomeBackEvent event = new WelcomeBackEvent();
        event.signedIn("user");
        assertFalse(event.consume("other"));
        assertTrue(event.consume("user"));
        assertFalse(event.consume("user"));
        event.signedIn("user");
        assertFalse(event.consume("user"));
    }
    @Test public void delayedResultsAndSwitchedAccountsNeverInterruptDashboard() {
        assertTrue(WelcomeBackEvent.canPresent("user", "user", 1500));
        assertFalse(WelcomeBackEvent.canPresent("user", "user", 1501));
        assertFalse(WelcomeBackEvent.canPresent("user", "user", -1));
        assertFalse(WelcomeBackEvent.canPresent("user", "other", 100));
        assertFalse(WelcomeBackEvent.canPresent(null, "user", 100));
    }
    @Test public void existingTrainingHistoryDeterminesLevelWithoutLoginXp() {
        List<CompletedWorkoutEntity> history = new ArrayList<>();
        for (int day = 1; day <= 12; day++)
            history.add(new CompletedWorkoutEntity(1,"user","Test",10,100,1,day*86400000L));
        long before = TrainingXp.total(history);
        WelcomeBackEvent event = new WelcomeBackEvent(); event.signedIn("user"); event.consume("user");
        assertEquals(1200, before);
        assertEquals(3, TrainingXp.level(TrainingXp.total(history)));
        assertEquals(before, TrainingXp.total(history));
    }
}
