# Contributing

Thanks for contributing to AppPerms.

## Development workflow

1. Create a focused branch from the default branch.
2. Make a small, reviewable change.
3. Run:

   ```bash
   ./gradlew :app:testDebugUnitTest :app:assembleDebug
   ```

4. Open a pull request with a concise summary, testing notes, and screenshots for UI changes.

## Engineering expectations

- Keep Package Manager and file I/O work off the main thread.
- Preserve the MVVM and repository boundaries.
- Prefer immutable UI state and `StateFlow`.
- Do not add credentials, signing keys, generated build output, or personal device data.
- Add or update tests for behavior changes.

## Commit messages

Use imperative, specific commit subjects, for example:

```text
Add configurable startup app scope
```
