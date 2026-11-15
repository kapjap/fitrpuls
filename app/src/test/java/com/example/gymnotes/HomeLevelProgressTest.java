package com.example.gymnotes;
import org.junit.Test;
import static org.junit.Assert.*;
public class HomeLevelProgressTest {
    @Test public void emptyHistoryStartsAtOneWithNoProgress() { check(0,1,0,500); }
    @Test public void boundariesMatchExistingLevelFormula() { check(499,1,499,1); check(500,2,0,500); check(1200,3,200,300); }
    @Test public void invalidNegativeXpNeverShowsNegativeProgress() { check(-1,1,0,500); }
    @Test public void maximumLevelDoesNotOverflowNextLevel() { check(Long.MAX_VALUE,Integer.MAX_VALUE,500,0); }
    private void check(long xp,int level,int progress,int remaining) {
        HomeLevelProgress value = new HomeLevelProgress(xp);
        assertEquals(level,value.level); assertEquals(progress,value.progress); assertEquals(remaining,value.remaining);
    }
}
