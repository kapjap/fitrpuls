package com.example.gymnotes;

/** At-most-once presentation event from createUser success, never from auth state/login. */
final class FirstLevelEvent {
    private String createdUid;
    private boolean consumed;
    void accountCreated(String uid) {
        if (!consumed && createdUid == null && uid != null && !uid.isEmpty()) createdUid = uid;
    }
    boolean consume(String uid) {
        if (consumed || createdUid == null || !createdUid.equals(uid)) return false;
        consumed = true;
        createdUid = null;
        return true;
    }
}
