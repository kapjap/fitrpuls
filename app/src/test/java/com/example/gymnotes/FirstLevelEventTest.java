package com.example.gymnotes;

import org.junit.Test;
import static org.junit.Assert.*;

public class FirstLevelEventTest {
    @Test public void loginAndRestoredSessionsHaveNoCreationEvent() {
        assertFalse(new FirstLevelEvent().consume("existing-user"));
    }
    @Test public void successIsConsumedOnlyOnceIncludingRepeatedCallbacks() {
        FirstLevelEvent event = new FirstLevelEvent();
        event.accountCreated("new-user");
        assertTrue(event.consume("new-user"));
        assertFalse(event.consume("new-user"));
        event.accountCreated("new-user");
        assertFalse(event.consume("new-user"));
    }
    @Test public void anotherAccountCannotConsumeTheEvent() {
        FirstLevelEvent event = new FirstLevelEvent();
        event.accountCreated("new-user");
        assertFalse(event.consume("other-user"));
        assertTrue(event.consume("new-user"));
    }
    @Test public void processRestartDoesNotReplayAndRegistrationDoesNotAwardXp() {
        FirstLevelEvent event = new FirstLevelEvent(); event.accountCreated("new-user");
        assertTrue(event.consume("new-user"));
        assertFalse(new FirstLevelEvent().consume("new-user"));
        assertEquals(0, TrainingXp.total(java.util.Collections.emptyList()));
        assertEquals(1, TrainingXp.level(0));
    }
}
