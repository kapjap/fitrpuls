package com.example.gymnotes;

/** One presentation event per login screen; never produced by restoring Firebase auth. */
final class WelcomeBackEvent {
    static final String SIGNED_IN_UID = "anti_gym_signed_in_uid";
    private String uid;
    private boolean consumed;
    void signedIn(String userId) {
        if (!consumed && uid == null && userId != null && !userId.isEmpty()) uid = userId;
    }
    boolean consume(String userId) {
        if (consumed || uid == null || !uid.equals(userId)) return false;
        consumed = true; uid = null;
        return true;
    }
    static boolean canPresent(String expectedUid, String currentUid, long elapsedMillis) {
        return expectedUid != null && expectedUid.equals(currentUid)
                && elapsedMillis >= 0 && elapsedMillis <= 1500;
    }
}
