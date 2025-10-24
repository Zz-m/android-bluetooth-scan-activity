import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
}

android {
    namespace = "cn.denghanxi.android_bluetooth_scan.lib"
    compileSdk = 36

    buildFeatures {
        viewBinding = true
    }

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
    }
    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_17
        }
    }
}

dependencies {

    //Kotlin coroutine
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")

    //Log
    implementation("org.slf4j:slf4j-api:2.0.17")

    //Swipe refresh
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.13.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.fragment:fragment-ktx:1.8.9")

    //test
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
}

//publish to maven central
//extra.apply {
//    set("PUBLISH_GROUP_ID", "cn.denghanxi")
//    set("PUBLISH_ARTIFACT_ID", "android-bluetooth-scan-activity")
//    set("PUBLISH_VERSION", "0.0.3")
//}
//apply(from = "${rootProject.projectDir}/scripts/publish-module.gradle")

//publish to jitpack (local test)
afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])

                groupId = "com.github.Zz-m"
                artifactId = "android-bluetooth-scan-activity"
                version = "0.0.9"
            }
        }
    }
}
