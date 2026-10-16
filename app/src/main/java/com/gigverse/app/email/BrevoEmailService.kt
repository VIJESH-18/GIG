package com.gigverse.app.email

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Client-side Brevo SMTP/API Email Dispatcher for GIGVERSE.
 * Sends transactional HTML emails directly from the client without requiring a separate backend server.
 */
object BrevoEmailService {

    private const val TAG = "BrevoEmailService"
    private const val BREVO_ENDPOINT = "https://api.brevo.com/v3/smtp/email"
    
    // Environment Configured Brevo Credentials (loaded dynamically from .env)
    val SMTP_HOST: String = com.gigverse.app.BuildConfig.SMTP_HOST
    val SMTP_PORT: Int = com.gigverse.app.BuildConfig.SMTP_PORT.toIntOrNull() ?: 587
    val SMTP_USER: String = com.gigverse.app.BuildConfig.SMTP_USER
    val SMTP_PASS: String = com.gigverse.app.BuildConfig.SMTP_PASS
    val BREVO_API_KEY: String = com.gigverse.app.BuildConfig.BREVO_API_KEY
    val FROM_EMAIL: String = com.gigverse.app.BuildConfig.SMTP_FROM_EMAIL
    val FROM_NAME: String = com.gigverse.app.BuildConfig.SMTP_FROM_NAME

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    /**
     * Sends a transactional HTML email directly via Brevo REST API v3.
     */
    suspend fun sendEmail(
        toEmail: String,
        toName: String,
        subject: String,
        htmlContent: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val senderObj = JSONObject().apply {
                put("name", FROM_NAME)
                put("email", FROM_EMAIL)
            }

            val recipientObj = JSONObject().apply {
                put("email", toEmail.trim())
                put("name", toName.trim().ifBlank { "GIGVERSE User" })
            }

            val toArray = JSONArray().apply {
                put(recipientObj)
            }

            val payload = JSONObject().apply {
                put("sender", senderObj)
                put("to", toArray)
                put("subject", subject)
                put("htmlContent", htmlContent)
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = payload.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url(BREVO_ENDPOINT)
                .post(requestBody)
                .addHeader("accept", "application/json")
                .addHeader("api-key", BREVO_API_KEY)
                .addHeader("content-type", "application/json")
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful) {
                Log.d(TAG, "Email sent successfully: $responseBody")
                val jsonRes = JSONObject(responseBody)
                val msgId = jsonRes.optString("messageId", "Success")
                Result.success(msgId)
            } else {
                Log.e(TAG, "Brevo API Error (${response.code}): $responseBody")
                Result.failure(Exception("Brevo API Error (${response.code}): $responseBody"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send email via Brevo API", e)
            Result.failure(e)
        }
    }
}
