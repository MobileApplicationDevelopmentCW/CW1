plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "my.foodon.pizzamania"
    compileSdk = 35

    defaultConfig {
        applicationId = "my.foodon.pizzamania"
        minSdk = 24
        targetSdk = 35
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
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Firebase
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)

    // Google Play Services
    implementation(libs.credentials)
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    implementation("com.google.android.material:material:1.5.0")
    implementation(libs.firebase.storage)

    // dependencies for glide
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation(libs.recyclerview)
    implementation(libs.fragment)
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    //lottifiles animations
    implementation("com.airbnb.android:lottie:6.6.6")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
