import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.devtools.ksp)
    alias(libs.plugins.kotlin.serialization)
}

val secretProperties = Properties().apply {
    val secretPropertiesFile = rootProject.file("secret.properties")
    if (secretPropertiesFile.exists()) {
        secretPropertiesFile.inputStream().use {
            load(it)
        }
    }
}

val tailscaleApiUrl = secretProperties.getProperty("tailscale.api.url") ?: "http://10.0.2.2:3000/api/"
val tailscaleSocketUrl = secretProperties.getProperty("tailscale.socket.url") ?: "http://10.0.2.2:3000"

val releaseApiUrl = secretProperties.getProperty("release.api.url") ?: "http://10.0.2.2:3000/api/"
val releaseSocketUrl = secretProperties.getProperty("release.socket.url") ?: "http://10.0.2.2:3000"


android {
    namespace = "com.timdiedrich.rbblfantasy"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.timdiedrich.rbblfantasy"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        compose = true
        buildConfig = true // Enables generated BuildConfig class
    }

    buildTypes {
        debug {
            // Default URL for Android Emulator
            buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:3000/api/\"")
            buildConfigField("String", "SOCKET_URL", "\"http://10.0.2.2:3000\"")
        }

        create("tailscale") {
            initWith(getByName("debug"))
            buildConfigField("String", "BASE_URL", "\"${tailscaleApiUrl.trim('\"')}\"")
            buildConfigField("String", "SOCKET_URL", "\"${tailscaleSocketUrl.trim('\"')}\"")
            matchingFallbacks += listOf("debug")
        }

        release {
            isMinifyEnabled = false
            // Replace with actual production backend URL when deployed
            buildConfigField("String", "BASE_URL", "\"${releaseApiUrl.trim('\"')}\"")
            buildConfigField("String", "SOCKET_URL", "\"${releaseSocketUrl.trim('\"')}\"")
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
}

dependencies {
    implementation(libs.socket.io)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.coil.compose)
    implementation(libs.retrofit)
    implementation(libs.converter.moshi)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.accompanist.permissions)
    implementation(libs.play.services.location)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.camera.core)
    implementation(libs.logging.interceptor)
    implementation(libs.okhttp)
    implementation(libs.moshi.kotlin)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.material)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.core)
    testImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.runner)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    "ksp"(libs.androidx.room.compiler)
    "ksp"(libs.moshi.kotlin.codegen)
}