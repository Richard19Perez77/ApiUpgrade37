pluginManagement {
    repositories {
        // The content filters keep plugin resolution from walking Google's repository for
        // artifacts it will never host. Purely a build-speed measure.
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    // Resolves the JDK that the Gradle daemon runs on. gradle/gradle-daemon-jvm.properties
    // asks for Java 25 and lists foojay download URLs per platform, so a machine without
    // that JDK downloads one instead of failing. This is why the README says you do not
    // need to install a JDK yourself, and it is separate from the Java 11 language level
    // the app's own code is compiled to.
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    // FAIL_ON_PROJECT_REPOS rejects any `repositories { }` block inside a module, so every
    // dependency in the build resolves from exactly the two repositories below.
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "ApiUpgrade37"
include(":app")
