plugins {
    id("com.android.application") version "9.5.0" apply false
    id("com.android.test") version "9.5.0" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.10" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.10" apply false
    id("com.google.devtools.ksp") version "2.3.13" apply false
    id("com.google.dagger.hilt.android") version "2.60.1" apply false
    id("androidx.baselineprofile") version "1.5.0" apply false
    id("com.google.gms.google-services") version "4.5.0" apply false
}

allprojects {
    group = "com.vital"
    version = "1.0.0-debug"
}