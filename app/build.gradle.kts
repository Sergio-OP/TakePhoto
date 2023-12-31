plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    // Add the Google services Gradle plugin
    id("com.google.gms.google-services")

}

android {
    namespace = "com.example.takephoto"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.example.takephoto"
        minSdk = 23
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.8.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.camera:camera-view:1.2.3")
    implementation("androidx.camera:camera-lifecycle:1.2.3")
    implementation("com.google.firebase:firebase-common-ktx:20.3.3")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation("androidx.camera:camera-core:1.2.3")
    implementation("androidx.camera:camera-camera2:1.2.3")
    implementation("androidx.camera:camera-view:1.2.3")
    implementation("androidx.camera:camera-lifecycle:1.2.3")

    implementation("androidx.concurrent:concurrent-futures-ktx:1.1.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")

    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:32.3.1"))
    implementation("com.google.firebase:firebase-storage-ktx")

    implementation("com.squareup.okhttp3:okhttp:4.9.1")

    implementation ("com.caverock:androidsvg:1.4")

    implementation("com.robotemi:sdk:1.131.4")


}