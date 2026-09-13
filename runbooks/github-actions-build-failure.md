# Incident: GitHub Actions APK Build Failure

## Purpose

To diagnose and fix failures in the `.github/workflows/build.yml` pipeline that prevent the automated generation of the Android Debug APK.

## Impact

Developers and testers cannot download the latest compiled `.apk` artifact from GitHub. No users are directly impacted in the production app, but deployment of new features is blocked.

## Symptoms

- The GitHub Actions tab shows a red "X" for the "Build Android APK" workflow.
- Email notification from GitHub stating the run failed.
- The `app-debug.apk` artifact is missing from the workflow summary.

## Severity

P2 — Internal deployment blockage. Does not affect live users.

## Immediate Actions

Review the GitHub Actions console logs to pinpoint which step failed.

## Diagnosis

1. Open the GitHub Repository > Actions > "Build Android APK".
2. Click on the failed run.
3. Expand the failing step (usually `Build Debug APK`).
4. Look for common Gradle errors:
   - `Compilation error (Syntax)`: A recent commit introduced invalid Kotlin code.
   - `Unresolved reference`: A missing import or dependency.
   - `gradle: not found`: The environment does not have Gradle installed (this workflow currently uses `gradle/actions/setup-gradle@v3` and runs `gradle assembleDebug` because there is no Gradle Wrapper).
   - `Resource not found`: An XML file, string, or icon is missing.

## Recovery

1. Identify the compile-time error from the logs.
2. Fix the error locally or via AI Studio.
3. Verify the fix builds successfully:
   ```bash
   gradle assembleDebug
   ```
4. Push the commit to the `main` branch.
5. The GitHub Action will trigger automatically and rebuild.

## Validation

1. Navigate to the GitHub Actions tab.
2. Ensure the latest workflow run completes with a green checkmark.
3. Scroll to the bottom of the workflow summary and confirm the `app-debug` artifact is available for download.

## Rollback

If a complex feature broke the build and cannot be fixed immediately:
1. Revert the offending commit via git: `git revert <commit-hash>`.
2. Push the revert. The build should turn green again.

## Escalation

If the build is failing due to GitHub Runner environment changes (e.g., JDK 17 deprecation, Ubuntu latest updates), escalate to a DevOps engineer to update the `.github/workflows/build.yml` configuration.

## Do Not

- Do not commit `.gradle/` or `build/` directories to the repository to "fix" caching issues.
- Do not bypass the CI/CD system by manually distributing APKs via email without version control.

## Root Cause Follow-Up

Consider adding a Pull Request workflow that runs `gradle test` and `gradle lint` before code is merged into `main`, preventing broken code from triggering the main build failure.
