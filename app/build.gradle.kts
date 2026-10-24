plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.zeeshanbhatti.puzzlegame"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.zeeshanbhatti.puzzlegame"
        minSdk = 24
        targetSdk = 36
        versionCode = 10
        versionName = "1.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
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

    // Optional: modern build features (no deprecated flags)
    buildFeatures {
        viewBinding = true
        compose = false
    }
}

dependencies {
    // Core & UI
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.activity:activity-ktx:1.8.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")

    // Google Play Ads
    implementation("com.google.android.gms:play-services-ads:23.0.0")

    // Unit testing
    testImplementation("junit:junit:4.13.2")

}