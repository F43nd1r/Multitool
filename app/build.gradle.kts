plugins {
    id("com.android.application")
    id("com.github.triplet.play")
    id("com.faendir.gradlekeepass") version "0.1"
    id("idea")
}
apply(plugin = "org.jetbrains.kotlin.android")
apply(plugin = "org.jetbrains.kotlin.android.extensions")
val versionCode: String by project
var vCode = versionCode
val version: String by project
val androidKeyStoreFile: String by project
val playKeyFile: String by project

android {
    compileSdkVersion(28)

    defaultConfig {
        applicationId = "com.faendir.lightning_launcher.multitool"
        minSdkVersion(22)
        targetSdkVersion(28)
        versionCode = Integer.parseInt(vCode)
        versionName = version
    }
    signingConfigs {
        create("release") {
            storeFile = file(androidKeyStoreFile)
            val login = keepass.getLogin("intellij.android.key")
            storePassword = login.Password
            keyAlias = login.Login
            keyPassword = login.Password
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
        getByName("debug") {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }
    lintOptions {
        isAbortOnError = false
        setLintConfig(file("lint.xml"))
    }
    compileOptions {
        setSourceCompatibility(JavaVersion.VERSION_1_8)
        setTargetCompatibility(JavaVersion.VERSION_1_8)
    }
}

play {
    serviceAccountCredentials = file(playKeyFile)
    track = if (version.contains("b")) "beta" else "production"
}

dependencies {
    implementation("com.faendir.lightninglauncher:scriptlib:4.1.1")
    implementation("androidx.appcompat:appcompat:1.1.0")
    implementation("androidx.recyclerview:recyclerview:1.0.0")
    implementation("androidx.preference:preference:1.1.0")
    implementation("com.google.android.material:material:1.0.0")
    implementation("com.google.code.gson:gson:2.8.5")
    implementation("ch.acra:acra-http:5.3.0")
    implementation("ch.acra:acra-limiter:5.3.0")
    implementation("ch.acra:acra-advanced-scheduler:5.3.0")
    implementation("org.greenrobot:eventbus:3.1.1")
    implementation("com.anjlab.android.iab.v3:library:1.0.44")
    implementation("com.mikepenz:materialdrawer:6.0.9")
    implementation("com.mikepenz:fastadapter-extensions:3.2.8")
    implementation("net.sourceforge.streamsupport:android-retrostreams:1.6.3")
    implementation("org.mozilla:rhino:1.7.10")
    implementation("com.evernote:android-job:1.2.6")
    compileOnly("com.google.auto.service:auto-service-annotations:1.0-rc6")
    annotationProcessor("com.google.auto.service:auto-service:1.0-rc6")
    implementation("androidx.constraintlayout:constraintlayout:1.1.3")
    implementation("org.apache.commons:commons-lang3:3.8.1")
    implementation("com.google.guava:guava:27.1-android")
}

tasks.register("increaseVersionNumber") {
    doLast {
        vCode = (Integer.parseInt(vCode) + 1).toString()
        android.defaultConfig.versionCode = Integer.parseInt(vCode)
        ant.withGroovyBuilder {
            "propertyfile"("file" to "../gradle.properties") {
                "entry"("key" to "versionCode", "value" to vCode)
            }
        }
    }
}


afterEvaluate {
    rootProject.tasks.getByName("confirmReleaseVersion").dependsOn(tasks.getByName("increaseVersionNumber"))
    rootProject.tasks.getByName("beforeReleaseBuild").dependsOn(tasks.getByName("clean"))
    rootProject.tasks.getByName("build").dependsOn(tasks.getByName("build"))
    rootProject.tasks.getByName("afterReleaseBuild").dependsOn(tasks.getByName("publishApkRelease"))
}
repositories {
    mavenCentral()
}