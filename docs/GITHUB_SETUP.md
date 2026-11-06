# GitHub repository setup

A short checklist for making the repository look and behave like a maintained project.
Nothing here is required to build the app; it is the public-facing polish.

## 1. Create the repository

- Name: **Vital**
- Visibility: **public**. The in-app updater downloads release assets without credentials,
  and GitHub requires authentication for assets on a private repository. Keep the source
  private only if you host `latest.json` and the APK somewhere publicly reachable.
- Do **not** initialise with a README, `.gitignore` or license — this repository already has
  them.

Suggested description:

> A local-first Android fitness tracker for food, lifting and cardio — Kotlin, Jetpack Compose,
> Room and optional Firebase sync.

Suggested topics:

```
android kotlin jetpack-compose material3 fitness health room hilt firebase
offline-first local-first material-you android-app
```

## 2. About panel

- **Website:** leave blank or link to a release page.
- **Topics:** paste the list above.
- **Social preview:** upload [`docs/assets/social-preview.png`](assets/social-preview.png)
  (1280×640).

## 3. Features to enable

In **Settings → General**:

- Issues
- Discussions (optional, useful for questions and ideas)
- Sponsorships (optional)
- Preserve this repository (optional)

In **Settings → Code and automation → Actions**:

- Allow all actions and reusable workflows.

In **Settings → Security**:

- Private vulnerability reporting: **on** (matches `SECURITY.md`).
- Dependabot alerts: **on**.
- Dependabot security updates: **on**.
- CodeQL: optional; the default Android query pack can be added later.

In **Settings → Branches**:

- Protect `main`.
- Require a pull request before merging.
- Require status checks to pass: `build`.
- Require branches to be up to date before merging.
- Do not allow force pushes.

## 4. Release signing secrets

The release workflow expects four repository secrets. Add them in
**Settings → Secrets and variables → Actions → New repository secret**.

| Secret | Value |
| --- | --- |
| `KEYSTORE_BASE64` | `base64 -i vital-release.jks \| pbcopy` (macOS) or `base64 -w0 vital-release.jks` (Linux) |
| `KEYSTORE_PASSWORD` | the keystore password |
| `KEY_ALIAS` | the key alias, e.g. `vital` |
| `KEY_PASSWORD` | the key password |

The workflow writes `keystore.properties` in the runner, builds `assembleRelease` and uploads
the signed APK plus `latest.json` to the GitHub Release. The keystore itself is never
committed.

> Keep a copy of the keystore and passwords outside GitHub. Losing them means users cannot
> receive updates without uninstalling first.

## 5. CI badge

The README uses a static CI badge so it renders before the repository exists. Once Actions has
run at least once, replace it with the live badge:

```markdown
[![Android CI](https://github.com/OWNER/REPO/actions/workflows/android.yml/badge.svg)](https://github.com/OWNER/REPO/actions/workflows/android.yml)
```

Replace `OWNER/REPO` with the real path.

## 6. First release

1. Make sure the app builds: `./gradlew testDebugUnitTest assembleDebug`.
2. Set `vital.updateManifestUrl` in `local.properties` to
   `https://github.com/OWNER/REPO/releases/latest/download/latest.json`.
3. Bump the version if needed.
4. Commit, tag and push:

   ```bash
   git tag v1.1.0
   git push origin v1.1.0
   ```

5. Open **Releases** and check that the APK and `latest.json` were attached.
6. Install the release APK on a device over the previous build. Confirm the signature and that
   the data survives.

## 7. Repository files

| File | Purpose |
| --- | --- |
| `README.md` | Project overview, screenshots, setup and links |
| `LICENSE` | MIT license |
| `CONTRIBUTING.md` | Development loop, style and PR checklist |
| `CODE_OF_CONDUCT.md` | Contributor Covenant 2.1 |
| `SECURITY.md` | Private vulnerability reporting and key handling |
| `CHANGELOG.md` | Human-readable release history |
| `.github/ISSUE_TEMPLATE/` | Bug report and feature request forms |
| `.github/pull_request_template.md` | PR checklist |
| `.github/workflows/android.yml` | Build + unit tests on pushes and PRs |
| `.github/workflows/release.yml` | Signed APK + `latest.json` on version tags |
| `.github/dependabot.yml` | Weekly Gradle and Actions updates |
| `.editorconfig` | Consistent whitespace across editors |

## 8. Optional polish

- Add a `docs/` index page that links architecture, setup and measurements.
- Add screenshots to the release notes for visual changes.
- Keep `CHANGELOG.md` current; the release workflow can use it as a starting point for notes.
- Pin the Gradle wrapper and keep Dependabot PRs small and reviewed.
