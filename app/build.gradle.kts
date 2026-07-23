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
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
}
