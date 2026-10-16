package com.gigverse.app.data.local

import android.content.Context
import android.content.SharedPreferences
import com.gigverse.app.config.EnvironmentMode
import java.util.UUID

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "gigverse_prod_session_prefs"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_ID = "active_user_id"
        private const val KEY_USER_ROLE = "active_user_role"
        private const val KEY_USER_EMAIL = "active_user_email"
        private const val KEY_AUTH_TOKEN = "auth_bearer_token"
        private const val KEY_LOGIN_TIMESTAMP = "login_timestamp"
        private const val KEY_ENVIRONMENT_MODE = "environment_mode"
        private const val KEY_IS_PROD_PURGED = "is_prod_purged"
        private const val KEY_REMEMBER_ME = "remember_me_preference"
        private const val KEY_SAVED_EMAIL = "saved_login_email"
        private const val KEY_SAVED_ROLE = "saved_login_role"

        // 30-day session TTL before requiring re-authentication
        const val SESSION_VALIDITY_DAYS = 30L
        const val SESSION_TIMEOUT_MS = SESSION_VALIDITY_DAYS * 24 * 60 * 60 * 1000L
    }

    fun saveUserSession(userId: String, role: String, email: String, rememberMe: Boolean = true) {
        val editor = prefs.edit()
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.putString(KEY_USER_ID, userId)
        editor.putString(KEY_USER_ROLE, role)
        editor.putString(KEY_USER_EMAIL, email)
        editor.putString(KEY_AUTH_TOKEN, "gv_live_token_" + UUID.randomUUID().toString().take(18))
        editor.putLong(KEY_LOGIN_TIMESTAMP, System.currentTimeMillis())
        editor.putBoolean(KEY_REMEMBER_ME, rememberMe)
        if (rememberMe) {
            editor.putString(KEY_SAVED_EMAIL, email)
            editor.putString(KEY_SAVED_ROLE, role)
        } else {
            editor.remove(KEY_SAVED_EMAIL)
            editor.remove(KEY_SAVED_ROLE)
        }
        editor.apply()
    }

    fun isRememberMeEnabled(): Boolean = prefs.getBoolean(KEY_REMEMBER_ME, false)

    fun setRememberMePreference(enabled: Boolean) {
        val editor = prefs.edit().putBoolean(KEY_REMEMBER_ME, enabled)
        if (!enabled) {
            editor.remove(KEY_SAVED_EMAIL)
            editor.remove(KEY_SAVED_ROLE)
        }
        editor.apply()
    }

    fun getSavedEmail(): String? = prefs.getString(KEY_SAVED_EMAIL, null)

    fun getSavedRole(): String? = prefs.getString(KEY_SAVED_ROLE, null)

    fun isSessionExpired(): Boolean {
        val timestamp = getLoginTimestamp()
        if (timestamp <= 0L) return true
        return (System.currentTimeMillis() - timestamp) > SESSION_TIMEOUT_MS
    }

    fun isLoggedIn(): Boolean {
        val loggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        if (!loggedIn) return false
        if (isSessionExpired()) {
            clearSession()
            return false
        }
        return true
    }

    fun getActiveUserId(): String? = prefs.getString(KEY_USER_ID, null)

    fun getActiveUserRole(): String? = prefs.getString(KEY_USER_ROLE, null)

    fun getActiveUserEmail(): String? = prefs.getString(KEY_USER_EMAIL, null)

    fun getAuthToken(): String? = prefs.getString(KEY_AUTH_TOKEN, null)

    fun getLoginTimestamp(): Long = prefs.getLong(KEY_LOGIN_TIMESTAMP, 0L)

    fun saveEnvironmentMode(mode: EnvironmentMode) {
        prefs.edit().putString(KEY_ENVIRONMENT_MODE, mode.name).apply()
    }

    fun getEnvironmentMode(): EnvironmentMode {
        val saved = prefs.getString(KEY_ENVIRONMENT_MODE, null)
        return try {
            if (saved != null) EnvironmentMode.valueOf(saved) else EnvironmentMode.PRODUCTION
        } catch (e: Exception) {
            EnvironmentMode.PRODUCTION
        }
    }

    fun setProdDataPurged(purged: Boolean) {
        prefs.edit().putBoolean(KEY_IS_PROD_PURGED, purged).apply()
    }

    fun isProdDataPurged(): Boolean = prefs.getBoolean(KEY_IS_PROD_PURGED, false)

    fun clearSession(preserveRememberMePreference: Boolean = true) {
        val editor = prefs.edit()
        editor.remove(KEY_IS_LOGGED_IN)
        editor.remove(KEY_USER_ID)
        editor.remove(KEY_USER_ROLE)
        editor.remove(KEY_USER_EMAIL)
        editor.remove(KEY_AUTH_TOKEN)
        editor.remove(KEY_LOGIN_TIMESTAMP)
        if (!preserveRememberMePreference) {
            editor.remove(KEY_REMEMBER_ME)
            editor.remove(KEY_SAVED_EMAIL)
            editor.remove(KEY_SAVED_ROLE)
        }
        editor.apply()
    }
}
