package com.gigverse.app

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.gigverse.app.data.local.AppDatabase
import com.gigverse.app.data.local.SessionManager
import com.gigverse.app.data.model.UserEntity
import com.gigverse.app.data.model.UserRole
import com.gigverse.app.data.repository.GigverseRepository
import com.gigverse.app.ui.viewmodel.GigverseViewModel
import com.gigverse.app.ui.viewmodel.ScreenNav
import com.gigverse.app.util.SecurityUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class GigverseLoginFeaturesTest {

    private lateinit var context: Context
    private lateinit var sessionManager: SessionManager
    private lateinit var testDbFile: File
    private var testDb: AppDatabase? = null

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext<Context>()
        sessionManager = SessionManager(context)
        sessionManager.clearSession(preserveRememberMePreference = false)

        testDbFile = File(context.filesDir, "test_gigverse_login_features.db")
        if (testDbFile.exists()) {
            testDbFile.delete()
        }
    }

    @After
    fun tearDown() {
        testDb?.close()
        testDb = null
        if (testDbFile.exists()) {
            testDbFile.delete()
        }
        sessionManager.clearSession(preserveRememberMePreference = false)
    }

    private fun getDb(): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, testDbFile.absolutePath)
            .addMigrations(
                AppDatabase.MIGRATION_1_2,
                AppDatabase.MIGRATION_2_3,
                AppDatabase.MIGRATION_3_4,
                AppDatabase.MIGRATION_1_4,
                AppDatabase.MIGRATION_2_4
            )
            .build()
    }

    private fun awaitCondition(timeoutMs: Long = 8000, condition: () -> Boolean) {
        val start = System.currentTimeMillis()
        while (!condition() && (System.currentTimeMillis() - start) < timeoutMs) {
            org.robolectric.shadows.ShadowLooper.idleMainLooper()
            Thread.sleep(20)
        }
        org.robolectric.shadows.ShadowLooper.idleMainLooper()
    }

    // =========================================================================
    // 1. REMEMBER ME TESTS
    // =========================================================================

    @Test
    fun testRememberMe_preferencePersistenceAndRestore() {
        assertFalse("Remember Me should default to false", sessionManager.isRememberMeEnabled())

        // Enable Remember Me preference
        sessionManager.setRememberMePreference(true)
        assertTrue("Remember Me should be enabled", sessionManager.isRememberMeEnabled())

        // Create new SessionManager instance to verify SharedPreferences persistence
        val newSessionManager = SessionManager(context)
        assertTrue("Remember Me preference must persist across SessionManager instances", newSessionManager.isRememberMeEnabled())

        // Disable Remember Me preference
        newSessionManager.setRememberMePreference(false)
        assertFalse("Remember Me should be disabled", newSessionManager.isRememberMeEnabled())

        val thirdSessionManager = SessionManager(context)
        assertFalse("Remember Me preference (false) must persist across instances", thirdSessionManager.isRememberMeEnabled())
    }

    @Test
    fun testRememberMe_savesEmailWhenEnabled_removesEmailWhenDisabled() {
        val email = "remember.test@gigverse.com"

        // Save session with rememberMe = true
        sessionManager.saveUserSession("user_123", "FREELANCER", email, rememberMe = true)
        assertTrue(sessionManager.isRememberMeEnabled())
        assertEquals(email, sessionManager.getSavedEmail())

        // Save session with rememberMe = false
        sessionManager.saveUserSession("user_456", "CLIENT", "other@gigverse.com", rememberMe = false)
        assertFalse(sessionManager.isRememberMeEnabled())
        assertNull("Saved email must be cleared when Remember Me is false", sessionManager.getSavedEmail())
    }

    @Test
    fun testRememberMe_sessionRestoredAcrossAppRestartWhenEnabled() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm1 = GigverseViewModel(app)

        val email = "session.restore@freelancer.io"
        val password = "SecureSessionPassword2026!"

        vm1.registerFreelancer(
            name = "Session Freelancer",
            email = email,
            passwordRaw = password,
            phone = "+1 555-4321",
            locationCountry = "USA",
            locationState = "CA",
            locationDistrict = "Oakland",
            headline = "Senior Compose Engineer",
            skills = "Compose, Room",
            hourlyRate = 80.0,
            govIdType = "Passport",
            govIdNumber = "US-9999",
            linkedInUrl = "",
            portfolioUrl = "",
            githubUrl = ""
        )
        awaitCondition { vm1.currentScreen.value is ScreenNav.Dashboard }
        val registeredUserId = vm1.currentUserId.value
        assertTrue(registeredUserId.isNotBlank())

        // Enable remember me and save session
        sessionManager.setRememberMePreference(true)
        sessionManager.saveUserSession(registeredUserId, "FREELANCER", email, rememberMe = true)
        assertTrue(sessionManager.isLoggedIn())

        // Simulate app kill and cold restart
        val vm2 = GigverseViewModel(app)
        org.robolectric.shadows.ShadowLooper.idleMainLooper()

        assertEquals("Session should automatically restore user ID on app cold start", registeredUserId, vm2.currentUserId.value)
        assertEquals("Screen should automatically restore to Dashboard", ScreenNav.Dashboard, vm2.currentScreen.value)
    }

    @Test
    fun testRememberMe_temporarySessionNotRestoredWhenDisabled() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm1 = GigverseViewModel(app)

        val email = "temp.session@client.io"
        val password = "TemporaryPassword123!"

        vm1.registerClient(
            name = "Temporary Client",
            company = "Temp Corp",
            email = email,
            passwordRaw = password,
            phone = "+1 555-7766",
            country = "USA",
            state = "WA",
            district = "Seattle",
            industry = "Retail"
        )
        awaitCondition { vm1.currentScreen.value is ScreenNav.Dashboard }
        val registeredUserId = vm1.currentUserId.value
        assertTrue(registeredUserId.isNotBlank())

        // Login with rememberMe = false
        sessionManager.saveUserSession(registeredUserId, "CLIENT", email, rememberMe = false)
        assertFalse("Remember Me should be false", sessionManager.isRememberMeEnabled())

        // Simulate app kill and cold restart
        val vm2 = GigverseViewModel(app)
        org.robolectric.shadows.ShadowLooper.idleMainLooper()

        assertEquals("Temporary session must NOT be restored on cold restart", "", vm2.currentUserId.value)
        assertEquals("User must be redirected to AuthLogin screen", ScreenNav.AuthLogin, vm2.currentScreen.value)
    }

    @Test
    fun testRememberMe_respects30DaySessionExpiryPolicy() {
        val now = System.currentTimeMillis()
        val email = "expiry.test@gigverse.com"

        // Active session within 30 days (e.g. 5 days ago)
        val activeTimestamp = now - (5L * 24 * 60 * 60 * 1000L)
        sessionManager.saveUserSession("user_active", "FREELANCER", email, rememberMe = true)
        val prefs = context.getSharedPreferences("gigverse_prod_session_prefs", Context.MODE_PRIVATE)
        prefs.edit().putLong("login_timestamp", activeTimestamp).commit()

        assertFalse("Session within 30 days must not be expired", sessionManager.isSessionExpired())
        assertTrue("isLoggedIn must return true for active session", sessionManager.isLoggedIn())

        // Expired session older than 30 days (e.g. 31 days ago)
        val expiredTimestamp = now - (31L * 24 * 60 * 60 * 1000L)
        prefs.edit().putLong("login_timestamp", expiredTimestamp).commit()

        assertTrue("Session older than 30 days must be expired", sessionManager.isSessionExpired())
        assertFalse("isLoggedIn must return false for expired session", sessionManager.isLoggedIn())
    }

    @Test
    fun testLogout_clearsActiveSession_preservesRememberMePreferenceAndEmail() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = GigverseViewModel(app)

        val email = "logout.test@domain.com"
        val password = "LogoutTestPassword123!"

        vm.registerClient(
            name = "Logout Client",
            company = "Logout Co",
            email = email,
            passwordRaw = password,
            phone = "+1 555-1234",
            country = "USA",
            state = "CA",
            district = "SF",
            industry = "Design"
        )
        awaitCondition { vm.currentScreen.value is ScreenNav.Dashboard }

        // Set rememberMe = true
        sessionManager.saveUserSession(vm.currentUserId.value, "CLIENT", email, rememberMe = true)
        assertTrue(sessionManager.isLoggedIn())
        assertTrue(vm.isRememberMe())

        // Trigger Logout
        vm.logout()
        org.robolectric.shadows.ShadowLooper.idleMainLooper()

        assertEquals("Logout must navigate to AuthLogin", ScreenNav.AuthLogin, vm.currentScreen.value)
        assertEquals("Logout must clear currentUserId in ViewModel", "", vm.currentUserId.value)
        assertFalse("SessionManager must report logged out", sessionManager.isLoggedIn())
        assertNull("Active user ID must be null after logout", sessionManager.getActiveUserId())

        // Remember Me preference and saved email must be preserved for login screen restoration
        assertTrue("Remember Me preference must be preserved across logout", vm.isRememberMe())
        assertEquals("Saved login email must be preserved for convenience", email, vm.getSavedLoginEmail())

        // Verify password is NOT stored anywhere
        val prefs = context.getSharedPreferences("gigverse_prod_session_prefs", Context.MODE_PRIVATE)
        assertFalse("Password must never be saved in SharedPreferences", prefs.contains("password"))
        assertFalse("Password hash must never be saved in SharedPreferences", prefs.contains("passwordHash"))
    }

    // =========================================================================
    // 2. PASSWORD HASHING & FORGOT PASSWORD TESTS
    // =========================================================================

    @Test
    fun testSecurityUtils_pbkdf2HashingAndSalting() {
        val password = "MySuperSecretPassword#2026"
        val hash1 = SecurityUtils.hashPassword(password)
        val hash2 = SecurityUtils.hashPassword(password)

        assertTrue("PBKDF2 hash must start with 'pbkdf2:'", hash1.startsWith("pbkdf2:10000:"))
        assertTrue("PBKDF2 hash must have 4 segments", hash1.split(":").size == 4)

        // Unique random salt verification: same password produces different hashes
        assertNotEquals("Unique salt must ensure different hash outputs for same password", hash1, hash2)

        // Verification checks
        val verify1 = SecurityUtils.verifyPassword(password, hash1)
        assertTrue("Correct password must verify against hash1", verify1.isValid)

        val verify2 = SecurityUtils.verifyPassword(password, hash2)
        assertTrue("Correct password must verify against hash2", verify2.isValid)

        val wrongVerify = SecurityUtils.verifyPassword("WrongPassword!", hash1)
        assertFalse("Wrong password must fail verification", wrongVerify.isValid)
    }

    @Test
    fun testForgotPassword_successfulResetUpdatesPasswordAndAuthenticates() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = GigverseViewModel(app)

        val email = "forgot.pass@test.com"
        val oldPassword = "OldPassword123!"
        val newPassword = "NewBrandNewPassword2026!"

        vm.registerFreelancer(
            name = "Reset User",
            email = email,
            passwordRaw = oldPassword,
            phone = "+1 555-9090",
            locationCountry = "USA",
            locationState = "CA",
            locationDistrict = "San Jose",
            headline = "Mobile Dev",
            skills = "Android",
            hourlyRate = 60.0,
            govIdType = "ID",
            govIdNumber = "12345",
            linkedInUrl = "",
            portfolioUrl = "",
            githubUrl = ""
        )
        awaitCondition { vm.currentScreen.value is ScreenNav.Dashboard }
        val userId = vm.currentUserId.value

        // Logout to reach login state
        vm.logout()
        org.robolectric.shadows.ShadowLooper.idleMainLooper()
        assertEquals(ScreenNav.AuthLogin, vm.currentScreen.value)

        // Perform password reset
        var resetSuccess = false
        var resetMessage = ""
        vm.resetPassword(
            email = email,
            newPasswordRaw = newPassword,
            confirmPasswordRaw = newPassword
        ) { success, msg ->
            resetSuccess = success
            resetMessage = msg
        }
        awaitCondition { resetSuccess }

        assertTrue("Password reset must succeed", resetSuccess)
        assertTrue("Success message must be clear", resetMessage.contains("successfully", ignoreCase = true))

        // Old password must NO LONGER authenticate
        vm.clearStatusMessage()
        vm.login(email = email, passwordRaw = oldPassword, role = UserRole.FREELANCER)
        awaitCondition { vm.statusMessage.value != null }
        assertEquals("Incorrect password. Please try again.", vm.statusMessage.value)
        assertEquals("Screen must remain on AuthLogin with old password", ScreenNav.AuthLogin, vm.currentScreen.value)

        // New password MUST authenticate successfully
        vm.clearStatusMessage()
        vm.login(email = email, passwordRaw = newPassword, role = UserRole.FREELANCER)
        awaitCondition { vm.currentScreen.value is ScreenNav.Dashboard }
        assertEquals("Login with new password must navigate to Dashboard", ScreenNav.Dashboard, vm.currentScreen.value)
        assertEquals("Logged in user ID must match original user", userId, vm.currentUserId.value)
    }

    @Test
    fun testForgotPassword_validationHandling() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = GigverseViewModel(app)

        // Case 1: Empty or invalid email
        var called1 = false
        vm.resetPassword(email = "", newPasswordRaw = "Pass123!", confirmPasswordRaw = "Pass123!") { success, msg ->
            called1 = true
            assertFalse(success)
            assertEquals("Please enter a valid email address.", msg)
        }
        assertTrue(called1)

        // Case 2: Empty new password
        var called2 = false
        vm.resetPassword(email = "user@test.com", newPasswordRaw = "", confirmPasswordRaw = "") { success, msg ->
            called2 = true
            assertFalse(success)
            assertEquals("Please enter a new password.", msg)
        }
        assertTrue(called2)

        // Case 3: Password too short (< 6 chars)
        var called3 = false
        vm.resetPassword(email = "user@test.com", newPasswordRaw = "12345", confirmPasswordRaw = "12345") { success, msg ->
            called3 = true
            assertFalse(success)
            assertEquals("Password must be at least 6 characters.", msg)
        }
        assertTrue(called3)

        // Case 4: Passwords mismatch
        var called4 = false
        vm.resetPassword(email = "user@test.com", newPasswordRaw = "Password123!", confirmPasswordRaw = "DifferentPass123!") { success, msg ->
            called4 = true
            assertFalse(success)
            assertEquals("New password and confirmation do not match.", msg)
        }
        assertTrue(called4)

        // Case 5: Non-existent email
        var called5 = false
        vm.resetPassword(email = "unknown.ghost@notfound.com", newPasswordRaw = "ValidPass123!", confirmPasswordRaw = "ValidPass123!") { success, msg ->
            called5 = true
            assertFalse(success)
            assertEquals("No account found with this email. Please check your credentials or register.", msg)
        }
        awaitCondition { called5 }
        assertTrue(called5)
    }

    @Test
    fun testForgotPassword_preservesAllOtherUserFields() = runBlocking {
        val db = getDb()
        val dao = db.appDao()

        val email = "preserve.fields@gigverse.com"
        val oldHash = SecurityUtils.hashPassword("InitialPassword123!")

        val initialUser = UserEntity(
            id = "user_preserve_test",
            role = "FREELANCER",
            fullName = "Preserve Dev",
            email = email,
            phone = "+1 555-8888",
            avatarUrl = "https://example.com/avatar.jpg",
            locationCountry = "Canada",
            locationState = "Ontario",
            locationDistrict = "Toronto",
            headline = "Principal Kotlin Engineer",
            bio = "Lifelong open-source developer",
            hourlyRate = 110.0,
            skills = "Kotlin, Room, Jetpack Compose",
            govIdType = "National ID",
            govIdNumber = "CA-123456",
            govIdDocUrl = "https://example.com/id.jpg",
            isGovIdVerified = true,
            linkedInUrl = "https://linkedin.com/in/preservedev",
            portfolioWebsite = "https://preservedev.ca",
            githubUrl = "https://github.com/preservedev",
            passwordHash = oldHash,
            workStatus = "AVAILABLE",
            trustScore = 98,
            onTimeDeliveryRate = 100,
            rating = 5.0,
            ratingCount = 42,
            completedProjectsCount = 15
        )
        dao.insertUser(initialUser)

        val newPassword = "UpgradedPassword2026#"
        val newHash = SecurityUtils.hashPassword(newPassword)

        // Update password using copy
        val userToUpdate = dao.getUserByEmail(email)
        assertNotNull(userToUpdate)
        dao.updateUser(userToUpdate!!.copy(passwordHash = newHash))

        // Reload and assert all fields are unchanged
        val reloaded = dao.getUserByEmail(email)
        assertNotNull(reloaded)
        assertEquals("user_preserve_test", reloaded?.id)
        assertEquals("FREELANCER", reloaded?.role)
        assertEquals("Preserve Dev", reloaded?.fullName)
        assertEquals(email, reloaded?.email)
        assertEquals("+1 555-8888", reloaded?.phone)
        assertEquals("https://example.com/avatar.jpg", reloaded?.avatarUrl)
        assertEquals("Canada", reloaded?.locationCountry)
        assertEquals("Ontario", reloaded?.locationState)
        assertEquals("Toronto", reloaded?.locationDistrict)
        assertEquals("Principal Kotlin Engineer", reloaded?.headline)
        assertEquals("Lifelong open-source developer", reloaded?.bio)
        assertEquals(110.0, reloaded?.hourlyRate ?: 0.0, 0.001)
        assertEquals("Kotlin, Room, Jetpack Compose", reloaded?.skills)
        assertEquals("CA-123456", reloaded?.govIdNumber)
        assertTrue(reloaded?.isGovIdVerified == true)
        assertEquals("AVAILABLE", reloaded?.workStatus)
        assertEquals(98, reloaded?.trustScore)
        assertEquals(100, reloaded?.onTimeDeliveryRate)
        assertEquals(5.0, reloaded?.rating ?: 0.0, 0.001)
        assertEquals(42, reloaded?.ratingCount)
        assertEquals(15, reloaded?.completedProjectsCount)

        // Old password rejected, new password verified
        assertFalse(SecurityUtils.verifyPassword("InitialPassword123!", reloaded!!.passwordHash).isValid)
        assertTrue(SecurityUtils.verifyPassword(newPassword, reloaded.passwordHash).isValid)

        db.close()
    }

    // =========================================================================
    // 3. ROLE ENFORCEMENT DURING AUTHENTICATION
    // =========================================================================

    @Test
    fun testAuthentication_roleEnforcement() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = GigverseViewModel(app)

        val freelancerEmail = "freelancer.role@gigverse.com"
        val password = "RoleTestPass123!"

        vm.registerFreelancer(
            name = "Freelancer Role User",
            email = freelancerEmail,
            passwordRaw = password,
            phone = "+1 555-3210",
            locationCountry = "USA",
            locationState = "TX",
            locationDistrict = "Austin",
            headline = "Backend Dev",
            skills = "Kotlin",
            hourlyRate = 50.0,
            govIdType = "ID",
            govIdNumber = "TX-1",
            linkedInUrl = "",
            portfolioUrl = "",
            githubUrl = ""
        )
        awaitCondition { vm.currentScreen.value is ScreenNav.Dashboard }
        vm.logout()
        org.robolectric.shadows.ShadowLooper.idleMainLooper()
        vm.clearStatusMessage()

        // Attempt logging into Client tab with Freelancer credentials
        vm.login(email = freelancerEmail, passwordRaw = password, role = UserRole.CLIENT)
        awaitCondition { vm.statusMessage.value != null }

        assertEquals("This email is registered as a Freelancer. Please switch to the Freelancer tab to sign in.", vm.statusMessage.value)
        assertEquals("Screen must remain AuthLogin", ScreenNav.AuthLogin, vm.currentScreen.value)

        // Logging into Freelancer tab with same credentials succeeds
        vm.clearStatusMessage()
        vm.login(email = freelancerEmail, passwordRaw = password, role = UserRole.FREELANCER)
        awaitCondition { vm.currentScreen.value is ScreenNav.Dashboard }

        assertEquals(ScreenNav.Dashboard, vm.currentScreen.value)
    }

    @Test
    fun testRememberMe_savedRolePersistenceAndRestore() {
        val email = "role.persist@gigverse.com"
        val role = "FREELANCER"

        // Save session with rememberMe = true
        sessionManager.saveUserSession("user_role_1", role, email, rememberMe = true)
        assertTrue(sessionManager.isRememberMeEnabled())
        assertEquals(email, sessionManager.getSavedEmail())
        assertEquals(role, sessionManager.getSavedRole())

        // Create new SessionManager instance to verify persistence across instances
        val newSessionManager = SessionManager(context)
        assertEquals(role, newSessionManager.getSavedRole())
        assertEquals(email, newSessionManager.getSavedEmail())

        // Save session with rememberMe = false clears saved role
        newSessionManager.saveUserSession("user_role_2", "CLIENT", "other@client.com", rememberMe = false)
        assertFalse(newSessionManager.isRememberMeEnabled())
        assertNull("Saved role must be cleared when Remember Me is false", newSessionManager.getSavedRole())
        assertNull("Saved email must be cleared when Remember Me is false", newSessionManager.getSavedEmail())
    }

    @Test
    fun testRememberMe_clientSessionRestorationAcrossRestart() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm1 = GigverseViewModel(app)

        val email = "client.restore@gigverse.com"
        val password = "ClientSecurePassword2026!"

        vm1.registerClient(
            name = "Client Restore User",
            company = "Restore Enterprises",
            email = email,
            passwordRaw = password,
            phone = "+1 555-4433",
            country = "USA",
            state = "NY",
            district = "New York",
            industry = "Finance"
        )
        awaitCondition { vm1.currentScreen.value is ScreenNav.Dashboard }
        val clientId = vm1.currentUserId.value
        assertTrue(clientId.isNotBlank())

        // Save session with rememberMe = true
        sessionManager.setRememberMePreference(true)
        sessionManager.saveUserSession(clientId, "CLIENT", email, rememberMe = true)
        assertTrue(sessionManager.isLoggedIn())

        // Cold restart ViewModel
        val vm2 = GigverseViewModel(app)
        org.robolectric.shadows.ShadowLooper.idleMainLooper()

        assertEquals("Client session must be automatically restored on cold restart", clientId, vm2.currentUserId.value)
        assertEquals("Screen must be Dashboard for remembered Client", ScreenNav.Dashboard, vm2.currentScreen.value)
        assertEquals("Saved role must be CLIENT", "CLIENT", vm2.getSavedRole())
    }

    @Test
    fun testLogin_callbackAndLoadingStateHandling() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = GigverseViewModel(app)

        val email = "callback.test@gigverse.com"
        val password = "ValidPassword123!"

        vm.registerClient(
            name = "Callback Client",
            company = "Callback Co",
            email = email,
            passwordRaw = password,
            phone = "+1 555-7788",
            country = "USA",
            state = "IL",
            district = "Chicago",
            industry = "Logistics"
        )
        awaitCondition { vm.currentScreen.value is ScreenNav.Dashboard }
        vm.logout()
        org.robolectric.shadows.ShadowLooper.idleMainLooper()

        // 1. Synchronous validation failure: empty email
        var cbCalled1 = false
        vm.login(email = "", passwordRaw = password, role = UserRole.CLIENT) { success, msg ->
            cbCalled1 = true
            assertFalse(success)
            assertEquals("Please enter your email address.", msg)
        }
        assertTrue("Callback must be invoked on empty email", cbCalled1)

        // 2. Synchronous validation failure: empty password
        var cbCalled2 = false
        vm.login(email = email, passwordRaw = "", role = UserRole.CLIENT) { success, msg ->
            cbCalled2 = true
            assertFalse(success)
            assertEquals("Please enter your password.", msg)
        }
        assertTrue("Callback must be invoked on empty password", cbCalled2)

        // 3. Asynchronous failure: wrong password
        var cbCalled3 = false
        var cbSuccess3 = true
        var cbMsg3 = ""
        vm.login(email = email, passwordRaw = "WrongPassword999!", role = UserRole.CLIENT) { success, msg ->
            cbCalled3 = true
            cbSuccess3 = success
            cbMsg3 = msg
        }
        awaitCondition { cbCalled3 }
        assertFalse("Callback must report failure for wrong password", cbSuccess3)
        assertEquals("Incorrect password. Please try again.", cbMsg3)

        // 4. Asynchronous success: correct password
        var cbCalled4 = false
        var cbSuccess4 = false
        vm.login(email = email, passwordRaw = password, role = UserRole.CLIENT) { success, _ ->
            cbCalled4 = true
            cbSuccess4 = success
        }
        awaitCondition { cbCalled4 }
        assertTrue("Callback must report success for correct password", cbSuccess4)
        assertEquals(ScreenNav.Dashboard, vm.currentScreen.value)
    }
}
