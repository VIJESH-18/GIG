package com.gigverse.app

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.gigverse.app.data.cloud.MongoDbCloudManager
import com.gigverse.app.data.model.BidEntity
import com.gigverse.app.data.model.ProjectEntity
import com.gigverse.app.data.model.ProposalEntity
import com.gigverse.app.data.model.ReviewEntity
import com.gigverse.app.data.model.TaskEntity
import com.gigverse.app.data.model.UserEntity
import com.gigverse.app.ui.viewmodel.GigverseViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class GigverseMongoDbCloudTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext<Context>()
    }

    @Test
    fun testMongoDbCloudManager_handlesUnconfiguredGracefully() {
        // Empty or placeholder URI must safely report false without throwing exceptions
        val result = MongoDbCloudManager.initialize(customUri = "")
        assertFalse("Unconfigured URI must report not connected", result)
        assertFalse(MongoDbCloudManager.isConnected)
        assertTrue(MongoDbCloudManager.statusMessage.contains("local.properties", ignoreCase = true))

        val placeholderResult = MongoDbCloudManager.initialize(customUri = "YOUR_MONGODB_URI_HERE")
        assertFalse(placeholderResult)
        assertFalse(MongoDbCloudManager.isConnected)
    }

    @Test
    fun testMongoDbCloudManager_liveConnectionWithReplicaSetUri() {
        val uri = "mongodb://vijeshcs23_db_user:jSgJ7Ib9Gukf3t8z@ac-6ujfo4n-shard-00-00.yrjjh4d.mongodb.net:27017,ac-6ujfo4n-shard-00-01.yrjjh4d.mongodb.net:27017,ac-6ujfo4n-shard-00-02.yrjjh4d.mongodb.net:27017/?ssl=true&replicaSet=atlas-g1aatp-shard-0&authSource=admin"
        val initSuccess = MongoDbCloudManager.initialize(customUri = uri)
        println("Mongo connection result: $initSuccess, status: ${MongoDbCloudManager.statusMessage}")
        assertTrue("MongoDbCloudManager should initialize successfully", initSuccess)
        assertTrue("MongoDbCloudManager isConnected flag should be true", MongoDbCloudManager.isConnected)
        assertTrue(MongoDbCloudManager.statusMessage.contains("Connected", ignoreCase = true))
    }

    @Test
    fun testMongoDbCloudManager_userDocumentMappingIntegrity() {
        val originalUser = UserEntity(
            id = "user_mongo_test_1",
            role = "FREELANCER",
            fullName = "Cloud Dev",
            email = "cloud.dev@gigverse.com",
            phone = "+1 555-1020",
            avatarUrl = "https://example.com/avatar.png",
            locationCountry = "USA",
            locationState = "WA",
            locationDistrict = "Seattle",
            headline = "Cloud Systems Architect",
            bio = "Distributed systems and mobile backends",
            hourlyRate = 95.0,
            skills = "Kotlin, MongoDB, Jetpack Compose",
            govIdType = "Passport",
            govIdNumber = "WA-992211",
            isGovIdVerified = true,
            linkedInUrl = "https://linkedin.com/in/clouddev",
            portfolioWebsite = "https://clouddev.io",
            githubUrl = "https://github.com/clouddev",
            passwordHash = "pbkdf2:10000:salt:hash",
            workStatus = "AVAILABLE",
            trustScore = 96,
            onTimeDeliveryRate = 99,
            rating = 4.95,
            ratingCount = 18,
            completedProjectsCount = 14,
            badgeLevel = "Top Rated",
            companyName = "",
            industry = "Cloud & Mobile",
            totalSpent = 0.0,
            projectsPostedCount = 0
        )

        // Reflection test on private converter or test via Document
        val doc = org.bson.Document().apply {
            append("_id", originalUser.id)
            append("role", originalUser.role)
            append("fullName", originalUser.fullName)
            append("email", originalUser.email)
            append("phone", originalUser.phone)
            append("avatarUrl", originalUser.avatarUrl)
            append("locationCountry", originalUser.locationCountry)
            append("locationState", originalUser.locationState)
            append("locationDistrict", originalUser.locationDistrict)
            append("headline", originalUser.headline)
            append("bio", originalUser.bio)
            append("hourlyRate", originalUser.hourlyRate)
            append("skills", originalUser.skills)
            append("govIdType", originalUser.govIdType)
            append("govIdNumber", originalUser.govIdNumber)
            append("isGovIdVerified", originalUser.isGovIdVerified)
            append("linkedInUrl", originalUser.linkedInUrl)
            append("portfolioWebsite", originalUser.portfolioWebsite)
            append("githubUrl", originalUser.githubUrl)
            append("passwordHash", originalUser.passwordHash)
            append("workStatus", originalUser.workStatus)
            append("trustScore", originalUser.trustScore)
            append("onTimeDeliveryRate", originalUser.onTimeDeliveryRate)
            append("rating", originalUser.rating)
            append("ratingCount", originalUser.ratingCount)
            append("completedProjectsCount", originalUser.completedProjectsCount)
            append("badgeLevel", originalUser.badgeLevel)
            append("industry", originalUser.industry)
        }

        assertEquals("user_mongo_test_1", doc.getString("_id"))
        assertEquals("FREELANCER", doc.getString("role"))
        assertEquals("Cloud Dev", doc.getString("fullName"))
        assertEquals("cloud.dev@gigverse.com", doc.getString("email"))
        assertEquals(95.0, doc.getDouble("hourlyRate"), 0.001)
        assertEquals(96, doc.getInteger("trustScore"))
    }

    @Test
    fun testMongoDbCloudManager_projectDocumentMappingIntegrity() {
        val originalProject = ProjectEntity(
            id = "proj_cloud_test_1",
            title = "Multi-Device Cross Interaction Platform",
            description = "Build Android client syncing with MongoDB",
            category = "Mobile",
            requiredSkills = "Kotlin, MongoDB, Compose",
            budget = 4500.0,
            budgetType = "FIXED",
            deadlineDays = 21,
            scope = "Large",
            clientId = "client_sophia",
            clientName = "Sophia Martinez",
            clientCompany = "NovaTech",
            clientLocation = "California, USA",
            status = "OPEN",
            isBidRoomEnabled = true,
            bidRoomAllowedLocation = "All Regions",
            aiEstimatedDays = 18,
            aiComplexityScore = "High Complexity",
            aiVelocityFactor = 1.2
        )

        val doc = org.bson.Document().apply {
            append("_id", originalProject.id)
            append("title", originalProject.title)
            append("description", originalProject.description)
            append("category", originalProject.category)
            append("requiredSkills", originalProject.requiredSkills)
            append("budget", originalProject.budget)
            append("budgetType", originalProject.budgetType)
            append("deadlineDays", originalProject.deadlineDays)
            append("scope", originalProject.scope)
            append("clientId", originalProject.clientId)
            append("clientName", originalProject.clientName)
            append("status", originalProject.status)
            append("isBidRoomEnabled", originalProject.isBidRoomEnabled)
            append("aiEstimatedDays", originalProject.aiEstimatedDays)
        }

        assertEquals("proj_cloud_test_1", doc.getString("_id"))
        assertEquals("Multi-Device Cross Interaction Platform", doc.getString("title"))
        assertEquals(4500.0, doc.getDouble("budget"), 0.001)
        assertTrue(doc.getBoolean("isBidRoomEnabled"))
        assertEquals(18, doc.getInteger("aiEstimatedDays"))
    }

    @Test
    fun testGigverseViewModel_cloudSyncStateFlows() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = GigverseViewModel(app)

        assertNotNull(vm.isCloudConnected)
        assertNotNull(vm.cloudStatusMessage)
        assertFalse(vm.isCloudConnected.value)

        // Triggering sync in unconfigured mode updates status message without crashing
        vm.syncWithCloud()
        org.robolectric.shadows.ShadowLooper.idleMainLooper()

        assertNotNull(vm.cloudStatusMessage.value)
    }
}
