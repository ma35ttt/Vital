import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("androidx.baselineprofile")
    // Applied only when app/google-services.json is present, so the project builds and
    // runs (sign-in disabled) before a Firebase project has been created.
    id("com.google.gms.google-services") apply false
}

// The google-services plugin fails the build if google-services.json is missing, so it is
// applied here only once the file exists. Drop google-services.json into app/ (see the
// README in com/vital/auth) to turn Firebase sign-in and sync on.
val hasGoogleServices = file("google-services.json").exists()
if (hasGoogleServices) {
    apply(plugin = "com.google.gms.google-services")
}

// Where the sideload updater looks for latest.json. Set `vital.updateManifestUrl` in
// local.properties (or pass -Pvital.updateManifestUrl=...), so the real release URL is
// not committed with the source. A blank value simply disables update checks.
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}
val updateManifestUrl: String = (
    providers.gradleProperty("vital.updateManifestUrl").orNull
        ?: localProperties.getProperty("vital.updateManifestUrl")
        ?: providers.environmentVariable("VITAL_UPDATE_MANIFEST_URL").orNull
        ?: ""
    ).trim()

// Optional release signing. Create keystore.properties next to this file with
// storeFile/storePassword/keyAlias/keyPassword to sign release builds with a stable key.
// Without it, release builds fall back to the debug key so local builds stay installable.
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        keystorePropertiesFile.inputStream().use { load(it) }
    }
}

android {
    namespace = "com.vital"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.vital"
        minSdk = 28
        targetSdk = 36
        versionCode = 2
        versionName = "1.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        buildConfigField(
            "String",
            "UPDATE_MANIFEST_URL",
            "\"${updateManifestUrl.replace("\\", "\\\\").replace("\"", "\\\"")}\""
        )
    }
    buildFeatures {
        buildConfig = true
    }
    signingConfigs {
        if (keystorePropertiesFile.exists()) {
            create("release") {
                storeFile = rootProject.file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }
    buildTypes {
        debug {
            isDebuggable = true
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            matchingFallbacks.add("debug")
        }
        release {
            // R8 shrinks and optimizes the app: smaller APK, faster startup, less work on
            // the main thread. The keep rules in proguard-rules.pro cover the reflective
            // edges (Gson models, WorkManager workers, kotlinx.serialization).
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            // Use the stable release key when keystore.properties is present; otherwise
            // fall back to the debug key so local release builds stay installable
            // (including Baseline Profile generation, which has to install the app).
            signingConfig = if (keystorePropertiesFile.exists()) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
            freeCompilerArgs.addAll(
                "-Xopt-in=kotlin.RequiresOptIn",
                "-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
                "-Xopt-in=androidx.lifecycle.ExperimentalLifecycleApi",
                "-Xopt-in=androidx.compose.ui.ExperimentalComposeUiApi",
                "-Xopt-in=androidx.compose.material3.ExperimentalMaterial3Api"
            )
        }
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1,LICENSE,LICENSE.txt,NOTICE,NOTICE.txt}"
        }
    }
}

// Keep the generated Baseline Profile in source so it is reviewed and committed
// alongside the code that produced it.
baselineProfile {
    saveInSrc = true
}

dependencies {
    // Core AndroidX
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.activity:activity-compose:1.9.3")
    // Navigation Compose: explicit 2.8.9 (hilt-navigation-compose only pulls 2.5.1, which predates
    // transition support). Verified on Google Maven; needs Compose 1.7.2 / activity-compose 1.8.0,
    // both already present.
    implementation("androidx.navigation:navigation-compose:2.8.9")
    implementation("androidx.constraintlayout:constraintlayout-compose:1.1.0")
    // Material Components for base theme (required for Compose Material3)
    implementation("com.google.android.material:material:1.12.0")

    // Compose BOM & Material3
    val composeBom = platform("androidx.compose:compose-bom:2024.10.00")
    implementation(composeBom)
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-geometry")
    implementation("androidx.compose.ui:ui-unit")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.runtime:runtime")
    implementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.compose.ui:ui-tooling-preview")

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Hilt
    implementation("com.google.dagger:hilt-android:2.60.1")
    implementation("androidx.hilt:hilt-work:1.2.0")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    implementation("androidx.hilt:hilt-lifecycle-viewmodel:1.4.0")
    ksp("com.google.dagger:hilt-compiler:2.60.1")

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // Biometric
    implementation("androidx.biometric:biometric:1.2.0-alpha04")
    implementation("androidx.biometric:biometric-ktx:1.2.0-alpha04")

    // Security (EncryptedSharedPreferences)
    implementation(libs.androidx.security.crypto)
    ksp(libs.androidx.security.crypto)

    // Gson
    implementation(libs.gson)

    // Network & Serialization
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")
    implementation("io.coil-kt:coil-compose:2.7.0")

    // ML Kit
    implementation("com.google.mlkit:text-recognition:16.0.0")

    // Firebase (Auth + Firestore) for Google sign-in and cloud sync, plus the
    // Credential Manager APIs that drive the Google account picker.
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)

    // PIN hashing - use PBKDF2 via Android Keystore / Security library
    // implementation("org.jetbrains.kotlinx:argon2:2.11") - not available

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.junit.jupiter:junit-jupiter:6.1.3")
    testImplementation("com.google.truth:truth:1.4.5")
    testImplementation("org.robolectric:robolectric:4.13")
    testImplementation("app.cash.turbine:turbine:1.0.0")
    testImplementation("androidx.test.ext:junit:1.2.1")
    testImplementation("androidx.compose.ui:ui-test-junit4")
    testImplementation("androidx.compose.ui:ui-test-manifest")

    // Baseline Profile generator/benchmark tests.
    baselineProfile(project(":baselineprofile"))
}