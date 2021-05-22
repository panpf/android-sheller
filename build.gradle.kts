// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
        google()
        maven { setUrl("https://mirrors.huaweicloud.com/repository/maven/") }
        mavenCentral()
        maven { setUrl("https://jitpack.io") }
        jcenter()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.2.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${property("KOTLIN_VERSION")}")
        classpath("com.vanniktech:gradle-maven-publish-plugin:0.15.1")
        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

allprojects {
    repositories {
        google()
        maven { setUrl("https://mirrors.huaweicloud.com/repository/maven/") }
        mavenCentral()
        maven { setUrl("https://jitpack.io") }
        jcenter()
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
