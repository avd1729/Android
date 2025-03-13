// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}

// For repository declarations in Kotlin DSL
buildscript {
    repositories {
        google()
        mavenCentral()
    }
}

// Remove the allprojects block as it's causing the conflict with settings.gradle.kts
// The repositories are now defined in settings.gradle.kts

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}