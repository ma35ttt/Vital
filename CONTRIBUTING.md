# Contributing to Vital

Thanks for taking the time to contribute. Vital is a small, opinionated Android app, so a
little context goes a long way. This guide covers the development loop, the code style and
what a good pull request looks like.

By participating, you agree to follow our [Code of Conduct](CODE_OF_CONDUCT.md).

## Ways to contribute

- **Report a bug** — use the bug report issue template. Include the app version, Android
  version, device model and clear reproduction steps.
- **Request a feature** — use the feature request template. Describe the problem before the
  solution; it is easier to agree on the outcome than on the implementation.
- **Improve the docs** — setup notes, screenshots and architecture explanations are always
  welcome.
- **Send code** — open an issue first for anything larger than a typo or a one-line fix, so
  the approach can be agreed on before you spend time on it.

## Development setup

1. Install JDK 17 and Android Studio (Ladybug or newer) with Android SDK 36.
2. Clone the repository and open it in Android Studio.
3. Build and run:

   ```bash
   ./gradlew assembleDebug
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

4. Firebase is optional. The app builds and runs without `app/google-services.json`; the
   account section simply reports that sign-in is not configured. See
   [docs/FIREBASE_SETUP.md](docs/FIREBASE_SETUP.md) if you need it.

## Project conventions

- **Kotlin first.** Prefer small, testable functions and immutable state.
- **Compose UI.** Keep screens stateless where practical; hoist state into a ViewModel.
- **Design tokens.** Use `VitalColors`, `VitalFonts` and the theme tokens instead of raw
  colours or font families. Do not hardcode hex values in screens.
- **Room is the source of truth.** A write goes to Room first; sync is a background concern.
- **Offline is the default.** Every screen must work with no network.
- **Strings.** User-facing text belongs in resources unless it is generated or part of a
  debug-only screen.
- **No secrets.** Never commit `google-services.json`, `keystore.properties`, `*.jks`,
  `*.keystore`, API keys or `local.properties`.
- **Formatting.** Kotlin uses four spaces, a 120-column soft limit and trailing commas in
  multiline argument lists. Keep imports sorted and remove unused ones.

## Branching and commits

- Branch from `main`: `feat/…`, `fix/…`, `docs/…`, `refactor/…`, `chore/…`.
- Keep commits focused. A commit should build and pass tests on its own.
- Write the subject in the imperative mood, under ~72 characters:

  ```
  Lift: keep the rest timer alive when the app is backgrounded

  The foreground service now owns the countdown, so the bottom-nav drain and
  the notification stay in sync after the activity stops.
  ```

- Reference issues in the body (`Closes #42`) when relevant.

## Tests

Run the unit suite before opening a pull request:

```bash
./gradlew testDebugUnitTest
```

Add tests for pure logic (parsers, calculators, adherence, 1RM, PR detection). UI changes
should include a screenshot in the pull request description.

## Pull request checklist

- [ ] The change is focused and explained in the description.
- [ ] `./gradlew testDebugUnitTest` passes.
- [ ] `./gradlew compileDebugKotlin` passes.
- [ ] New logic has tests.
- [ ] Screenshots are attached for visible UI changes.
- [ ] No secrets, keystores or generated build output are included.
- [ ] Docs are updated if behaviour or setup changed.

## Release process

Maintainers release from a signed tag. The full checklist is in
[docs/UPDATES.md](docs/UPDATES.md). Do not change the release signing key without a
migration plan; Android will reject updates signed with a different key.
