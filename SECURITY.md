# Security policy

## Supported versions

Vital is distributed as a sideloaded Android app. Security fixes are shipped in the latest
release; older builds are not maintained.

| Version | Supported |
| --- | --- |
| Latest release | Yes |
| Older releases | No |

## Reporting a vulnerability

**Please do not open a public issue for a security problem.**

Use GitHub's private vulnerability reporting:

1. Open the repository's **Security** tab.
2. Choose **Report a vulnerability**.
3. Describe the issue and, if possible, include a minimal reproduction.

If private reporting is unavailable, contact the maintainer through the contact method on
their GitHub profile and ask for a private channel. Do not include exploit details in a
public issue, discussion or pull request.

### What to include

- A description of the issue and its impact.
- The app version (`Settings → About`), Android version and device model.
- Steps to reproduce, or a proof of concept.
- Any relevant logs, screenshots or network captures with secrets removed.

### What to expect

- Acknowledgement within 72 hours.
- An initial assessment and a plan within 7 days.
- Credit in the release notes if you want it, once a fix is available.

This is a personal project, so responses are best-effort rather than a commercial SLA.

## Scope

In scope:

- The Android app and its local data handling.
- The optional Firebase Auth / Firestore integration and its rules.
- The in-app update channel: manifest parsing, APK download, checksum verification and the
  hand-off to the system installer.
- Credential storage (AI keys, PIN material) and the export/import path.

Out of scope:

- Issues that require a rooted device or a compromised operating system.
- Social engineering of the maintainer or users.
- Denial of service against third-party services.
- Vulnerabilities in dependencies that are already publicly known and have an upstream fix;
  please report those upstream, but a heads-up is welcome.

## Handling secrets

- Never commit `google-services.json`, `keystore.properties`, `*.jks`, `*.keystore`,
  `local.properties`, API keys or Firebase refresh tokens.
- The release keystore is the trust anchor for updates. If it is lost, users must uninstall
  and reinstall. If it is compromised, rotate it and publish a migration note; do not ship a
  silently re-signed update.
- AI provider keys are stored with Android Keystore-backed encryption and are stripped from
  JSON exports and cloud sync.
