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
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.recyclerview)
    implementation(libs.fragment)

    // Material Design
    implementation("com.google.android.material:material:1.12.0")

    // Firebase (with BoM)
    implementation(platform("com.google.firebase:firebase-bom:33.3.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-functions")

    // Google Play Services
    implementation(libs.credentials)
    implementation("com.google.android.gms:play-services-auth:20.7.0")

    // Glide
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation(libs.play.services.location)
    implementation(libs.play.services.maps)
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // Lottie animations
    implementation("com.airbnb.android:lottie:6.6.6")

    // Circular profile pics
    implementation("de.hdodenhof:circleimageview:3.1.0")

    // Stripe
    implementation("com.stripe:stripe-android:20.49.0")


    implementation("androidx.cardview:cardview:1.0.0")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
