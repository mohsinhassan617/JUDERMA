plugins {
    alias(libs.plugins.android.application)

    id("com.google.gms.google-services") version "4.4.2" apply true
}

android {
    namespace = "com.app.juderma"
    compileSdk = 34



    defaultConfig {
        applicationId ="com.app.juderma"
        minSdk =26
        targetSdk =34
        versionCode =1
        versionName ="1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        androidResources {
            noCompress += "tflite"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

buildscript {
    dependencies {
        classpath(libs.google.services)
    }
}

dependencies {
    implementation (libs.play.services.base)
    implementation (libs.play.services.auth)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(platform(libs.firebase.bom))
    implementation(libs.google.firebase.core)
    implementation(libs.play.services.tasks)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.database)
    implementation(libs.firebase.ml.common)
    implementation(libs.firebase.ml.model.interpreter)
    implementation(libs.androidx.preference)
    implementation(libs.firebase.inappmessaging)
    implementation(libs.cronet.embedded)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.androidx.appcompat.v131)
    implementation(libs.androidx.appcompat.v140)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.pytorch.android)  // Use the appropriate version
    implementation(libs.pytorch.android.torchvision)
    implementation(libs.pytorch.android.v1130)
    implementation(libs.pytorch.android.torchvision.v1130)
    implementation(libs.tensorflow.lite)
    implementation(libs.firebase.analytics)
    implementation(libs.glide)
    annotationProcessor(libs.compiler)
    implementation(libs.tensorflow.lite.support)
    implementation ("androidx.recyclerview:recyclerview:1.2.1")
    implementation ("com.google.android.material:material:1.8.0")
    implementation ("com.squareup.picasso:picasso:2.71828")
    implementation ("com.google.android.gms:play-services-vision:20.0.0") // Or the version you need
    implementation ("org.tensorflow:tensorflow-lite:2.8.0")
    implementation ("org.tensorflow:tensorflow-lite-support:0.2.0")
    implementation ("com.github.bumptech.glide:glide:4.13.2")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.13.2")
    implementation ("androidx.core:core:1.10.0")

}
