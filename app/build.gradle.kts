import java.util.Properties

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.roborazzi)
  alias(libs.plugins.secrets)
}

android {
  namespace = "com.gigverse.app"
  compileSdk = 36

  defaultConfig {
    applicationId = "com.gigverse.app"
    minSdk = 24
    targetSdk = 36
    versionCode = 7
    versionName = "1.3.1"

    val envFile = file("${rootDir}/.env")
    val envProps = Properties()
    if (envFile.exists()) {
      envFile.inputStream().use { envProps.load(it) }
    }

    val brevoKey = envProps.getProperty("BREVO_API_KEY") ?: System.getenv("BREVO_API_KEY") ?: ""
    val smtpHost = envProps.getProperty("SMTP_HOST") ?: System.getenv("SMTP_HOST") ?: "smtp-relay.brevo.com"
    val smtpPort = envProps.getProperty("SMTP_PORT") ?: System.getenv("SMTP_PORT") ?: "587"
    val smtpUser = envProps.getProperty("SMTP_USER") ?: System.getenv("SMTP_USER") ?: ""
    val smtpPass = envProps.getProperty("SMTP_PASS") ?: System.getenv("SMTP_PASS") ?: ""
    val smtpFromEmail = envProps.getProperty("SMTP_FROM_EMAIL") ?: System.getenv("SMTP_FROM_EMAIL") ?: "godfrey.cs23@krct.ac.in"
    val smtpFromName = envProps.getProperty("SMTP_FROM_NAME") ?: System.getenv("SMTP_FROM_NAME") ?: "GIGVERSE Security !"

    val localPropsFile = rootProject.file("local.properties")
    val localProps = Properties()
    if (localPropsFile.exists()) {
      localPropsFile.inputStream().use { localProps.load(it) }
    }
    val rawMongoUri = (localProps.getProperty("MONGODB_URI")
        ?: localProps.getProperty("mongodb.uri")
        ?: envProps.getProperty("MONGODB_URI")
        ?: System.getenv("MONGODB_URI")
        ?: "").trim().removeSurrounding("\"").removeSurrounding("'")
    val rawMongoDatabase = (localProps.getProperty("MONGODB_DATABASE")
        ?: localProps.getProperty("mongodb.database")
        ?: envProps.getProperty("MONGODB_DATABASE")
        ?: "gigverse").trim().removeSurrounding("\"").removeSurrounding("'")

    buildConfigField("String", "BREVO_API_KEY", "\"${brevoKey}\"")
    buildConfigField("String", "SMTP_HOST", "\"${smtpHost}\"")
    buildConfigField("String", "SMTP_PORT", "\"${smtpPort}\"")
    buildConfigField("String", "SMTP_USER", "\"${smtpUser}\"")
    buildConfigField("String", "SMTP_PASS", "\"${smtpPass}\"")
    buildConfigField("String", "SMTP_FROM_EMAIL", "\"${smtpFromEmail}\"")
    buildConfigField("String", "SMTP_FROM_NAME", "\"${smtpFromName}\"")
    buildConfigField("String", "MONGODB_URI", "\"${rawMongoUri}\"")
    buildConfigField("String", "MONGODB_DATABASE", "\"${rawMongoDatabase}\"")

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  signingConfigs {
    create("release") {
      val keystorePath = System.getenv("KEYSTORE_PATH") ?: "${rootDir}/my-upload-key.jks"
      storeFile = file(keystorePath)
      storePassword = System.getenv("STORE_PASSWORD") ?: "gigverse2026pass"
      keyAlias = System.getenv("KEY_ALIAS") ?: "upload"
      keyPassword = System.getenv("KEY_PASSWORD") ?: "gigverse2026pass"
      enableV1Signing = true
      enableV2Signing = true
    }
    create("debugConfig") {
      val debugKeystore = if (file("${rootDir}/my-upload-key.jks").exists()) "${rootDir}/my-upload-key.jks" else "${rootDir}/debug.keystore"
      storeFile = file(debugKeystore)
      storePassword = System.getenv("STORE_PASSWORD") ?: "gigverse2026pass"
      keyAlias = System.getenv("KEY_ALIAS") ?: "upload"
      keyPassword = System.getenv("KEY_PASSWORD") ?: "gigverse2026pass"
    }
  }

  buildTypes {
    release {
      isCrunchPngs = true
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.getByName("release")
      buildConfigField("boolean", "IS_PRODUCTION", "true")
      buildConfigField("String", "ENVIRONMENT", "\"PRODUCTION\"")
      buildConfigField("boolean", "ENABLE_ANALYTICS", "true")
      buildConfigField("boolean", "ENABLE_MOCK_SEEDING", "false")
    }
    debug {
      signingConfig = signingConfigs.getByName("debugConfig")
      buildConfigField("boolean", "IS_PRODUCTION", "false")
      buildConfigField("String", "ENVIRONMENT", "\"DEVELOPMENT\"")
      buildConfigField("boolean", "ENABLE_ANALYTICS", "false")
      buildConfigField("boolean", "ENABLE_MOCK_SEEDING", "true")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
  kotlinOptions {
    jvmTarget = "17"
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  testOptions {
    unitTests {
      isIncludeAndroidResources = true
      all {
        it.jvmArgs(
          "--add-opens=java.base/sun.net.ext=ALL-UNNAMED",
          "--add-opens=java.base/java.lang=ALL-UNNAMED",
          "--add-opens=java.base/java.util=ALL-UNNAMED"
        )
      }
    }
  }
  dependenciesInfo {
    includeInApk = false
    includeInBundle = true
  }
  packaging {
    resources {
      excludes += "META-INF/native-image/**"
      excludes += "META-INF/{AL2.0,LGPL2.1}"
    }
  }
}

// Configure the Secrets Gradle Plugin to use .env and .env.example files
// to match the convention used in Web projects.
secrets {
  propertiesFileName = ".env"
  defaultPropertiesFileName = ".env.example"
  ignoreList.add("FIREBASE_APPCHECK_DEBUG_TOKEN")
  ignoreList.add("MONGODB_URI")
  ignoreList.add("MONGODB_DATABASE")
  ignoreList.add("MONGODB_USERNAME")
  ignoreList.add("MONGODB_PASSWORD")
}

// Some unused dependencies are commented out below instead of being removed.
// This makes it easy to add them back in the future if needed.
dependencies {
  implementation(platform(libs.androidx.compose.bom))
  // implementation(libs.accompanist.permissions)
  implementation(libs.androidx.activity.compose)
  // implementation(libs.androidx.camera.camera2)
  // implementation(libs.androidx.camera.core)
  // implementation(libs.androidx.camera.lifecycle)
  // implementation(libs.androidx.camera.view)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.material)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  // implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  implementation(libs.coil.compose)
  implementation(libs.converter.moshi)
  // Uncomment to use Firestore:
  // implementation(libs.firebase.firestore)

  // Uncomment ALL FOUR of the following dependencies together to use Firebase Auth and Google
  // Sign-In via Credential Manager:
  // implementation(libs.firebase.auth)
  // implementation(libs.androidx.credentials)
  // implementation(libs.androidx.credentials.play.services)
  // implementation(libs.googleid)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.logging.interceptor)
  implementation(libs.moshi.kotlin)
  implementation(libs.okhttp)
  // implementation(libs.play.services.location)
  implementation(libs.retrofit)
  implementation("org.mongodb:mongodb-driver-sync:5.3.1")
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)
  testImplementation(libs.roborazzi.junit.rule)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  ksp(libs.androidx.room.compiler)
  ksp(libs.moshi.kotlin.codegen)
}
