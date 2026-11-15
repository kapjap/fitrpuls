package com.example.gymnotes;

/** Presentation of the existing 500-XP interval, without awarding or storing XP. */
final class HomeLevelProgress {
    final int level, progress, remaining;
    HomeLevelProgress(long xp) {
        long safeXp = Math.max(0, xp);
        level = TrainingXp.level(safeXp);
        progress = level == Integer.MAX_VALUE ? 500 : (int) (safeXp % 500);
        remaining = 500 - progress;
    }
}
