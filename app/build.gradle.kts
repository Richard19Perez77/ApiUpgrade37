plugins {
    alias(libs.plugins.android.application)
    // The Compose compiler ships with Kotlin itself from 2.0 onward, so its version is
    // tied to the Kotlin version rather than pinned separately. Older projects still
    // carry a composeOptions { kotlinCompilerExtensionVersion } block; this one must not.
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.rick.apiupgrade37"

    // AGP 9 replaced the flat `compileSdk = 37` assignment with this block, which can also
    // express previews and extension levels, for example `version = preview("CinnamonBun")`
    // or `minorVersion`/`sdkExtension` for a minor SDK drop. The old assignment still
    // compiles for now but is on its way out.
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.rick.apiupgrade37"

        // 24 keeps the "how did we do this before?" comments honest: everything in this
        // project has to at least start up on a device nine API levels older than target.
        minSdk = 24

        // targetSdk is what opts you into the Android 17 behavior changes. Bumping this
        // is the moment orientation locks stop working on large screens, MessageQueue goes
        // lock-free, static final freezes, and SMS OTPs get delayed. See BehaviorChangesScreen.
        targetSdk = 37

        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            // AGP 9 renamed the old `isMinifyEnabled` / `isShrinkResources` pair into this
            // block. Kept off so release builds stay readable while learning; a real app
            // should enable it and ship the keep rules in src/main/keepRules/.
            optimization {
                enable = false
            }
        }
    }

    compileOptions {
        // The Java language level of this app's own bytecode. Unrelated to the JDK that
        // runs Gradle, which is pinned to 25 in gradle/gradle-daemon-jvm.properties.
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    // The BOM sets every androidx.compose.* version, so the individual entries below are
    // deliberately version-less. Bump composeBom in libs.versions.toml, not these lines.
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Deliberately absent: navigation-compose and material3-adaptive-navigation-suite.
    // Api37App routes with a `when` over a saved String instead. That keeps this project
    // buildable from the stock BOM with no extra resolution, at the cost of the automatic
    // bottom-bar-to-rail switch a production adaptive app should use. See README.
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
