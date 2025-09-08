plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.fake.mzansilingo"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.fake.mzansilingo"
        minSdk = 24
        targetSdk = 34
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
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Firebase BoM - use latest version
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))

    // Firebase modules (versions managed by BoM)
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-database-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")

    // 🔥 FIXED: Firestore with consistent Kotlin DSL syntax
    implementation("com.google.firebase:firebase-firestore-ktx")

    // Google Sign-In for Firebase Auth
    implementation("com.google.android.gms:play-services-auth:21.2.0")

    // Google Play Services Base (required for Google Sign-In)
    implementation("com.google.android.gms:play-services-base:18.5.0")

    // OkHttp for TTS API calls
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}