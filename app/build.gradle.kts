plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.dagger.hilt.android")
    id("kotlin-kapt")
}

android {
    namespace = "com.example.kusgangaliwas"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.kusgangaliwas"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    // ----------------------------
    // Core Android / Kotlin
    // ----------------------------
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")

    // ----------------------------
    // Jetpack Compose (UI)
    // ----------------------------
    implementation(platform("androidx.compose:compose-bom:2024.09.00"))

    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // ----------------------------
    // Dependency Injection (Hilt)
    // ----------------------------
    implementation("com.google.dagger:hilt-android:2.52")
    kapt("com.google.dagger:hilt-android-compiler:2.52")

    // ----------------------------
    // Hilt + Compose ViewModel
    // ----------------------------
    implementation("androidx.hilt:hilt-lifecycle-viewmodel-compose:1.3.0")

    // ----------------------------
    // Navigation (Compose)
    // ----------------------------
    implementation("androidx.navigation:navigation-compose:2.8.0")

    // ----------------------------
    // ViewModel (AK-style usage)
    // ----------------------------
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")

    // ----------------------------
    // Room (Database)
    // ----------------------------
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")

    kapt("androidx.room:room-compiler:2.6.1")

    // ----------------------------
    // Coroutines
    // ----------------------------
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // ----------------------------
    // Testing (Unit tests first)
    // ----------------------------
    testImplementation("junit:junit:4.13.2")

    // ----------------------------
    // Android Instrumented Tests (later use)
    // ----------------------------
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    androidTestImplementation(platform("androidx.compose:compose-bom:2024.09.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}

kapt {
    correctErrorTypes = true
}