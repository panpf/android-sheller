import com.novoda.gradle.release.PublishExtension
import java.util.*

plugins {
    id("com.android.library")
}

android {
    compileSdkVersion(property("COMPILE_SDK_VERSION").toString().toInt())

    defaultConfig {
        minSdkVersion(property("MIN_SDK_VERSION").toString().toInt())
        targetSdkVersion(property("TARGET_SDK_VERSION").toString().toInt())
        versionCode = property("VERSION_CODE").toString().toInt()
        versionName = property("VERSION_NAME").toString()

        consumerProguardFiles("proguard-rules.pro")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }
}

dependencies {
    testImplementation("junit:junit:${property("JUNIT_VERSION")}")
    androidTestImplementation("androidx.test:runner:${property("ANDROIDX_TEST_RUNNER")}")
    androidTestImplementation("androidx.test:rules:${property("ANDROIDX_TEST_RULES")}")
    androidTestImplementation("androidx.test.ext:junit:${property("ANDROIDX_TEST_JUNIT")}")
    androidTestImplementation("androidx.test.espresso:espresso-core:${property("ANDROIDX_TEST_ESPRESSO")}")
    api("androidx.annotation:annotation:${property("ANDROIDX_ANNOTATION")}")
}

Properties().apply { project.file("local.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) } }.takeIf { !it.isEmpty }?.let { localProperties ->
    apply { plugin("com.novoda.bintray-release") }

    configure<PublishExtension> {
        groupId = "me.panpf"
        artifactId = "sheller"
        publishVersion = property("VERSION_NAME").toString()
        desc = "Android, App, Install"
        website = "https://github.com/panpf/android-sheller"
        userOrg = localProperties.getProperty("bintray.userOrg")
        bintrayUser = localProperties.getProperty("bintray.user")
        bintrayKey = localProperties.getProperty("bintray.apikey")
    }
}