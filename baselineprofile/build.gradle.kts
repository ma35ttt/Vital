plugins {
    id("com.android.test")
    id("androidx.baselineprofile")
}

android {
    namespace = "com.vital.baselineprofile"
    compileSdk = 36

    defaultConfig {
        minSdk = 28
        targetSdk = 36
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    // The module the profile is generated for.
    targetProjectPath = ":app"
    // Macrobenchmark is self-instrumenting: this test APK hosts the runner and drives
    // the target app as a separate process.
    experimentalProperties["android.experimental.self-instrumenting"] = true
}

baselineProfile {
    // Generate on the connected device (a rooted emulator or any API 33+ device).
    useConnectedDevices = true
}

dependencies {
    implementation("androidx.test.ext:junit:1.2.1")
    implementation("androidx.test.uiautomator:uiautomator:2.4.1")
    implementation("androidx.benchmark:benchmark-macro-junit4:1.5.0")
}
