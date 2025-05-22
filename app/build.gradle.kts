plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp") version "2.0.0-1.0.21" // Add the KSP plugin and version
    id("dagger.hilt.android.plugin")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.example.jellyfinryan"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.jellyfinryan"
        minSdk = 26
        targetSdk = 36 // Consider updating to 35 if compileSdk is 35, or update compileSdk
        versionCode = 1
        versionName = "1.0"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
        // REMOVE THE UNUSED VARIABLE:
        // var compilerExtensionVersion = "1.5.11"
    }

    buildFeatures {
        compose = true
    }

}
dependencies {
    // Core Android dependencies
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose dependencies
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.ui)
    implementation(libs.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.ui.tooling)
    implementation(libs.androidx.foundation)

    // TV-specific dependencies - using an older version for compatibility
    implementation(libs.androidx.tv.foundation)
    implementation(libs.androidx.tv.material)

    // Dependency Injection - using an older version for better compatibility
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler) // Change from kapt to ksp
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.coil.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.datastore.preferences.rxjava2)
    implementation(libs.androidx.datastore.preferences.rxjava3)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    debugImplementation(libs.ui.tooling)
    implementation(libs.retrofit)
    implementation(libs.converter.gson) // Keep this dependency
    implementation(libs.androidx.media3.exoplayer) // Add Media3 Exoplayer dependency
    implementation(libs.androidx.media3.ui) // Add Media3 UI dependency
}

dependencies {
    implementation(libs.androidx.material3.lint)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)
}