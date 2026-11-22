plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.habisin"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.habisin"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            // Rilis dipakai untuk demo: R8 mengecilkan APK dan mempercepat
            // cold start. Ditandatangani dengan debug key supaya bisa dipasang
            // langsung tanpa membuat keystore baru.
            signingConfig = signingConfigs.getByName("debug")
            optimization {
                enable = true
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }

    defaultConfig {
        // Alamat backend tidak ditanam di kode: bisa dioverride lewat
        // gradle.properties / -Phabisin.apiBaseUrl=... tanpa mengubah source.
        val apiBaseUrl = (project.findProperty("habisin.apiBaseUrl") as String?) ?: "http://127.0.0.1:8000/"
        buildConfigField("String", "API_BASE_URL", "\"$apiBaseUrl\"")
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.lottie.compose)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

/**
 * Meneruskan port 8000 dari emulator/perangkat ke backend di laptop secara
 * otomatis setiap kali `installDebug` selesai, jadi tidak perlu menjalankan
 * `adb reverse` manual.
 */
val adbExecutable: String by lazy {
    val fromEnv = System.getenv("ANDROID_HOME") ?: System.getenv("ANDROID_SDK_ROOT")
    val fromProps =
        rootProject.file("local.properties")
            .takeIf { it.exists() }
            ?.readLines()
            ?.firstOrNull { it.trimStart().startsWith("sdk.dir=") }
            ?.substringAfter("sdk.dir=")
            ?.trim()
            ?.replace("\\\\", "/")
            ?.replace("\\:", ":")
            ?.replace("\\", "/")
    val sdk = fromEnv ?: fromProps ?: ""
    val exe = if (System.getProperty("os.name").lowercase().contains("win")) "adb.exe" else "adb"
    if (sdk.isBlank()) "" else "$sdk/platform-tools/$exe"
}

val adbPath: String = adbExecutable
val adbAvailable: Boolean = adbPath.isNotBlank() && rootProject.file(adbPath).exists()

val adbReverse =
    tasks.register<Exec>("adbReverse") {
        group = "install"
        description = "adb reverse tcp:8000 tcp:8000 agar app bisa akses backend lokal"
        notCompatibleWithConfigurationCache("adb reverse bergantung pada perangkat yang sedang terhubung")
        commandLine(adbPath, "reverse", "tcp:8000", "tcp:8000")
        isIgnoreExitValue = true
    }

tasks.matching { it.name == "installDebug" }.configureEach { finalizedBy(adbReverse) }
