# Firebase setup (Google sign-in + Firestore sync)

The app builds and runs **without** a Firebase project: sign-in is disabled and the
Settings → Account section shows "Sign-in is not set up yet". Firebase is switched on
automatically the moment `app/google-services.json` exists.

## 1. Create the project and app

1. Create a Firebase project (Spark / free plan — no card required).
2. Project settings → **Add app** → **Android**.
   - Android package name: `com.vital` (must match `applicationId` exactly).
   - Nickname: `Vital`.

## 2. Register the signing fingerprints

Google sign-in checks the app's signing certificate, so every key you sign with must be
registered. Add at least the debug key (the debug build is signed with it):

| Type | Value |
| --- | --- |
| SHA-1 | `A0:FF:6F:75:E3:41:6D:74:68:27:35:4C:E3:FB:07:D1:02:76:DC:9A` |
| SHA-256 | `1A:B2:33:58:97:A9:DD:0C:F0:AC:F7:0F:AC:4B:8E:EE:FB:73:94:28:77:B3:C7:A4:3C:A7:36:31:85:C3:2A:D0` |

Get the fingerprint of your own debug keystore any time with:

```sh
keytool -list -v -keystore ~/.android/debug.keystore \
  -alias androiddebugkey -storepass android -keypass android | grep SHA1
```

If you later ship a release build signed with an upload/app-signing key, add that key's
SHA-1 too, or sign-in will fail in release.

## 3. Download `google-services.json`

Save it to `app/google-services.json` (it is gitignored). The Gradle build detects it and
applies the `com.google.gms.google-services` plugin; without it the plugin is skipped so
the build stays green.

## 4. Enable Google sign-in

Authentication → **Get started** → **Sign-in method** → **Google** → **Enable**, then choose
a project support email. Enabling it lets the `google-services` plugin surface the web
client id as `@string/default_web_client_id`, which the sign-in code reads automatically.

You do **not** need to fill `google_web_client_id` in `res/values/strings.xml` unless you
want to point at a specific OAuth web client.

## 5. Create Firestore

Firestore Database → **Create database** → start in **production mode**. Apply rules that
scope every document to its owner:

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{uid}/{document=**} {
      allow read, write: if request.auth != null && request.auth.uid == uid;
    }
  }
}
```

Each user's data lives under `users/{uid}/<table>/<rowId>` as
`{ json, updatedAt, deleted }`, so these rules keep every account isolated.

Do not enable Cloud Storage / Blaze — only Firestore (text data) is used.
