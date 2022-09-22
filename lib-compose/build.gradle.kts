/*
 * Copyright 2021 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

plugins {
    id("com.android.library")
    id("kotlin-android")
}

val composeVersion = rootProject.extra["compose_version"] as String
val PUBLISH_GROUP_ID: String by project
val PUBLISH_VERSION: String by project
val PUBLISH_ARTIFACT_ID by extra("permissions-compose-ktx")

apply(from = "${rootProject.projectDir}/gradle/publish-module.gradle")

android {
    compileSdk = 31

    defaultConfig {
        minSdk = 21
        targetSdk = 31

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        named("release") {
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
        freeCompilerArgs = listOf("-Xinline-classes", "-Xopt-in=kotlin.RequiresOptIn")
    }

    buildFeatures {
        viewBinding = true
        compose = true
        buildConfig = false
    }
    composeOptions {
        kotlinCompilerExtensionVersion = composeVersion
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    api(project(":lib"))
    api("androidx.activity:activity-compose:1.6.0")
}