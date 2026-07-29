plugins {
    id("com.android.application")
}

android {
    namespace = "org.greensvilla.qasidrelay"
    compileSdk = 35
    val releaseKeystorePath = System.getenv("QASIDRELAY_SIGNING_KEYSTORE_PATH")
    val releaseStorePassword = System.getenv("QASIDRELAY_SIGNING_STORE_PASSWORD")
    val releaseKeyAlias = System.getenv("QASIDRELAY_SIGNING_KEY_ALIAS")
    val releaseKeyPassword = System.getenv("QASIDRELAY_SIGNING_KEY_PASSWORD")
    val releaseSigningAvailable = !releaseKeystorePath.isNullOrBlank() &&
        !releaseStorePassword.isNullOrBlank() &&
        !releaseKeyAlias.isNullOrBlank() &&
        !releaseKeyPassword.isNullOrBlank()

    defaultConfig {
        applicationId = "org.greensvilla.qasidrelay"
        minSdk = 23
        targetSdk = 35
        versionCode = 8
        versionName = "0.1.7"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (releaseSigningAvailable) {
            create("release") {
                storeFile = file(releaseKeystorePath!!)
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
                storeType = "pkcs12"
            }
        }
    }

    buildTypes {
        release {
            if (releaseSigningAvailable) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test:runner:1.7.0")
}
