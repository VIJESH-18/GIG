package com.gigverse.app.data.api

import android.os.Build
import android.util.Log
import com.gigverse.app.BuildConfig
import com.gigverse.app.data.api.model.ApiErrorResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

object ApiClient {

    private const val TAG = "GigverseApiClient"
    const val DEFAULT_LOCAL_LAN_IP = "192.168.1.9"
    const val DEFAULT_EMULATOR_HOST = "10.0.2.2"

    @Volatile
    private var customBaseUrl: String? = null

    @Volatile
    private var mockApi: GigverseAuthApi? = null

    val moshi: Moshi by lazy {
        Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    private val okHttpClient: OkHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor { message ->
            // Log only safe HTTP lines, avoiding any credential leaks
            if (!message.contains("password", ignoreCase = true) && !message.contains("authorization", ignoreCase = true)) {
                Log.d(TAG, message)
            }
        }.apply {
            level = HttpLoggingInterceptor.Level.HEADERS
        }

        OkHttpClient.Builder()
            .connectTimeout(12, TimeUnit.SECONDS)
            .readTimeout(18, TimeUnit.SECONDS)
            .writeTimeout(18, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    /**
     * Determines whether the app is executing on an Android Emulator.
     */
    fun isRunningOnEmulator(): Boolean {
        val fingerprint = Build.FINGERPRINT ?: ""
        val model = Build.MODEL ?: ""
        val manufacturer = Build.MANUFACTURER ?: ""
        val hardware = Build.HARDWARE ?: ""
        val product = Build.PRODUCT ?: ""
        val brand = Build.BRAND ?: ""
        val device = Build.DEVICE ?: ""

        return fingerprint.startsWith("generic") ||
                fingerprint.startsWith("unknown") ||
                model.contains("google_sdk", ignoreCase = true) ||
                model.contains("Emulator", ignoreCase = true) ||
                model.contains("Android SDK built for x86", ignoreCase = true) ||
                manufacturer.contains("Genymotion", ignoreCase = true) ||
                (brand.startsWith("generic") && device.startsWith("generic")) ||
                hardware.contains("goldfish", ignoreCase = true) ||
                hardware.contains("ranchu", ignoreCase = true) ||
                product.contains("sdk", ignoreCase = true) ||
                product.contains("google_sdk", ignoreCase = true) ||
                product.contains("sdk_gphone", ignoreCase = true)
    }

    /**
     * Resolves the authoritative backend URL:
     * - If customBaseUrl is set, uses it.
     * - In production builds (IS_PRODUCTION = true), uses BuildConfig.BACKEND_URL (HTTPS).
     * - In development/debug builds:
     *   - If running on emulator, uses 10.0.2.2:5000.
     *   - If running on a physical Android phone, automatically uses the host LAN IP (192.168.1.9:5000).
     */
    fun getBaseUrl(): String {
        customBaseUrl?.let { url ->
            val clean = url.trim()
            val normalized = if (clean.endsWith("/")) clean else "$clean/"
            if (BuildConfig.IS_PRODUCTION) {
                check(normalized.startsWith("https://", ignoreCase = true)) {
                    "Production release customBaseUrl must use HTTPS: $normalized"
                }
            }
            return normalized
        }

        val buildUrl = BuildConfig.BACKEND_URL.trim()

        val resolved = if (BuildConfig.IS_PRODUCTION) {
            check(buildUrl.startsWith("https://", ignoreCase = true)) {
                "FATAL: Production release backend URL must use secure HTTPS! Provided: '$buildUrl'"
            }
            check(!buildUrl.contains("10.0.2.2") &&
                  !buildUrl.contains("192.168.") &&
                  !buildUrl.contains("localhost") &&
                  !buildUrl.contains("127.0.0.1")) {
                "FATAL: Production release cannot use local development addresses! Provided: '$buildUrl'"
            }
            buildUrl
        } else {
            if (buildUrl.contains(DEFAULT_EMULATOR_HOST) && !isRunningOnEmulator()) {
                // Running on physical Android device during local dev: replace 10.0.2.2 with local Wi-Fi LAN IP
                buildUrl.replace(DEFAULT_EMULATOR_HOST, DEFAULT_LOCAL_LAN_IP)
            } else {
                buildUrl
            }
        }

        return if (resolved.endsWith("/")) resolved else "$resolved/"
    }

    fun setBaseUrl(url: String) {
        val clean = url.trim()
        if (BuildConfig.IS_PRODUCTION) {
            check(clean.startsWith("https://", ignoreCase = true)) {
                "Cannot override production backend with non-HTTPS URL: $clean"
            }
        }
        customBaseUrl = clean
        synchronized(this) {
            _cachedRetrofit = null
            _cachedApi = null
        }
    }

    val hasMockAuthApi: Boolean
        get() = mockApi != null

    fun setMockAuthApi(api: GigverseAuthApi?) {
        mockApi = api
    }

    @Volatile
    private var _cachedRetrofit: Retrofit? = null

    @Volatile
    private var _cachedApi: GigverseAuthApi? = null

    fun getRetrofit(): Retrofit {
        return _cachedRetrofit ?: synchronized(this) {
            _cachedRetrofit ?: Retrofit.Builder()
                .baseUrl(getBaseUrl())
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build().also { _cachedRetrofit = it }
        }
    }

    val authApi: GigverseAuthApi
        get() {
            mockApi?.let { return it }
            return _cachedApi ?: synchronized(this) {
                _cachedApi ?: getRetrofit().create(GigverseAuthApi::class.java).also { _cachedApi = it }
            }
        }

    fun parseError(errorBodyString: String?): String {
        if (errorBodyString.isNullOrBlank()) {
            return "An unexpected server error occurred."
        }
        return try {
            val adapter = moshi.adapter(ApiErrorResponse::class.java)
            val parsed = adapter.fromJson(errorBodyString)
            parsed?.error ?: parsed?.message ?: "An error occurred during cloud operation."
        } catch (_: Exception) {
            "An error occurred during cloud operation."
        }
    }

    fun formatNetworkException(e: Exception): String {
        val msg = e.message ?: ""
        return when {
            e is ConnectException || msg.contains("failed to connect", ignoreCase = true) || msg.contains("Connection refused", ignoreCase = true) -> {
                "Unable to connect to GigVerse cloud server (${getBaseUrl()}). Please make sure the backend is running and reachable from your device."
            }
            e is SocketTimeoutException || msg.contains("timeout", ignoreCase = true) -> {
                "Connection to GigVerse cloud server timed out. Please check your network connection."
            }
            e is UnknownHostException || msg.contains("Unable to resolve host", ignoreCase = true) -> {
                "Unable to resolve GigVerse server domain (${getBaseUrl()}). Please verify internet connectivity."
            }
            else -> e.message ?: "Network error. Please check your internet connection and try again."
        }
    }
}
