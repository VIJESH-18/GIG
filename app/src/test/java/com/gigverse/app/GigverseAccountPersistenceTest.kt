package com.gigverse.app

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import com.gigverse.app.data.local.AppDao
import com.gigverse.app.data.local.AppDatabase
import com.gigverse.app.data.local.SessionManager
import com.gigverse.app.data.model.ProjectEntity
import com.gigverse.app.data.model.ProposalEntity
import com.gigverse.app.data.model.UserEntity
import com.gigverse.app.data.model.UserRole
import com.gigverse.app.data.repository.GigverseRepository
import com.gigverse.app.ui.viewmodel.GigverseViewModel
import com.gigverse.app.ui.viewmodel.ScreenNav
import com.gigverse.app.util.SecurityUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class GigverseAccountPersistenceTest {

    private lateinit var context: Context
    private lateinit var sessionManager: SessionManager
    private lateinit var testDbFile: File
    private var persistentDb: AppDatabase? = null

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext<Context>()
        sessionManager = SessionManager(context)
        sessionManager.clearSession()
        testDbFile = File(context.filesDir, "test_gigverse_persistent.db")
        if (testDbFile.exists()) {
            testDbFile.delete()
        }
    }

    @After
    fun tearDown() {
        persistentDb?.close()
        persistentDb = null
        if (testDbFile.exists()) {
            testDbFile.delete()
        }
        sessionManager.clearSession()
    }

    private fun createPersistentDb(): AppDatabase {
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

    private fun createV3Database(dbFile: File, userVersion: Int = 3, onPopulate: (android.database.sqlite.SQLiteDatabase) -> Unit) {
        val db = android.database.sqlite.SQLiteDatabase.openOrCreateDatabase(dbFile, null)
        try {
            db.execSQL("CREATE TABLE IF NOT EXISTS `users` (`id` TEXT NOT NULL, `role` TEXT NOT NULL, `fullName` TEXT NOT NULL, `email` TEXT NOT NULL, `phone` TEXT NOT NULL, `avatarUrl` TEXT NOT NULL, `locationCountry` TEXT NOT NULL, `locationState` TEXT NOT NULL, `locationDistrict` TEXT NOT NULL, `headline` TEXT NOT NULL, `bio` TEXT NOT NULL, `hourlyRate` REAL NOT NULL, `skills` TEXT NOT NULL, `govIdType` TEXT NOT NULL, `govIdNumber` TEXT NOT NULL, `govIdDocUrl` TEXT NOT NULL, `isGovIdVerified` INTEGER NOT NULL, `linkedInUrl` TEXT NOT NULL, `portfolioWebsite` TEXT NOT NULL, `githubUrl` TEXT NOT NULL, `passwordHash` TEXT NOT NULL, `workStatus` TEXT NOT NULL, `currentActiveProjectId` TEXT, `trustScore` INTEGER NOT NULL, `onTimeDeliveryRate` INTEGER NOT NULL, `rating` REAL NOT NULL, `ratingCount` INTEGER NOT NULL, `disputeCount` INTEGER NOT NULL, `completedProjectsCount` INTEGER NOT NULL, `badgeLevel` TEXT NOT NULL, `companyName` TEXT NOT NULL, `industry` TEXT NOT NULL, `totalSpent` REAL NOT NULL, `projectsPostedCount` INTEGER NOT NULL, PRIMARY KEY(`id`))")
            db.execSQL("CREATE TABLE IF NOT EXISTS `projects` (`id` TEXT NOT NULL, `title` TEXT NOT NULL, `description` TEXT NOT NULL, `category` TEXT NOT NULL, `requiredSkills` TEXT NOT NULL, `budget` REAL NOT NULL, `budgetType` TEXT NOT NULL, `deadlineDays` INTEGER NOT NULL, `scope` TEXT NOT NULL, `clientId` TEXT NOT NULL, `clientName` TEXT NOT NULL, `clientCompany` TEXT NOT NULL, `clientLocation` TEXT NOT NULL, `status` TEXT NOT NULL, `assignedFreelancerId` TEXT, `assignedFreelancerName` TEXT, `createdAt` INTEGER NOT NULL, `startedAt` INTEGER, `completedAt` INTEGER, `milestoneStage` INTEGER NOT NULL, `stageNotes` TEXT NOT NULL, `isBidRoomEnabled` INTEGER NOT NULL, `bidRoomAllowedLocation` TEXT NOT NULL, `aiEstimatedDays` INTEGER NOT NULL, `aiComplexityScore` TEXT NOT NULL, `aiVelocityFactor` REAL NOT NULL, PRIMARY KEY(`id`))")
            db.execSQL("CREATE TABLE IF NOT EXISTS `proposals` (`id` TEXT NOT NULL, `projectId` TEXT NOT NULL, `freelancerId` TEXT NOT NULL, `freelancerName` TEXT NOT NULL, `freelancerAvatar` TEXT NOT NULL, `freelancerTrustScore` INTEGER NOT NULL, `freelancerHourlyRate` REAL NOT NULL, `bidAmount` REAL NOT NULL, `estimatedDays` INTEGER NOT NULL, `coverLetter` TEXT NOT NULL, `status` TEXT NOT NULL, `submittedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))")
            db.execSQL("CREATE TABLE IF NOT EXISTS `bid_room_bids` (`id` TEXT NOT NULL, `projectId` TEXT NOT NULL, `freelancerId` TEXT NOT NULL, `freelancerName` TEXT NOT NULL, `freelancerLocation` TEXT NOT NULL, `freelancerTrustScore` INTEGER NOT NULL, `bidAmount` REAL NOT NULL, `durationDays` INTEGER NOT NULL, `pitch` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, PRIMARY KEY(`id`))")
            db.execSQL("CREATE TABLE IF NOT EXISTS `messages` (`id` TEXT NOT NULL, `projectId` TEXT NOT NULL, `senderId` TEXT NOT NULL, `senderName` TEXT NOT NULL, `senderRole` TEXT NOT NULL, `message` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, PRIMARY KEY(`id`))")
            db.execSQL("CREATE TABLE IF NOT EXISTS `reviews` (`id` TEXT NOT NULL, `projectId` TEXT NOT NULL, `fromUserId` TEXT NOT NULL, `fromUserName` TEXT NOT NULL, `toUserId` TEXT NOT NULL, `rating` REAL NOT NULL, `tags` TEXT NOT NULL, `comment` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, PRIMARY KEY(`id`))")
            db.execSQL("CREATE TABLE IF NOT EXISTS `gig_packages` (`id` TEXT NOT NULL, `freelancerId` TEXT NOT NULL, `freelancerName` TEXT NOT NULL, `title` TEXT NOT NULL, `category` TEXT NOT NULL, `coverImageUrl` TEXT NOT NULL, `basicTitle` TEXT NOT NULL, `basicPrice` REAL NOT NULL, `basicDeliveryDays` INTEGER NOT NULL, `standardTitle` TEXT NOT NULL, `standardPrice` REAL NOT NULL, `standardDeliveryDays` INTEGER NOT NULL, `premiumTitle` TEXT NOT NULL, `premiumPrice` REAL NOT NULL, `premiumDeliveryDays` INTEGER NOT NULL, `rating` REAL NOT NULL, `reviewsCount` INTEGER NOT NULL, PRIMARY KEY(`id`))")
            db.execSQL("CREATE TABLE IF NOT EXISTS `tasks` (`id` TEXT NOT NULL, `projectId` TEXT NOT NULL, `title` TEXT NOT NULL, `description` TEXT NOT NULL, `status` TEXT NOT NULL, `priority` TEXT NOT NULL, `assigneeName` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`id`))")
            db.execSQL("CREATE TABLE IF NOT EXISTS `disputes` (`id` TEXT NOT NULL, `projectId` TEXT NOT NULL, `projectTitle` TEXT NOT NULL, `raisedByUserId` TEXT NOT NULL, `raisedByUserName` TEXT NOT NULL, `reason` TEXT NOT NULL, `description` TEXT NOT NULL, `evidenceUrl` TEXT NOT NULL, `status` TEXT NOT NULL, `resolutionNotes` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`id`))")
            db.execSQL("CREATE TABLE IF NOT EXISTS `milestone_submissions` (`id` TEXT NOT NULL, `projectId` TEXT NOT NULL, `milestoneStage` INTEGER NOT NULL, `title` TEXT NOT NULL, `deliverableLinks` TEXT NOT NULL, `notes` TEXT NOT NULL, `status` TEXT NOT NULL, `revisionComments` TEXT NOT NULL, `submittedAt` INTEGER NOT NULL, `reviewedAt` INTEGER, PRIMARY KEY(`id`))")
            db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
            db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'older_hash_v${'$'}userVersion')")
            db.version = userVersion
            onPopulate(db)
        } finally {
            db.close()
        }
    }

    private fun awaitCondition(timeoutMs: Long = 3000, condition: () -> Boolean) {
        val start = System.currentTimeMillis()
        while (!condition() && (System.currentTimeMillis() - start) < timeoutMs) {
            org.robolectric.shadows.ShadowLooper.idleMainLooper()
            Thread.sleep(20)
        }
        org.robolectric.shadows.ShadowLooper.idleMainLooper()
    }

    // =========================================================================
    // Test A — Registration persistence (authoritative DB write and restart)
    // =========================================================================
    @Test
    fun testA_registrationPersistenceAcrossDatabaseRestart() = runBlocking {
        // Step 1: Register new account in authoritative DB
        val db1 = createPersistentDb()
        val dao1 = db1.appDao()
        val rawEmail = "alex.founder@techstart.io"
        val rawPassword = "StartupPassword2026!"
        val normalizedEmail = SecurityUtils.normalizeEmail(rawEmail)
        val passwordHash = SecurityUtils.hashPassword(rawPassword)

        val newUser = UserEntity(
            id = "client_test_001",
            role = "CLIENT",
            fullName = "Alex Founder",
            email = normalizedEmail,
            phone = "+1 555-0101",
            companyName = "TechStart IO",
            passwordHash = passwordHash
        )
        dao1.insertUser(newUser)

        // Step 2: Verify account exists in authoritative DB
        val retrieved1 = dao1.getUserByEmail(normalizedEmail)
        assertNotNull("Account must exist in database after insert", retrieved1)
        assertEquals("Alex Founder", retrieved1?.fullName)

        // Step 3 & 4: Stop backend/close DB, then reopen DB
        db1.close()

        val db2 = createPersistentDb()
        val dao2 = db2.appDao()

        // Step 5 & 6: Query with same credentials and verify success
        val retrieved2 = dao2.getUserByEmail(normalizedEmail)
        assertNotNull("Account must persist after database restart", retrieved2)
        val verifyResult = SecurityUtils.verifyPassword(rawPassword, retrieved2!!.passwordHash)
        assertTrue("Password verification must succeed after restart", verifyResult.isValid)
        assertFalse("Modern PBKDF2 hash should not require rehash", verifyResult.needsRehash)

        db2.close()
    }

    // =========================================================================
    // Test B — Persistence across application restart (Session & ViewModel)
    // =========================================================================
    @Test
    fun testB_persistenceAcrossApplicationRestart() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()

        // 1. First app launch: ViewModel registers an account
        val vm1 = GigverseViewModel(app)
        val email = "jane.designer@creatives.co"
        val password = "CreativePass123!"

        vm1.registerFreelancer(
            name = "Jane Designer",
            email = email,
            passwordRaw = password,
            phone = "+1 555-0202",
            locationCountry = "USA",
            locationState = "NY",
            locationDistrict = "Manhattan",
            headline = "Principal UI/UX Designer",
            skills = "Figma, Jetpack Compose",
            hourlyRate = 95.0,
            govIdType = "State ID",
            govIdNumber = "NY-12345",
            linkedInUrl = "https://linkedin.com/in/janedesign",
            portfolioUrl = "https://janedesign.io",
            githubUrl = "https://github.com/janedesign"
        )

        // Wait for coroutines and Room background queries to complete
        awaitCondition { vm1.currentScreen.value is ScreenNav.Dashboard }

        assertEquals(ScreenNav.Dashboard, vm1.currentScreen.value)
        val firstUserId = vm1.currentUserId.value
        assertTrue("User ID must be set", firstUserId.isNotBlank())
        assertTrue("Session manager must record user as logged in", sessionManager.isLoggedIn())

        // 2. Simulate force closing and reopening the app next day (new ViewModel instance)
        val vm2 = GigverseViewModel(app)
        org.robolectric.shadows.ShadowLooper.idleMainLooper()

        // The startup purge bug would have erased the user here!
        // Verify that with the bug fixed, the user session is preserved:
        assertEquals("User ID must persist across ViewModel recreation", firstUserId, vm2.currentUserId.value)
        assertEquals(ScreenNav.Dashboard, vm2.currentScreen.value)

        // Also test logging in with the same credentials
        sessionManager.clearSession()
        val vm3 = GigverseViewModel(app)
        org.robolectric.shadows.ShadowLooper.idleMainLooper()
        assertEquals(ScreenNav.AuthLogin, vm3.currentScreen.value)

        vm3.login(email = email, passwordRaw = password, role = UserRole.FREELANCER)
        awaitCondition { vm3.currentScreen.value is ScreenNav.Dashboard }

        assertEquals("Login must succeed after app restart", ScreenNav.Dashboard, vm3.currentScreen.value)
        assertEquals("Logged in user ID must match registered ID", firstUserId, vm3.currentUserId.value)
    }

    // =========================================================================
    // Test C — Persistence across time (simulated later login session)
    // =========================================================================
    @Test
    fun testC_persistenceAcrossTimeSimulated() = runBlocking {
        val db = createPersistentDb()
        val dao = db.appDao()

        val email = "future.contractor@remote.org"
        val password = "TimelessSecret42!"
        val normalizedEmail = SecurityUtils.normalizeEmail(email)
        val initialUser = UserEntity(
            id = "freelancer_time_test",
            role = "FREELANCER",
            fullName = "Future Contractor",
            email = normalizedEmail,
            phone = "+1 555-8888",
            passwordHash = SecurityUtils.hashPassword(password)
        )
        dao.insertUser(initialUser)
        db.close()

        // Simulate independent session at t + 86400s (next day)
        val reopenedDb = createPersistentDb()
        val reopenedDao = reopenedDb.appDao()

        val foundUser = reopenedDao.getUserByEmail(normalizedEmail)
        assertNotNull("User must exist on subsequent day query", foundUser)
        assertEquals("Future Contractor", foundUser?.fullName)
        val authResult = SecurityUtils.verifyPassword(password, foundUser!!.passwordHash)
        assertTrue("Authentication must succeed on subsequent day", authResult.isValid)

        reopenedDb.close()
    }

    // =========================================================================
    // Test D — Duplicate registration rejection
    // =========================================================================
    @Test
    fun testD_duplicateRegistrationRejection() = runBlocking {
        val db = createPersistentDb()
        val dao = db.appDao()

        val email = "duplicate.test@gigverse.com"
        val normalizedEmail = SecurityUtils.normalizeEmail(email)

        val user1 = UserEntity(
            id = "user_dup_1",
            role = "CLIENT",
            fullName = "Original User",
            email = normalizedEmail,
            phone = "+1 555-1111",
            passwordHash = SecurityUtils.hashPassword("Password111!")
        )
        dao.insertUser(user1)

        val count = dao.countUsersByEmail(normalizedEmail)
        assertEquals(1, count)

        // Attempting to insert another entity with different primary key but same email
        val user2 = UserEntity(
            id = "user_dup_2",
            role = "CLIENT",
            fullName = "Imposter User",
            email = normalizedEmail,
            phone = "+1 555-2222",
            passwordHash = SecurityUtils.hashPassword("Password222!")
        )

        try {
            dao.insertUser(user2)
            fail("Unique constraint on email must throw SQLiteConstraintException on duplicate email insert")
        } catch (e: Exception) {
            // Success: database constraint safely prevented duplicate
            assertTrue("Exception must be constraint related", e.message?.contains("UNIQUE", ignoreCase = true) == true || e.message?.contains("constraint", ignoreCase = true) == true)
        }

        // Verify original account remains untouched
        val survivingUser = dao.getUserByEmail(normalizedEmail)
        assertEquals("Original User", survivingUser?.fullName)
        assertEquals("user_dup_1", survivingUser?.id)

        db.close()
    }

    // =========================================================================
    // Test E — Email normalization (case and whitespace insensitivity)
    // =========================================================================
    @Test
    fun testE_emailNormalization() = runBlocking {
        val db = createPersistentDb()
        val dao = db.appDao()

        val registeredEmail = "  User.Name+Test@Example.COM  "
        val normalizedEmail = SecurityUtils.normalizeEmail(registeredEmail)
        assertEquals("user.name+test@example.com", normalizedEmail)

        dao.insertUser(
            UserEntity(
                id = "user_norm_01",
                role = "FREELANCER",
                fullName = "Normalized User",
                email = normalizedEmail,
                phone = "+1 555-3333",
                passwordHash = SecurityUtils.hashPassword("SecurePass123!")
            )
        )

        // Query variations: uppercase, extra spaces, mixed case
        val query1 = dao.getUserByEmail("user.name+test@example.com")
        assertNotNull("Exact match lookup must succeed", query1)

        val query2 = dao.getUserByEmail("USER.NAME+TEST@EXAMPLE.COM")
        assertNotNull("All-caps lookup must succeed", query2)

        val query3 = dao.getUserByEmail("   User.Name+Test@example.com   ")
        assertNotNull("Whitespace-padded lookup must succeed", query3)

        assertEquals("user_norm_01", query1?.id)
        assertEquals("user_norm_01", query2?.id)
        assertEquals("user_norm_01", query3?.id)

        db.close()
    }

    // =========================================================================
    // Test F — Wrong password rejected and account intact
    // =========================================================================
    @Test
    fun testF_wrongPasswordRejection() = runBlocking {
        val rawPassword = "CorrectPassword123!"
        val hash = SecurityUtils.hashPassword(rawPassword)

        val correctAttempt = SecurityUtils.verifyPassword("CorrectPassword123!", hash)
        assertTrue("Correct password must verify", correctAttempt.isValid)

        val wrongAttempt = SecurityUtils.verifyPassword("WrongPassword456!", hash)
        assertFalse("Wrong password must be rejected", wrongAttempt.isValid)

        val emptyAttempt = SecurityUtils.verifyPassword("", hash)
        assertFalse("Empty password must be rejected", emptyAttempt.isValid)
    }

    // =========================================================================
    // Test G — Profile persistence
    // =========================================================================
    @Test
    fun testG_profilePersistenceAcrossRestarts() = runBlocking {
        val db1 = createPersistentDb()
        val dao1 = db1.appDao()

        val userId = "freelancer_profile_test"
        val user = UserEntity(
            id = userId,
            role = "FREELANCER",
            fullName = "Initial Profile Name",
            email = "profile.test@gigverse.com",
            phone = "+1 555-4444",
            headline = "Junior Developer",
            hourlyRate = 40.0,
            passwordHash = SecurityUtils.hashPassword("Pass1234!")
        )
        dao1.insertUser(user)

        // Update profile fields
        val updatedUser = user.copy(
            fullName = "Senior Lead Architect",
            headline = "Staff Mobile Architect & AI Specialist",
            hourlyRate = 120.0,
            skills = "Android, Kotlin, Multiplatform, Room, Compose"
        )
        dao1.updateUser(updatedUser)
        db1.close()

        // Reopen DB
        val db2 = createPersistentDb()
        val dao2 = db2.appDao()

        val reloaded = dao2.getUserByIdSync(userId)
        assertNotNull("User must exist", reloaded)
        assertEquals("Senior Lead Architect", reloaded?.fullName)
        assertEquals("Staff Mobile Architect & AI Specialist", reloaded?.headline)
        assertEquals(120.0, reloaded?.hourlyRate ?: 0.0, 0.001)
        assertEquals("Android, Kotlin, Multiplatform, Room, Compose", reloaded?.skills)

        db2.close()
    }

    // =========================================================================
    // Test H — Database unavailable / Controlled validation error handling
    // =========================================================================
    @Test
    fun testH_controlledErrorHandlingOnInvalidData() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = GigverseViewModel(app)

        // Attempt login with empty credentials
        vm.login("", "", UserRole.CLIENT)
        assertEquals("Please enter your email address.", vm.statusMessage.value)

        vm.login("valid.email@domain.com", "", UserRole.CLIENT)
        assertEquals("Please enter your password.", vm.statusMessage.value)

        // Attempt login with non-existent account
        vm.login("nonexistent@domain.com", "anyPassword123!", UserRole.CLIENT)
        awaitCondition { vm.statusMessage.value != "Please enter your password." }
        assertEquals("No account found with this email. Please check your credentials or register.", vm.statusMessage.value)
    }

    // =========================================================================
    // Test I — Migration preserves data and adds unique index
    // =========================================================================
    @Test
    fun testI_migrationPreservesExistingData() = runBlocking {
        // Step 1: Create a manual SQLite database with schema v3 (without the unique index)
        createV3Database(testDbFile, userVersion = 3) { db ->
            val cv = android.content.ContentValues().apply {
                put("id", "legacy_user_01")
                put("role", "FREELANCER")
                put("fullName", "Legacy Freelancer")
                put("email", "legacy@gigverse.com")
                put("phone", "+1 555-7777")
                put("avatarUrl", "")
                put("locationCountry", "United States")
                put("locationState", "California")
                put("locationDistrict", "San Francisco")
                put("headline", "")
                put("bio", "")
                put("hourlyRate", 45.0)
                put("skills", "Kotlin")
                put("govIdType", "Passport")
                put("govIdNumber", "US-123")
                put("govIdDocUrl", "")
                put("isGovIdVerified", 1)
                put("linkedInUrl", "")
                put("portfolioWebsite", "")
                put("githubUrl", "")
                put("passwordHash", SecurityUtils.hashPassword("LegacyPass123!"))
                put("workStatus", "AVAILABLE")
                put("trustScore", 90)
                put("onTimeDeliveryRate", 95)
                put("rating", 4.9)
                put("ratingCount", 10)
                put("disputeCount", 0)
                put("completedProjectsCount", 5)
                put("badgeLevel", "Top Rated")
                put("companyName", "")
                put("industry", "")
                put("totalSpent", 0.0)
                put("projectsPostedCount", 0)
            }
            db.insert("users", null, cv)
        }

        // Step 2: Open database with v4 migrations applied
        val upgradedDb = createPersistentDb()
        val upgradedDao = upgradedDb.appDao()

        val retrieved = upgradedDao.getUserByIdSync("legacy_user_01")
        assertNotNull("Existing user must survive migration", retrieved)
        assertEquals("Legacy Freelancer", retrieved?.fullName)
        assertEquals("legacy@gigverse.com", retrieved?.email)

        upgradedDb.close()
    }

    // =========================================================================
    // Test J — Concurrent registration requests with the same email
    // =========================================================================
    @Test
    fun testJ_concurrentRegistrationsPreventDuplicates() = runBlocking(Dispatchers.IO) {
        val db = createPersistentDb()
        val dao = db.appDao()
        val email = "race.condition@test.com"
        val normalized = SecurityUtils.normalizeEmail(email)

        val attempts = (1..5).map { index ->
            async {
                try {
                    dao.insertUser(
                        UserEntity(
                            id = "concurrent_user_$index",
                            role = "FREELANCER",
                            fullName = "Concurrent User $index",
                            email = normalized,
                            phone = "+1 555-000$index",
                            passwordHash = SecurityUtils.hashPassword("Password$index!")
                        )
                    )
                    true
                } catch (e: Exception) {
                    false
                }
            }
        }

        val results = attempts.awaitAll()
        val successes = results.count { it }
        val failures = results.count { !it }

        assertEquals("Exactly 1 concurrent insert must succeed", 1, successes)
        assertEquals("4 concurrent inserts must be rejected by unique constraint", 4, failures)

        val totalInDb = dao.countUsersByEmail(normalized)
        assertEquals(1, totalInDb)

        db.close()
    }

    // =========================================================================
    // Test K — Existing users (Legacy SHA-256 and Plaintext hash upgrades)
    // =========================================================================
    @Test
    fun testK_legacyHashBackwardCompatibilityAndUpgrade() = runBlocking {
        val rawPassword = "OldPassword2024!"

        // 1. Simulate legacy SHA-256 hash (64 hex characters)
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        val legacySha256 = digest.digest(rawPassword.toByteArray()).joinToString("") { "%02x".format(it) }
        assertEquals(64, legacySha256.length)

        val verifyLegacy = SecurityUtils.verifyPassword(rawPassword, legacySha256)
        assertTrue("Legacy SHA-256 password must authenticate successfully", verifyLegacy.isValid)
        assertTrue("Legacy SHA-256 hash must signal that it needs rehash to modern PBKDF2", verifyLegacy.needsRehash)

        // 2. Simulate legacy plaintext password
        val legacyPlaintext = "PlaintextPass!"
        val verifyPlaintext = SecurityUtils.verifyPassword("PlaintextPass!", legacyPlaintext)
        assertTrue("Legacy plaintext password must authenticate successfully", verifyPlaintext.isValid)
        assertTrue("Legacy plaintext must signal that it needs rehash to modern PBKDF2", verifyPlaintext.needsRehash)

        // 3. Test automatic upgrade in database
        val db = createPersistentDb()
        val dao = db.appDao()

        val legacyUser = UserEntity(
            id = "user_legacy_sha",
            role = "CLIENT",
            fullName = "Legacy Account Holder",
            email = "legacy.holder@company.com",
            phone = "+1 555-9999",
            passwordHash = legacySha256
        )
        dao.insertUser(legacyUser)

        val retrieved = dao.getUserByEmail("legacy.holder@company.com")
        assertNotNull(retrieved)

        val check = SecurityUtils.verifyPassword(rawPassword, retrieved!!.passwordHash)
        assertTrue(check.isValid)
        if (check.needsRehash) {
            val upgraded = SecurityUtils.hashPassword(rawPassword)
            dao.updateUser(retrieved.copy(passwordHash = upgraded))
        }

        // Verify the database now contains the upgraded PBKDF2 hash
        val reRetrieved = dao.getUserByEmail("legacy.holder@company.com")
        assertTrue("Upgraded hash must start with 'pbkdf2:'", reRetrieved!!.passwordHash.startsWith("pbkdf2:"))

        val checkUpgraded = SecurityUtils.verifyPassword(rawPassword, reRetrieved.passwordHash)
        assertTrue(checkUpgraded.isValid)
        assertFalse("Upgraded hash does not need rehash anymore", checkUpgraded.needsRehash)

        db.close()
    }

    // =========================================================================
    // Test L — Session expiration and re-authentication
    // =========================================================================
    @Test
    fun testL_sessionExpiryAndReAuthentication() = runBlocking {
        // Setup session created 31 days ago (exceeding 30-day TTL)
        val pastTimestamp = System.currentTimeMillis() - (31L * 24 * 60 * 60 * 1000L)
        val prefs = context.getSharedPreferences("gigverse_prod_session_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean("is_logged_in", true)
            .putString("active_user_id", "user_expired_session")
            .putString("active_user_email", "expired@gigverse.com")
            .putString("active_user_role", "FREELANCER")
            .putLong("login_timestamp", pastTimestamp)
            .commit()

        assertTrue("Session must be detected as expired", sessionManager.isSessionExpired())
        assertFalse("isLoggedIn must return false for expired session", sessionManager.isLoggedIn())

        // Re-authenticate user
        sessionManager.saveUserSession("user_expired_session", "FREELANCER", "expired@gigverse.com")
        assertFalse("Renewed session must not be expired", sessionManager.isSessionExpired())
        assertTrue("isLoggedIn must return true after renewal", sessionManager.isLoggedIn())
    }

    // =========================================================================
    // Test M — Malformed hash input handling
    // =========================================================================
    @Test
    fun testM_malformedHashDoesNotCrashOrAuthenticate() {
        val malformedCases = listOf(
            "pbkdf2:invalid:salt:hash",
            "pbkdf2:10000:oddlengthsalt:evenhash01",
            "pbkdf2:10000:zznonhexsaltzz:0123456789abcdef",
            "pbkdf2:notanumber:abcd:ef01",
            "pbkdf2:50:shortsalt:shorthash",
            "notahashatall",
            ""
        )
        for (badHash in malformedCases) {
            val result = SecurityUtils.verifyPassword("Password123!", badHash)
            assertFalse("Malformed hash '$badHash' must reject authentication safely", result.isValid)
        }
    }

    // =========================================================================
    // Test N — Failed logins preserve stored credentials
    // =========================================================================
    @Test
    fun testN_failedLoginPreservesCredentials() = runBlocking {
        val db = createPersistentDb()
        val dao = db.appDao()

        val originalHash = SecurityUtils.hashPassword("OriginalSecretPass!")
        val user = UserEntity(
            id = "user_safety_check",
            role = "CLIENT",
            fullName = "Safety User",
            email = "safety@gigverse.com",
            phone = "+1 555-5555",
            passwordHash = originalHash
        )
        dao.insertUser(user)

        // Attempt verify with wrong password
        val verification = SecurityUtils.verifyPassword("WrongPasswordAttempt!", user.passwordHash)
        assertFalse(verification.isValid)

        // Re-query user from database to ensure no mutation occurred
        val persisted = dao.getUserByEmail("safety@gigverse.com")
        assertNotNull(persisted)
        assertEquals("Stored password hash must be strictly identical after failed login", originalHash, persisted?.passwordHash)

        db.close()
    }

    // =========================================================================
    // Test O — Logout and re-login retrieves correct profile
    // =========================================================================
    @Test
    fun testO_logoutAndReLoginRetrievesCorrectProfile() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = GigverseViewModel(app)

        val email = "session.flow@gigverse.com"
        val password = "ValidPassword123!"

        vm.registerClient(
            name = "Session Enterprise",
            company = "Enterprise Cloud",
            email = email,
            passwordRaw = password,
            phone = "+1 555-9000",
            country = "USA",
            state = "WA",
            district = "Seattle",
            industry = "Cloud Infra"
        )
        awaitCondition { vm.currentScreen.value is ScreenNav.Dashboard }

        val originalUserId = vm.currentUserId.value
        assertTrue(originalUserId.isNotBlank())
        assertTrue(sessionManager.isLoggedIn())

        // Logout
        vm.logout()
        org.robolectric.shadows.ShadowLooper.idleMainLooper()

        assertEquals(ScreenNav.AuthLogin, vm.currentScreen.value)
        assertFalse("SessionManager must reflect logged out state", sessionManager.isLoggedIn())
        assertNull("Active user ID must be cleared on logout", sessionManager.getActiveUserId())

        // Re-login
        vm.login(email = email, passwordRaw = password, role = UserRole.CLIENT)
        awaitCondition { vm.currentScreen.value is ScreenNav.Dashboard }

        assertEquals(ScreenNav.Dashboard, vm.currentScreen.value)
        assertEquals("Re-logged in user ID must match original account", originalUserId, vm.currentUserId.value)
        assertTrue(sessionManager.isLoggedIn())
    }

    // =========================================================================
    // Test P — Migration resolves duplicates without deleting records or breaking relationships
    // =========================================================================
    @Test
    fun testP_migrationResolvesDuplicatesWithoutDataLoss() = runBlocking {
        val duplicateEmail = "shared.dup@domain.com"
        createV3Database(testDbFile, userVersion = 3) { db ->
            val cv1 = android.content.ContentValues().apply {
                put("id", "user_dup_primary")
                put("role", "CLIENT")
                put("fullName", "Primary Account")
                put("email", duplicateEmail)
                put("phone", "+1 555-1001")
                put("avatarUrl", "")
                put("locationCountry", "United States")
                put("locationState", "California")
                put("locationDistrict", "San Francisco")
                put("headline", "")
                put("bio", "")
                put("hourlyRate", 45.0)
                put("skills", "Kotlin")
                put("govIdType", "Passport")
                put("govIdNumber", "US-123")
                put("govIdDocUrl", "")
                put("isGovIdVerified", 1)
                put("linkedInUrl", "")
                put("portfolioWebsite", "")
                put("githubUrl", "")
                put("passwordHash", SecurityUtils.hashPassword("Pass1!"))
                put("workStatus", "AVAILABLE")
                put("trustScore", 90)
                put("onTimeDeliveryRate", 95)
                put("rating", 4.9)
                put("ratingCount", 10)
                put("disputeCount", 0)
                put("completedProjectsCount", 5)
                put("badgeLevel", "Top Rated")
                put("companyName", "Company 1")
                put("industry", "Tech")
                put("totalSpent", 1000.0)
                put("projectsPostedCount", 1)
            }
            val cv2 = android.content.ContentValues().apply {
                put("id", "user_dup_secondary")
                put("role", "FREELANCER")
                put("fullName", "Secondary Account")
                put("email", duplicateEmail)
                put("phone", "+1 555-1002")
                put("avatarUrl", "")
                put("locationCountry", "United States")
                put("locationState", "California")
                put("locationDistrict", "San Francisco")
                put("headline", "")
                put("bio", "")
                put("hourlyRate", 50.0)
                put("skills", "Android")
                put("govIdType", "Passport")
                put("govIdNumber", "US-456")
                put("govIdDocUrl", "")
                put("isGovIdVerified", 1)
                put("linkedInUrl", "")
                put("portfolioWebsite", "")
                put("githubUrl", "")
                put("passwordHash", SecurityUtils.hashPassword("Pass2!"))
                put("workStatus", "AVAILABLE")
                put("trustScore", 92)
                put("onTimeDeliveryRate", 98)
                put("rating", 4.8)
                put("ratingCount", 8)
                put("disputeCount", 0)
                put("completedProjectsCount", 4)
                put("badgeLevel", "Top Rated")
                put("companyName", "")
                put("industry", "")
                put("totalSpent", 0.0)
                put("projectsPostedCount", 0)
            }
            val cvProj = android.content.ContentValues().apply {
                put("id", "proj_test_rel_01")
                put("title", "Cloud Migration Project")
                put("description", "Project description")
                put("category", "Cloud")
                put("requiredSkills", "Kotlin, AWS")
                put("budget", 5000.0)
                put("budgetType", "FIXED")
                put("deadlineDays", 30)
                put("scope", "Large")
                put("clientId", "user_dup_primary")
                put("clientName", "Primary Account")
                put("clientCompany", "Company 1")
                put("clientLocation", "San Francisco")
                put("status", "OPEN")
                put("createdAt", System.currentTimeMillis())
                put("milestoneStage", 1)
                put("stageNotes", "")
                put("isBidRoomEnabled", 0)
                put("bidRoomAllowedLocation", "ANY")
                put("aiEstimatedDays", 25)
                put("aiComplexityScore", "MEDIUM")
                put("aiVelocityFactor", 1.0)
            }
            db.insert("users", null, cv1)
            db.insert("users", null, cv2)
            db.insert("projects", null, cvProj)
        }

        // Apply migrations to v4
        val upgradedDb = createPersistentDb()
        val upgradedDao = upgradedDb.appDao()

        val reloadedUser1 = upgradedDao.getUserByIdSync("user_dup_primary")
        val reloadedUser2 = upgradedDao.getUserByIdSync("user_dup_secondary")

        assertNotNull("Primary user must survive migration", reloadedUser1)
        assertNotNull("Secondary user must survive migration without deletion", reloadedUser2)

        assertEquals("shared.dup@domain.com", reloadedUser1?.email)
        assertEquals("shared.dup@domain.com_dup_user_dup_secondary", reloadedUser2?.email)

        // Verify project relationship is intact
        val reloadedProject = upgradedDao.getProjectByIdSync("proj_test_rel_01")
        assertNotNull(reloadedProject)
        assertEquals("user_dup_primary", reloadedProject?.clientId)

        // Verify that after migration to v4, the unique constraint is actively enforced
        try {
            val user3 = UserEntity(
                id = "user_dup_tertiary",
                role = "FREELANCER",
                fullName = "Tertiary Collision",
                email = "shared.dup@domain.com",
                phone = "+1 555-9999",
                passwordHash = SecurityUtils.hashPassword("Pass3!")
            )
            upgradedDao.insertUser(user3)
            fail("Inserting duplicate email after migration to v4 must trigger constraint failure")
        } catch (e: Exception) {
            assertTrue("Expected SQLiteConstraintException", e is android.database.sqlite.SQLiteConstraintException)
        }

        upgradedDb.close()
    }
}
