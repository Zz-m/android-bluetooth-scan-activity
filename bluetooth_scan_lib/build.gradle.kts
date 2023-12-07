plugins {
    id("com.android.library")
}

android {
    namespace = "cn.denghanxi.android_bluetooth_scan.lib"
    compileSdk = 34

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
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    // rx异步编程
    implementation("io.reactivex.rxjava3:rxandroid:3.0.2")
    implementation("io.reactivex.rxjava3:rxjava:3.1.8")
    // 日志
    implementation("org.slf4j:slf4j-api:2.0.9")

    // 下拉刷新组件
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}

//publish
extra.apply {
    set("PUBLISH_GROUP_ID", "cn.denghanxi")
    set("PUBLISH_ARTIFACT_ID", "android-bluetooth-scan-activity")
    set("PUBLISH_VERSION", "0.0.1")
}

apply(from = "${rootProject.projectDir}/scripts/publish-module.gradle")
