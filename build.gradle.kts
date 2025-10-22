// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.13.0" apply false
    id("com.android.library") version "8.13.0" apply false

    id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
    id("org.jetbrains.kotlin.android") version "2.2.20" apply false
}

//publish to maven central
//apply(from = "${rootDir}/scripts/publish-root.gradle")