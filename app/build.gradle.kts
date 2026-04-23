import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.autsing.miga"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.autsing.miga"
        minSdk = 26
        targetSdk = 36
        versionCode = 20100
        versionName = "2.1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {

    implementation(libs.play.services.wearable)
    implementation(platform(libs.compose.bom))
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.compose.material)
    implementation(libs.compose.foundation)
    implementation(libs.wear.tooling.preview)
    implementation(libs.activity.compose)
    implementation(libs.core.splashscreen)
    implementation(libs.tiles)
    implementation(libs.tiles.material)
    implementation(libs.tiles.tooling.preview)
    implementation(libs.protolayout)
    implementation(libs.protolayout.material3)
    implementation(libs.horologist.compose.tools)
    implementation(libs.horologist.compose.layout)
    implementation(libs.horologist.tiles)
    implementation(libs.complications.data.source)
    implementation(libs.serialization.json)
    implementation(libs.qrcode)
    implementation(libs.coil.compose)
    implementation(libs.coil.ktor3)
    implementation(libs.datastore.preferences)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.negotiation)
    implementation(libs.ktor.client.json)
    implementation(libs.ktor.client.logging)
}