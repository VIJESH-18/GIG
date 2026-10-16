package com.gigverse.app

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gigverse.app.data.local.AppDao
import com.gigverse.app.data.local.AppDatabase
import com.gigverse.app.data.model.ProjectEntity
import com.gigverse.app.data.model.UserEntity
import com.gigverse.app.util.SecurityUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * On-Device Android Instrumented Database Test (AndroidJUnit4):
 * Runs directly on Android device or emulator to test real SQLite/Room operations,
 * schema constraints, transactions, and queries.
 */
@RunWith(AndroidJUnit4::class)
class AppDatabaseInstrumentedTest {

    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var dao: AppDao

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.appDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testUserAndProjectFlowOnDevice() = runBlocking {
        val user = UserEntity(
            id = "instrumented_user_1",
            role = "CLIENT",
            fullName = "Instrumented Client",
            email = "device.client@gigverse.com",
            phone = "+1 555-0100",
            passwordHash = SecurityUtils.hashPassword("DevicePass2026!")
        )
        dao.insertUser(user)

        val retrieved = dao.getUserByIdSync("instrumented_user_1")
        assertNotNull(retrieved)
        assertEquals("Instrumented Client", retrieved?.fullName)

        val project = ProjectEntity(
            id = "instrumented_proj_1",
            title = "On-Device SQLite Verification",
            description = "Validating Room on Android OS",
            category = "Testing",
            requiredSkills = "Android, Room, SQLite",
            budget = 2500.0,
            budgetType = "FIXED",
            deadlineDays = 14,
            scope = "Medium",
            clientId = "instrumented_user_1",
            clientName = "Instrumented Client",
            clientCompany = "GigVerse QA",
            clientLocation = "Localhost",
            status = "OPEN"
        )
        dao.insertProject(project)

        val floated = dao.getFloatedProjects().first()
        assertTrue(floated.any { it.id == "instrumented_proj_1" })

        // Test Unique Constraint on Device
        val duplicateUser = UserEntity(
            id = "instrumented_user_2",
            role = "FREELANCER",
            fullName = "Duplicate Email Attempt",
            email = "device.client@gigverse.com",
            phone = "+1 555-0102"
        )
        try {
            dao.insertUser(duplicateUser)
            fail("Device SQLite must throw unique constraint exception")
        } catch (_: Exception) {
            // Expected
        }
    }
}
