
plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.contacthub"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.contacthub"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        manifestPlaceholders["android.max_aspect"] = "2.1"
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

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    //添加Gson依赖
    implementation(libs.gson)
    implementation(libs.monitor)
    implementation(libs.tinypinyin)
    testImplementation(libs.junit)

    // 直接指定zxing-android-embedded的依赖
    implementation("com.journeyapps:zxing-android-embedded:4.3.0") {
        exclude(group = "com.google.zxing", module = "core")
    }
    implementation(libs.core.v352)
    
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
