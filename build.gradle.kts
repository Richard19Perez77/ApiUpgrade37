// Root build file. Plugins are declared here with `apply false` purely to pin their
// versions for the whole build; the :app module is what actually applies them.
// Project-wide configuration that affects the Android build lives in app/build.gradle.kts,
// and dependency versions live in gradle/libs.versions.toml.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
}
