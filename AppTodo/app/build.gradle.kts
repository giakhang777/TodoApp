plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.apptodo"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.apptodo"
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
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // Các thư viện đã có
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
    implementation("me.relex:circleindicator:2.1.6")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.annotation)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:3.12.0")
    implementation("com.squareup.picasso:picasso:2.71828")
    implementation("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")

    //  Cloudinary
    implementation("com.cloudinary:cloudinary-android:3.0.2")
    implementation("com.cloudinary:cloudinary-android-core:1.38.0")
    implementation("com.cloudinary:cloudinary-android-preprocess:3.0.2")
    implementation("com.cloudinary:cloudinary-android-download:3.0.2")
    implementation("com.cloudinary:cloudinary-android-ui:3.0.2")

    implementation("com.github.bumptech.glide:glide:4.16.0") // Glide cho tải ảnh
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
    implementation ("androidx.work:work-runtime:2.9.0")
    implementation ("androidx.viewpager2:viewpager2:1.0.0")
    implementation ("com.google.guava:guava:30.1-android")

    // https://mvnrepository.com/artifact/com.github.yukuku/ambilwarna
    implementation("com.github.yukuku:ambilwarna:2.0.1")
}
