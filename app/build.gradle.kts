plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.capstone.safehito"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.capstone.safehito"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
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

    // ✅ exclude duplicates from libraries
    packaging {
        resources {
            excludes += "META-INF/versions/**"
            excludes += "META-INF/*.SF"
            excludes += "META-INF/*.DSA"
            excludes += "META-INF/*.RSA"
        }
    }
}



dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.room.compiler)
    implementation(libs.ui.graphics)
    testImplementation(libs.androidx.compose.testing)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Firebase
    implementation (platform("com.google.firebase:firebase-bom:32.7.2"))
    implementation("com.google.firebase:firebase-database-ktx:20.3.0") // or latest
    implementation ("com.google.firebase:firebase-auth-ktx")
    implementation ("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-firestore-ktx")


    implementation ("com.google.android.material:material:1.11.0")
    implementation ("androidx.compose.material3:material3:1.2.1")
    implementation ("androidx.core:core-splashscreen:1.0.1")

    implementation ("androidx.navigation:navigation-compose:2.7.7")
    implementation("com.airbnb.android:lottie-compose:6.0.0")

// Jetpack Compose
    implementation ("androidx.compose.material:material:1.6.0")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")

    implementation ("androidx.compose.material:material-icons-extended:1.6.0")
    implementation("com.google.accompanist:accompanist-swiperefresh:0.33.2-alpha")

    implementation ("com.google.firebase:firebase-messaging:23.4.1") // or latest version
    implementation ("androidx.core:core-ktx:1.10.1")
    implementation ("androidx.work:work-runtime-ktx:2.9.0")

    implementation("io.coil-kt:coil-compose:2.5.0") // or latest version

    implementation ("com.google.accompanist:accompanist-systemuicontroller:0.32.0")

    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.squareup.retrofit2:converter-scalars:2.9.0")
    implementation("com.squareup.retrofit2:adapter-rxjava2:2.9.0") {
        exclude(group = "com.intellij", module = "annotations")
    }
    implementation ("com.squareup.okhttp3:logging-interceptor:4.9.1")
    implementation ("com.squareup.retrofit2:converter-moshi:2.9.0")

    // CameraX core library
    implementation ("androidx.camera:camera-core:1.3.0")
    implementation ("androidx.camera:camera-camera2:1.3.0")

// Lifecycle binding
    implementation ("androidx.camera:camera-lifecycle:1.3.0")

// View for PreviewView
    implementation ("androidx.camera:camera-view:1.3.0")

// For CameraX extensions (optional)
    implementation ("androidx.camera:camera-extensions:1.3.0")

    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    implementation ("androidx.datastore:datastore-preferences:1.0.0")

    implementation ("com.google.accompanist:accompanist-navigation-animation:0.33.0-alpha")

    implementation ("com.google.accompanist:accompanist-systemuicontroller:0.34.0")

    implementation("androidx.compose.ui:ui-text:1.6.0") // or match your Compose version

    implementation(platform("androidx.compose:compose-bom:2023.10.01"))

    implementation ("com.google.accompanist:accompanist-pager:0.34.0")
    implementation ("com.google.accompanist:accompanist-pager-indicators:0.34.0")

    implementation ("androidx.compose.ui:ui-viewbinding:1.6.0") // optional but recommended

    implementation ("androidx.compose.ui:ui")
    implementation ("androidx.compose.ui:ui-tooling")
    implementation ("io.coil-kt:coil-gif:2.5.0")

    implementation("com.github.CanHub:Android-Image-Cropper:4.3.2") {
        exclude(group = "com.intellij", module = "annotations")
    }


    implementation("org.jetbrains:annotations:23.0.0")

}

configurations.all {
    exclude(group = "com.intellij", module = "annotations")
    resolutionStrategy {
        force("org.jetbrains:annotations:23.0.0")
    }
}

