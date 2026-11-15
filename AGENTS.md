# Continuation instructions

- Work in `/workspace/fitrpuls` (`kapjap/fitrpuls`). Do not change `/workspace/GymNotes`, remove its data, or change the saved repository selection. Use this checkout; do not create a worktree unless requested.
- Read `docs/PROGRESS.md`, `docs/PRODUCT.md`, `DESIGN_SYSTEM.md`, and `docs/AUDIT.md` before edits. `docs/PRODUCT.md` is the authoritative ANTI-GYM concept.
- Preserve Java, Firebase Auth/Realtime Database, Room, Yandex MapKit, minSdk 24, existing features and user data. Never add destructive Room migration. Preserve applicationId, signing keys and production configuration. Do not publish an app or add real payments without explicit approval.
- Next major phase is ANTI-GYM UI/UX, not more feature modules. Home may have character; active workout must be practical; achievements can be emotional but optional. No copying franchise assets. Normal/Bro/Chaos affect presentation only.
- Inspect working changes before editing. Do not overwrite local work. Keep the v2.0 APK unchanged; its hash is in `docs/baseline-apk.sha256`.
- Read progress notes for the managed JDK/SDK/proxy setup. Run debug build, unit tests, Android-test compilation and lint. Migration device tests are not proven just by compilation. Never bypass TLS/checksum verification.
- Never print embedded API keys, credential files or signed URLs. Production Firebase rules are unavailable; no live data mutations for tests. Cloud sync/account-deletion still have documented gaps.
- Keep documentation honest: distinguish implemented, compiled, unit-tested, device-tested and planned. Do not call this a production-ready release.
- Update `docs/PROGRESS.md` with commands, results, blockers and the precise next task before ending a substantial session.

- Work one screen at a time: finish screen → build/check → commit → next screen. Home and workout list redesign are completed in the 2026-11-15 checkpoint; do not repeat them.
- The welcome-screen stage is complete after active workout. Stop after commit/push and Actions check; await owner phone review. Do not redesign login/registration or other screens without a new request. Runtime visual checks remain pending. Read latest PROGRESS first.
- Never overwrite the owner's `.github/workflows/android.yml`. After each stage: local build/tests → commit → push → check Actions for that exact SHA and report APK build status. Owner downloads `ANTI-GYM-Debug` from Actions; no manual APK delivery.
