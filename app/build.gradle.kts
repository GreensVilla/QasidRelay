plugins {
    id("com.android.application")
}

android {
    namespace = "org.greensvilla.qasidrelay"
    compileSdk = 35

    defaultConfig {
        applicationId = "org.greensvilla.qasidrelay"
        minSdk = 23
        targetSdk = 35
        versionCode = 3
        versionName = "0.1.2"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test:runner:1.7.0")
}
