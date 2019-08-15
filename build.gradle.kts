// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
        google()
        jcenter()
        maven { setUrl("https://jitpack.io") }
        maven { setUrl("https://mirrors.huaweicloud.com/repository/maven/") }
    }
    dependencies {
        classpath("com.android.tools.build:gradle:${property("ANDROID_PLUGIN")}")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${property("KOTLIN_VERSION")}")
        classpath("com.novoda:bintray-release:${property("BINTRAY_RELEASE")}")
        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

allprojects {
    repositories {
        google()
        jcenter()
        maven { setUrl("https://jitpack.io") }
        maven { setUrl("https://mirrors.huaweicloud.com/repository/maven/") }
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
