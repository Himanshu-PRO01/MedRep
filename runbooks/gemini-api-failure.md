# Incident: Gemini API Analysis Failure

## Purpose

To diagnose and recover from failures when the app attempts to analyze a medical report (text or image) using the Google Gemini API.

## Impact

Users are unable to analyze new medical reports. They will see an error state in the `AnalysisScreen` or `ReportScannerScreen`. Existing saved reports in the local database remain accessible.

## Symptoms

- User sees "Error: Text and API Key are required." or "Image and API Key are required."
- User sees "Error: Failed to parse report."
- API responds with HTTP 400, 401, 403, 429, or 500 status codes (visible in Logcat via OkHttp logging interceptor).
- `AnalysisState.Error(message)` is emitted by `MainViewModel`.

## Severity

P1 — Core feature outage (analysis is the primary function of the app), though offline capabilities remain intact.

## Immediate Actions

1. Check if the user has entered a valid API Key in the Settings screen.
2. Verify if the Gemini API is experiencing a global outage (check Google Cloud status page).

## Diagnosis

1. **Verify API Key**: Check the Settings screen. Ensure the key is present and not expired/revoked.
2. **Check Logs**: Connect the device via USB/Wireless debugging and check Logcat.
   ```bash
   # Filter for network requests (OkHttp)
   adb logcat | grep OkHttp
   ```
   Look for the HTTP response code from `https://generativelanguage.googleapis.com/`:
   - `400 Bad Request`: The prompt format changed or the image is unsupported.
   - `401/403`: Invalid or revoked API key.
   - `429 Too Many Requests`: Rate limiting hit. The user is scanning too many reports too quickly.
   - `500/503`: Google Gemini service is down.

## Recovery

- **For 401/403**: Have the user generate a new Gemini API Key from Google AI Studio and enter it into the app's Settings screen.
- **For 429**: Advise the user to wait a few minutes before trying again.
- **For 500/503**: Wait for Google to resolve the outage.
- **For Parsing Errors ("Failed to parse report")**: If the AI returns malformed JSON, try scanning the report again. If the issue persists, the prompt in `ReportRepository.kt` may need adjustments to strictly enforce JSON output.

## Validation

1. Open the app and navigate to the Scanner/Analysis screen.
2. Provide a sample text or image.
3. Tap "Analyze".
4. Ensure the `AnalysisScreen` displays the parsed Test Results, Urgency, and Patient Summary without error.

## Rollback

No verified rollback mechanism found. (This is a client-side API call failure, not a deployment issue).

## Escalation

If the API is consistently returning malformed JSON despite the system prompt, escalate to a developer to adjust the `GeminiRequest` instructions in `ReportRepository.kt`.

## Do Not

- Do not attempt to reset the local Room database; it is unrelated to API connectivity.
- Do not hardcode an API key into `NetworkModule.kt` as a temporary fix (security risk).

## Root Cause Follow-Up

If parsing failures are common, consider switching to "JSON schema structured outputs" in the Gemini API configuration to guarantee the JSON matches the Moshi `SavedReport` class structure.
