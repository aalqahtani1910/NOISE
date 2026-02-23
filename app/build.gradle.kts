import org.gradle.api.GradleException

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
}

val mapsApiKeyProvider = providers.gradleProperty("MAPS_API_KEY")
    .orElse(providers.environmentVariable("MAPS_API_KEY"))

val checkReleaseMapsApiKey by tasks.registering {
    group = "verification"
    description = "Ensures MAPS_API_KEY is configured before release builds."
    doLast {
        if (mapsApiKeyProvider.orNull.isNullOrBlank()) {
            throw GradleException(
                "MAPS_API_KEY is missing. Define it in local.properties, gradle.properties, or as the MAPS_API_KEY environment variable before building release."
            )
        }
    }
}

android {
    namespace = "com.example.noise"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.noise"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKeyProvider.orElse("").get()
    }

    tasks.named("preReleaseBuild") {
        dependsOn(checkReleaseMapsApiKey)
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.materialIconsExtended)

    // Added for this project
    implementation(libs.androidx.navigation.compose)
    implementation(libs.google.maps.compose)
    implementation(libs.google.play.services.maps)

    // Firebase
    implementation(platform(libs.firebaseBom))
    implementation(libs.firebaseFirestoreKtx)
    implementation(libs.firebaseMessagingKtx)


    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}