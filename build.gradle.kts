// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
        jcenter()
        google()
        mavenLocal()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:3.5.2")
        classpath("com.github.triplet.gradle:play-publisher:2.0.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.61")

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}
plugins {
    id("net.researchgate.release") version "2.7.0"
    idea
}

allprojects {
    repositories {
        jcenter()
        google()
        mavenLocal()
    }
}

tasks.register("build") {
}
