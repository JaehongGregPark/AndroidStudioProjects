plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    kotlin("kapt")

    // ⭐ 루트에서 선언한 Hilt plugin 사용
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.example.pythonttsmvvmapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.pythonttsmvvmapp"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
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

    // ⭐ Room schema export
    kapt {
        correctErrorTypes = true
    }
}

dependencies {

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.activity:activity-compose:1.9.0")

    // Lifecycle
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    // ⭐ Hilt
    implementation("com.google.dagger:hilt-android:2.51.1")
    kapt("com.google.dagger:hilt-compiler:2.51.1")
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")

    implementation("com.tom-roush:pdfbox-android:2.0.27.0")

}
