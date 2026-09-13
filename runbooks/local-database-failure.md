# Incident: Local Database Failure (Room)

## Purpose

To diagnose and recover from local data persistence issues, specifically related to the Room database (`medical_reports_db`) failing to initialize, read, or write.

## Impact

Users cannot view their saved `HistoryScreen` reports, or the app crashes immediately upon startup due to a database migration exception.

## Symptoms

- App crashes on launch (visible in crash reporting tools or Logcat).
- `IllegalStateException: A migration from [x] to [y] was required but not found.`
- `HistoryScreen` fails to load saved reports.
- Saving a report from the `AnalysisScreen` silently fails or crashes.

## Severity

P2 — Limited operational issue. It completely degrades the experience for a specific user, but it is a localized device issue, not a global backend outage.

## Immediate Actions

If the user is stuck in a crash loop, they should be advised to Clear App Data from their Android System Settings. (Warning: This deletes their local history).

## Diagnosis

1. Connect device to ADB and check Logcat for Room exceptions:
   ```bash
   adb logcat | grep SQLite
   adb logcat | grep Room
   ```
2. Check if a recent app update modified the `SavedReport` entity or `AppDatabase` schema (e.g., adding the `category` field) but failed to increment the database version properly or provide a migration.

## Recovery

### User-Side Recovery (Destructive)
1. Open Android Settings > Apps > MedRep > Storage.
2. Tap "Clear Data".
3. Relaunch the app. The database will be recreated from scratch.

### Developer-Side Recovery (Code Fix)
If a release caused widespread migration crashes:
1. Open `AppDatabase.kt`.
2. Ensure the `version` number is incremented.
3. Ensure `.fallbackToDestructiveMigration()` is present in the `Room.databaseBuilder` if preserving old data is not strictly required, OR provide a valid `Migration` object.
   *Note: `.fallbackToDestructiveMigration()` is currently implemented.*

## Validation

1. Install the fixed APK.
2. Launch the app.
3. Verify the app does not crash.
4. Scan a new report and save it.
5. Verify the report appears in the `HistoryScreen`.

## Rollback

To rollback a bad release causing DB crashes:
1. Revert the commit that introduced the schema change.
2. Trigger the GitHub Actions build to generate a clean APK.
3. Instruct affected users to downgrade or wait for the patch.

## Escalation

If local database corruption is occurring without schema changes, escalate to investigate potential device storage limitations or threading issues in `ReportRepository`.

## Do Not

- Do not ignore migration paths if users have critical medical data saved that they cannot afford to lose.
- Do not attempt to read `medical_reports_db` directly from a non-rooted device; it is stored in private internal storage.

## Root Cause Follow-Up

Implement automated Room database migration testing to verify schema updates before they are merged to the `main` branch.
