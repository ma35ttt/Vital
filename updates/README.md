# Update channel

This folder holds the inputs for the sideload updater.

| File | Purpose |
| --- | --- |
| `release-notes.json` | The bullet list shown in the in-app update dialog. Edit this before tagging a release. |
| `latest.example.json` | A complete manifest example. The release workflow generates the real `latest.json` and attaches it to the GitHub Release. |

## How a release is published

1. Edit `release-notes.json`.
2. Bump `versionCode` and `versionName` in `app/build.gradle.kts`.
3. Tag and push: `git tag v1.2.0 && git push origin v1.2.0`.
4. The **Release** workflow builds the signed APK, computes its SHA-256 and publishes a GitHub
   Release containing the APK and `latest.json`.
5. The app reads `https://github.com/OWNER/REPO/releases/latest/download/latest.json`.

The manifest schema, signing rules and a local test recipe are in
[`docs/UPDATES.md`](../docs/UPDATES.md).

> Never edit a published release's APK in place. Publish a new `versionCode`; the updater only
> moves forward.
