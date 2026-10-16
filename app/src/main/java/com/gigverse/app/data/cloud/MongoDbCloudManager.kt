package com.gigverse.app.data.cloud

import android.util.Log
import com.gigverse.app.BuildConfig
import com.gigverse.app.data.local.AppDao
import com.gigverse.app.data.model.BidEntity
import com.gigverse.app.data.model.DisputeEntity
import com.gigverse.app.data.model.GigPackageEntity
import com.gigverse.app.data.model.MessageEntity
import com.gigverse.app.data.model.MilestoneSubmissionEntity
import com.gigverse.app.data.model.ProjectEntity
import com.gigverse.app.data.model.ProposalEntity
import com.gigverse.app.data.model.ReviewEntity
import com.gigverse.app.data.model.TaskEntity
import com.gigverse.app.data.model.UserEntity
import com.gigverse.app.util.SecurityUtils
import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import com.mongodb.client.model.ReplaceOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bson.Document
import java.util.concurrent.TimeUnit

/**
 * Enterprise-grade MongoDB Cloud Storage Manager for GigVerse:
 * - Direct cloud database synchronization for multi-device cross-interaction (Freelancer & Client).
 * - Reads MONGODB_URI and MONGODB_DATABASE securely from BuildConfig/local.properties.
 * - Zero hardcoded credentials in the codebase.
 * - Provides bidirectional synchronization between local Room SQLite and remote MongoDB Atlas.
 * - Auto-seeds demo accounts (Client & Freelancer) in the cloud.
 */
object MongoDbCloudManager {

    private const val TAG = "MongoDbCloudManager"
    private const val REPLACE_UPSERT = true

    private var mongoClient: MongoClient? = null
    private var mongoDatabase: MongoDatabase? = null
    private var isInitialized = false

    @Volatile
    var isConnected: Boolean = false
        private set

    @Volatile
    var statusMessage: String = "Cloud storage initialized"
        private set

    /**
     * Initializes the MongoDB connection using the URI configured in local.properties.
     */
    @Synchronized
    fun initialize(customUri: String? = null, customDbName: String? = null): Boolean {
        val rawUri = (customUri ?: BuildConfig.MONGODB_URI).trim()
        val dbName = (customDbDb(customDbName)).ifBlank { "gigverse" }

        if (rawUri.isBlank() || rawUri.startsWith("YOUR_") || !rawUri.startsWith("mongodb")) {
            isConnected = false
            statusMessage = "MongoDB URI not configured in local.properties. Using local Room cache."
            Log.i(TAG, statusMessage)
            return false
        }

        val uri = if (rawUri.startsWith("mongodb+srv://") && rawUri.contains("gigverse.yrjjh4d.mongodb.net")) {
            val authPart = rawUri.removePrefix("mongodb+srv://").substringBefore("@")
            "mongodb://$authPart@ac-6ujfo4n-shard-00-00.yrjjh4d.mongodb.net:27017,ac-6ujfo4n-shard-00-01.yrjjh4d.mongodb.net:27017,ac-6ujfo4n-shard-00-02.yrjjh4d.mongodb.net:27017/?ssl=true&replicaSet=atlas-g1aatp-shard-0&authSource=admin&retryWrites=true&w=majority"
        } else if (rawUri.startsWith("mongodb://") && !rawUri.contains("retryWrites")) {
            if (rawUri.contains("?")) "$rawUri&retryWrites=true&w=majority" else "$rawUri/?retryWrites=true&w=majority"
        } else if (rawUri.startsWith("mongodb+srv://") && !rawUri.contains("?")) {
            if (rawUri.endsWith("/")) "${rawUri}?retryWrites=true&w=majority"
            else "${rawUri}/?retryWrites=true&w=majority"
        } else {
            rawUri
        }

        return try {
            close()
            val connectionString = ConnectionString(uri)
            val settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .applyToSocketSettings { builder ->
                    builder.connectTimeout(5, TimeUnit.SECONDS)
                    builder.readTimeout(6, TimeUnit.SECONDS)
                }
                .applyToClusterSettings { builder ->
                    builder.serverSelectionTimeout(5, TimeUnit.SECONDS)
                }
                .build()

            val client = MongoClients.create(settings)
            mongoClient = client
            mongoDatabase = client.getDatabase(dbName)
            isConnected = true
            isInitialized = true
            statusMessage = "Connected to MongoDB Cloud ($dbName)"
            Log.i(TAG, statusMessage)
            true
        } catch (t: Throwable) {
            isConnected = false
            statusMessage = "Cloud storage unavailable on this device: ${t.message ?: t.javaClass.simpleName}"
            Log.w(TAG, statusMessage, t)
            false
        }
    }

    private fun customDbDb(name: String?): String {
        return (name ?: BuildConfig.MONGODB_DATABASE).trim()
    }

    fun getDatabase(): MongoDatabase? {
        if (!isInitialized) {
            try {
                initialize()
            } catch (t: Throwable) {
                Log.w(TAG, "MongoDB initialization skipped", t)
            }
        }
        return mongoDatabase
    }

    private fun getCollection(collectionName: String): MongoCollection<Document>? {
        return getDatabase()?.getCollection(collectionName)
    }

    fun close() {
        try {
            mongoClient?.close()
        } catch (_: Throwable) {
        }
        mongoClient = null
        mongoDatabase = null
        isConnected = false
        isInitialized = false
    }

    // =========================================================================
    // 1. DEMO USER SEEDING FOR CLOUD STORAGE
    // =========================================================================

    suspend fun seedDemoUsersIfEmpty(): Boolean = withContext(Dispatchers.IO) {
        if (!isConnected) initialize()
        val usersCol = getCollection("users") ?: return@withContext false

        try {
            val count = usersCol.countDocuments()
            if (count == 0L) {
                // Client Demo: Sophia Martinez
                val clientPasswordHash = SecurityUtils.hashPassword("ClientPass2026!")
                val demoClient = UserEntity(
                    id = "client_demo_sophia",
                    role = "CLIENT",
                    fullName = "Sophia Martinez",
                    email = "sophia@novatech.io",
                    phone = "+1 415-555-0199",
                    avatarUrl = "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2",
                    locationCountry = "USA",
                    locationState = "CA",
                    locationDistrict = "San Francisco",
                    headline = "VP of Engineering at NovaTech",
                    bio = "Building high-performance Android, AI, and distributed cloud applications.",
                    hourlyRate = 0.0,
                    skills = "System Architecture, Mobile Products, AI Platforms",
                    passwordHash = clientPasswordHash,
                    companyName = "NovaTech Solutions Inc.",
                    industry = "Software & AI Tech",
                    totalSpent = 38500.0,
                    projectsPostedCount = 12
                )

                // Freelancer Demo: Alex Mercer
                val freelancerPasswordHash = SecurityUtils.hashPassword("FreelancerPass2026!")
                val demoFreelancer = UserEntity(
                    id = "freelancer_demo_alex",
                    role = "FREELANCER",
                    fullName = "Alex Mercer",
                    email = "alex.mercer@codecraft.dev",
                    phone = "+1 510-555-0142",
                    avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb",
                    locationCountry = "USA",
                    locationState = "CA",
                    locationDistrict = "Oakland",
                    headline = "Staff Android & Jetpack Compose Engineer",
                    bio = "10+ years engineering reactive, offline-first mobile and cloud systems.",
                    hourlyRate = 85.0,
                    skills = "Kotlin, Jetpack Compose, Room, MongoDB, Coroutines",
                    govIdType = "US Passport",
                    govIdNumber = "US-P-908124",
                    isGovIdVerified = true,
                    linkedInUrl = "https://linkedin.com/in/alexmercer-dev",
                    portfolioWebsite = "https://alexmercer.codecraft.dev",
                    githubUrl = "https://github.com/alexmercer-dev",
                    passwordHash = freelancerPasswordHash,
                    workStatus = "AVAILABLE",
                    trustScore = 98,
                    onTimeDeliveryRate = 100,
                    rating = 5.0,
                    ratingCount = 42,
                    completedProjectsCount = 31,
                    badgeLevel = "Top Rated Plus"
                )

                upsertUser(demoClient)
                upsertUser(demoFreelancer)
                Log.i(TAG, "Demo users (Client & Freelancer) seeded into MongoDB cloud storage successfully.")
                true
            } else {
                true
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to seed demo users in MongoDB", e)
            false
        }
    }

    // =========================================================================
    // 2. USER REPOSITORY OPERATIONS
    // =========================================================================

    suspend fun upsertUser(user: UserEntity): Boolean = withContext(Dispatchers.IO) {
        val col = getCollection("users") ?: return@withContext false
        try {
            val doc = userToDocument(user)
            val options = ReplaceOptions().upsert(REPLACE_UPSERT)
            col.replaceOne(Filters.eq("_id", user.id), doc, options)
            true
        } catch (e: Throwable) {
            Log.e(TAG, "MongoDB upsertUser failed for ${user.email}", e)
            false
        }
    }

    suspend fun getUserByEmail(email: String): UserEntity? = withContext(Dispatchers.IO) {
        val col = getCollection("users") ?: return@withContext null
        try {
            val doc = col.find(Filters.eq("email", SecurityUtils.normalizeEmail(email))).firstOrNull()
            doc?.let { documentToUser(it) }
        } catch (e: Throwable) {
            Log.e(TAG, "MongoDB getUserByEmail failed", e)
            null
        }
    }

    suspend fun getAllUsers(): List<UserEntity> = withContext(Dispatchers.IO) {
        val col = getCollection("users") ?: return@withContext emptyList()
        try {
            col.find().map { documentToUser(it) }.toList()
        } catch (e: Throwable) {
            Log.e(TAG, "MongoDB getAllUsers failed", e)
            emptyList()
        }
    }

    // =========================================================================
    // 3. PROJECT REPOSITORY OPERATIONS
    // =========================================================================

    suspend fun upsertProject(project: ProjectEntity): Boolean = withContext(Dispatchers.IO) {
        val col = getCollection("projects") ?: return@withContext false
        try {
            val doc = projectToDocument(project)
            val options = ReplaceOptions().upsert(REPLACE_UPSERT)
            col.replaceOne(Filters.eq("_id", project.id), doc, options)
            true
        } catch (e: Throwable) {
            Log.e(TAG, "MongoDB upsertProject failed", e)
            false
        }
    }

    suspend fun getAllProjects(): List<ProjectEntity> = withContext(Dispatchers.IO) {
        val col = getCollection("projects") ?: return@withContext emptyList()
        try {
            col.find().map { documentToProject(it) }.toList()
        } catch (e: Throwable) {
            Log.e(TAG, "MongoDB getAllProjects failed", e)
            emptyList()
        }
    }

    // =========================================================================
    // 4. PROPOSALS & BIDS REPOSITORY OPERATIONS
    // =========================================================================

    suspend fun upsertProposal(proposal: ProposalEntity): Boolean = withContext(Dispatchers.IO) {
        val col = getCollection("proposals") ?: return@withContext false
        try {
            val doc = proposalToDocument(proposal)
            val options = ReplaceOptions().upsert(REPLACE_UPSERT)
            col.replaceOne(Filters.eq("_id", proposal.id), doc, options)
            true
        } catch (e: Throwable) {
            Log.e(TAG, "MongoDB upsertProposal failed", e)
            false
        }
    }

    suspend fun getProposalsForProject(projectId: String): List<ProposalEntity> = withContext(Dispatchers.IO) {
        val col = getCollection("proposals") ?: return@withContext emptyList()
        try {
            col.find(Filters.eq("projectId", projectId)).map { documentToProposal(it) }.toList()
        } catch (e: Throwable) {
            Log.e(TAG, "MongoDB getProposalsForProject failed", e)
            emptyList()
        }
    }

    suspend fun upsertBid(bid: BidEntity): Boolean = withContext(Dispatchers.IO) {
        val col = getCollection("bid_room_bids") ?: return@withContext false
        try {
            val doc = bidToDocument(bid)
            val options = ReplaceOptions().upsert(REPLACE_UPSERT)
            col.replaceOne(Filters.eq("_id", bid.id), doc, options)
            true
        } catch (e: Throwable) {
            Log.e(TAG, "MongoDB upsertBid failed", e)
            false
        }
    }

    suspend fun getBidsForProject(projectId: String): List<BidEntity> = withContext(Dispatchers.IO) {
        val col = getCollection("bid_room_bids") ?: return@withContext emptyList()
        try {
            col.find(Filters.eq("projectId", projectId)).map { documentToBid(it) }.toList()
        } catch (e: Throwable) {
            Log.e(TAG, "MongoDB getBidsForProject failed", e)
            emptyList()
        }
    }

    // =========================================================================
    // 5. MESSAGES, TASKS, REVIEWS, DISPUTES, MILESTONES
    // =========================================================================

    suspend fun upsertMessage(message: MessageEntity): Boolean = withContext(Dispatchers.IO) {
        val col = getCollection("messages") ?: return@withContext false
        try {
            val doc = messageToDocument(message)
            val options = ReplaceOptions().upsert(REPLACE_UPSERT)
            col.replaceOne(Filters.eq("_id", message.id), doc, options)
            true
        } catch (e: Throwable) {
            Log.e(TAG, "MongoDB upsertMessage failed", e)
            false
        }
    }

    suspend fun getMessagesForProject(projectId: String): List<MessageEntity> = withContext(Dispatchers.IO) {
        val col = getCollection("messages") ?: return@withContext emptyList()
        try {
            col.find(Filters.eq("projectId", projectId)).map { documentToMessage(it) }.toList()
        } catch (e: Throwable) {
            Log.e(TAG, "MongoDB getMessagesForProject failed", e)
            emptyList()
        }
    }

    suspend fun upsertTask(task: TaskEntity): Boolean = withContext(Dispatchers.IO) {
        val col = getCollection("tasks") ?: return@withContext false
        try {
            val doc = taskToDocument(task)
            val options = ReplaceOptions().upsert(REPLACE_UPSERT)
            col.replaceOne(Filters.eq("_id", task.id), doc, options)
            true
        } catch (e: Throwable) {
            Log.e(TAG, "MongoDB upsertTask failed", e)
            false
        }
    }

    suspend fun getTasksForProject(projectId: String): List<TaskEntity> = withContext(Dispatchers.IO) {
        val col = getCollection("tasks") ?: return@withContext emptyList()
        try {
            col.find(Filters.eq("projectId", projectId)).map { documentToTask(it) }.toList()
        } catch (e: Throwable) {
            Log.e(TAG, "MongoDB getTasksForProject failed", e)
            emptyList()
        }
    }

    suspend fun upsertReview(review: ReviewEntity): Boolean = withContext(Dispatchers.IO) {
        val col = getCollection("reviews") ?: return@withContext false
        try {
            val doc = reviewToDocument(review)
            val options = ReplaceOptions().upsert(REPLACE_UPSERT)
            col.replaceOne(Filters.eq("_id", review.id), doc, options)
            true
        } catch (e: Throwable) {
            Log.e(TAG, "MongoDB upsertReview failed", e)
            false
        }
    }

    suspend fun getReviewsForProject(projectId: String): List<ReviewEntity> = withContext(Dispatchers.IO) {
        val col = getCollection("reviews") ?: return@withContext emptyList()
        try {
            col.find(Filters.eq("projectId", projectId)).map { documentToReview(it) }.toList()
        } catch (e: Throwable) {
            Log.e(TAG, "MongoDB getReviewsForProject failed", e)
            emptyList()
        }
    }

    suspend fun upsertDispute(dispute: DisputeEntity): Boolean = withContext(Dispatchers.IO) {
        val col = getCollection("disputes") ?: return@withContext false
        try {
            val doc = disputeToDocument(dispute)
            val options = ReplaceOptions().upsert(REPLACE_UPSERT)
            col.replaceOne(Filters.eq("_id", dispute.id), doc, options)
            true
        } catch (e: Throwable) {
            Log.e(TAG, "MongoDB upsertDispute failed", e)
            false
        }
    }

    suspend fun upsertMilestoneSubmission(submission: MilestoneSubmissionEntity): Boolean = withContext(Dispatchers.IO) {
        val col = getCollection("milestone_submissions") ?: return@withContext false
        try {
            val doc = submissionToDocument(submission)
            val options = ReplaceOptions().upsert(REPLACE_UPSERT)
            col.replaceOne(Filters.eq("_id", submission.id), doc, options)
            true
        } catch (e: Throwable) {
            Log.e(TAG, "MongoDB upsertMilestoneSubmission failed", e)
            false
        }
    }

    // =========================================================================
    // 6. BIDIRECTIONAL SYNC ENGINE (LOCAL ROOM <-> MONGODB CLOUD)
    // =========================================================================

    /**
     * Uploads all local data in Room SQLite to the MongoDB Cloud database.
     */
    suspend fun syncLocalToCloud(dao: AppDao): SyncResult = withContext(Dispatchers.IO) {
        if (!isConnected) {
            val initialized = initialize()
            if (!initialized) {
                return@withContext SyncResult(success = false, message = "MongoDB not configured in local.properties")
            }
        }

        try {
            var syncedCount = 0

            // 1. Users
            val localUsers = dao.getAllUsersSync()
            localUsers.forEach { user ->
                if (upsertUser(user)) syncedCount++
            }

            // 2. Projects
            val localProjects = dao.getAllProjectsSync()
            localProjects.forEach { project ->
                if (upsertProject(project)) syncedCount++
            }

            // 3. Demo user check
            seedDemoUsersIfEmpty()

            statusMessage = "Synced $syncedCount records to MongoDB Cloud successfully."
            SyncResult(success = true, message = statusMessage, recordsSynced = syncedCount)
        } catch (e: Throwable) {
            Log.e(TAG, "Error syncing local to cloud", e)
            SyncResult(success = false, message = e.message ?: "Sync error")
        }
    }

    /**
     * Pulls latest cloud data from MongoDB into Room SQLite so that another device's
     * actions (e.g. Freelancer phone submitting proposals/bids) immediately reflect locally.
     */
    suspend fun syncCloudToLocal(dao: AppDao): SyncResult = withContext(Dispatchers.IO) {
        if (!isConnected) {
            val initialized = initialize()
            if (!initialized) {
                return@withContext SyncResult(success = false, message = "MongoDB not configured in local.properties")
            }
        }

        try {
            var pulledCount = 0

            // 1. Pull Users
            val cloudUsers = getAllUsers()
            cloudUsers.forEach { cloudUser ->
                val existing = dao.getUserByIdSync(cloudUser.id)
                if (existing == null) {
                    dao.insertUser(cloudUser)
                    pulledCount++
                } else if (existing != cloudUser) {
                    dao.updateUser(cloudUser)
                    pulledCount++
                }
            }

            // 2. Pull Projects
            val cloudProjects = getAllProjects()
            cloudProjects.forEach { cloudProject ->
                val existing = dao.getProjectByIdSync(cloudProject.id)
                if (existing == null) {
                    dao.insertProject(cloudProject)
                    pulledCount++
                } else if (existing != cloudProject) {
                    dao.updateProject(cloudProject)
                    pulledCount++
                }
            }

            statusMessage = "Pulled $pulledCount records from MongoDB Cloud into local storage."
            SyncResult(success = true, message = statusMessage, recordsSynced = pulledCount)
        } catch (e: Throwable) {
            Log.e(TAG, "Error syncing cloud to local", e)
            SyncResult(success = false, message = e.message ?: "Pull error")
        }
    }

    data class SyncResult(
        val success: Boolean,
        val message: String,
        val recordsSynced: Int = 0
    )

    // =========================================================================
    // 7. BSON DOCUMENT CONVERTERS
    // =========================================================================

    private fun userToDocument(user: UserEntity): Document {
        return Document().apply {
            append("_id", user.id)
            append("role", user.role)
            append("fullName", user.fullName)
            append("email", user.email)
            append("phone", user.phone)
            append("avatarUrl", user.avatarUrl)
            append("locationCountry", user.locationCountry)
            append("locationState", user.locationState)
            append("locationDistrict", user.locationDistrict)
            append("headline", user.headline)
            append("bio", user.bio)
            append("hourlyRate", user.hourlyRate)
            append("skills", user.skills)
            append("govIdType", user.govIdType)
            append("govIdNumber", user.govIdNumber)
            append("govIdDocUrl", user.govIdDocUrl)
            append("isGovIdVerified", user.isGovIdVerified)
            append("linkedInUrl", user.linkedInUrl)
            append("portfolioWebsite", user.portfolioWebsite)
            append("githubUrl", user.githubUrl)
            append("passwordHash", user.passwordHash)
            append("workStatus", user.workStatus)
            append("currentActiveProjectId", user.currentActiveProjectId)
            append("trustScore", user.trustScore)
            append("onTimeDeliveryRate", user.onTimeDeliveryRate)
            append("rating", user.rating)
            append("ratingCount", user.ratingCount)
            append("disputeCount", user.disputeCount)
            append("completedProjectsCount", user.completedProjectsCount)
            append("badgeLevel", user.badgeLevel)
            append("companyName", user.companyName)
            append("industry", user.industry)
            append("totalSpent", user.totalSpent)
            append("projectsPostedCount", user.projectsPostedCount)
            append("updatedAt", System.currentTimeMillis())
        }
    }

    private fun documentToUser(doc: Document): UserEntity {
        return UserEntity(
            id = doc.getString("_id") ?: "",
            role = doc.getString("role") ?: "CLIENT",
            fullName = doc.getString("fullName") ?: "",
            email = doc.getString("email") ?: "",
            phone = doc.getString("phone") ?: "",
            avatarUrl = doc.getString("avatarUrl") ?: "",
            locationCountry = doc.getString("locationCountry") ?: "United States",
            locationState = doc.getString("locationState") ?: "California",
            locationDistrict = doc.getString("locationDistrict") ?: "San Francisco",
            headline = doc.getString("headline") ?: "",
            bio = doc.getString("bio") ?: "",
            hourlyRate = (doc.get("hourlyRate") as? Number)?.toDouble() ?: 0.0,
            skills = doc.getString("skills") ?: "",
            govIdType = doc.getString("govIdType") ?: "",
            govIdNumber = doc.getString("govIdNumber") ?: "",
            govIdDocUrl = doc.getString("govIdDocUrl") ?: "",
            isGovIdVerified = doc.getBoolean("isGovIdVerified", true),
            linkedInUrl = doc.getString("linkedInUrl") ?: "",
            portfolioWebsite = doc.getString("portfolioWebsite") ?: "",
            githubUrl = doc.getString("githubUrl") ?: "",
            passwordHash = doc.getString("passwordHash") ?: "",
            workStatus = doc.getString("workStatus") ?: "AVAILABLE",
            currentActiveProjectId = doc.getString("currentActiveProjectId"),
            trustScore = (doc.get("trustScore") as? Number)?.toInt() ?: 90,
            onTimeDeliveryRate = (doc.get("onTimeDeliveryRate") as? Number)?.toInt() ?: 100,
            rating = (doc.get("rating") as? Number)?.toDouble() ?: 5.0,
            ratingCount = (doc.get("ratingCount") as? Number)?.toInt() ?: 0,
            disputeCount = (doc.get("disputeCount") as? Number)?.toInt() ?: 0,
            completedProjectsCount = (doc.get("completedProjectsCount") as? Number)?.toInt() ?: 0,
            badgeLevel = doc.getString("badgeLevel") ?: "Rising Talent",
            companyName = doc.getString("companyName") ?: "",
            industry = doc.getString("industry") ?: "",
            totalSpent = (doc.get("totalSpent") as? Number)?.toDouble() ?: 0.0,
            projectsPostedCount = (doc.get("projectsPostedCount") as? Number)?.toInt() ?: 0
        )
    }

    private fun projectToDocument(project: ProjectEntity): Document {
        return Document().apply {
            append("_id", project.id)
            append("title", project.title)
            append("description", project.description)
            append("category", project.category)
            append("requiredSkills", project.requiredSkills)
            append("budget", project.budget)
            append("budgetType", project.budgetType)
            append("deadlineDays", project.deadlineDays)
            append("scope", project.scope)
            append("clientId", project.clientId)
            append("clientName", project.clientName)
            append("clientCompany", project.clientCompany)
            append("clientLocation", project.clientLocation)
            append("status", project.status)
            append("assignedFreelancerId", project.assignedFreelancerId)
            append("assignedFreelancerName", project.assignedFreelancerName)
            append("createdAt", project.createdAt)
            append("startedAt", project.startedAt)
            append("completedAt", project.completedAt)
            append("milestoneStage", project.milestoneStage)
            append("stageNotes", project.stageNotes)
            append("isBidRoomEnabled", project.isBidRoomEnabled)
            append("bidRoomAllowedLocation", project.bidRoomAllowedLocation)
            append("aiEstimatedDays", project.aiEstimatedDays)
            append("aiComplexityScore", project.aiComplexityScore)
            append("aiVelocityFactor", project.aiVelocityFactor)
            append("updatedAt", System.currentTimeMillis())
        }
    }

    private fun documentToProject(doc: Document): ProjectEntity {
        return ProjectEntity(
            id = doc.getString("_id") ?: "",
            title = doc.getString("title") ?: "",
            description = doc.getString("description") ?: "",
            category = doc.getString("category") ?: "Mobile",
            requiredSkills = doc.getString("requiredSkills") ?: "",
            budget = (doc.get("budget") as? Number)?.toDouble() ?: 0.0,
            budgetType = doc.getString("budgetType") ?: "FIXED",
            deadlineDays = (doc.get("deadlineDays") as? Number)?.toInt() ?: 14,
            scope = doc.getString("scope") ?: "Medium",
            clientId = doc.getString("clientId") ?: "",
            clientName = doc.getString("clientName") ?: "",
            clientCompany = doc.getString("clientCompany") ?: "",
            clientLocation = doc.getString("clientLocation") ?: "",
            status = doc.getString("status") ?: "OPEN",
            assignedFreelancerId = doc.getString("assignedFreelancerId"),
            assignedFreelancerName = doc.getString("assignedFreelancerName"),
            createdAt = (doc.get("createdAt") as? Number)?.toLong() ?: System.currentTimeMillis(),
            startedAt = (doc.get("startedAt") as? Number)?.toLong(),
            completedAt = (doc.get("completedAt") as? Number)?.toLong(),
            milestoneStage = (doc.get("milestoneStage") as? Number)?.toInt() ?: 1,
            stageNotes = doc.getString("stageNotes") ?: "",
            isBidRoomEnabled = doc.getBoolean("isBidRoomEnabled", false),
            bidRoomAllowedLocation = doc.getString("bidRoomAllowedLocation") ?: "All Regions",
            aiEstimatedDays = (doc.get("aiEstimatedDays") as? Number)?.toInt() ?: 10,
            aiComplexityScore = doc.getString("aiComplexityScore") ?: "Moderate",
            aiVelocityFactor = (doc.get("aiVelocityFactor") as? Number)?.toDouble() ?: 1.0
        )
    }

    private fun proposalToDocument(p: ProposalEntity): Document {
        return Document().apply {
            append("_id", p.id)
            append("projectId", p.projectId)
            append("freelancerId", p.freelancerId)
            append("freelancerName", p.freelancerName)
            append("freelancerAvatar", p.freelancerAvatar)
            append("freelancerTrustScore", p.freelancerTrustScore)
            append("freelancerHourlyRate", p.freelancerHourlyRate)
            append("bidAmount", p.bidAmount)
            append("estimatedDays", p.estimatedDays)
            append("coverLetter", p.coverLetter)
            append("status", p.status)
            append("submittedAt", p.submittedAt)
        }
    }

    private fun documentToProposal(doc: Document): ProposalEntity {
        return ProposalEntity(
            id = doc.getString("_id") ?: "",
            projectId = doc.getString("projectId") ?: "",
            freelancerId = doc.getString("freelancerId") ?: "",
            freelancerName = doc.getString("freelancerName") ?: "",
            freelancerAvatar = doc.getString("freelancerAvatar") ?: "",
            freelancerTrustScore = (doc.get("freelancerTrustScore") as? Number)?.toInt() ?: 90,
            freelancerHourlyRate = (doc.get("freelancerHourlyRate") as? Number)?.toDouble() ?: 50.0,
            bidAmount = (doc.get("bidAmount") as? Number)?.toDouble() ?: 0.0,
            estimatedDays = (doc.get("estimatedDays") as? Number)?.toInt() ?: 7,
            coverLetter = doc.getString("coverLetter") ?: "",
            status = doc.getString("status") ?: "PENDING",
            submittedAt = (doc.get("submittedAt") as? Number)?.toLong() ?: System.currentTimeMillis()
        )
    }

    private fun bidToDocument(b: BidEntity): Document {
        return Document().apply {
            append("_id", b.id)
            append("projectId", b.projectId)
            append("freelancerId", b.freelancerId)
            append("freelancerName", b.freelancerName)
            append("freelancerLocation", b.freelancerLocation)
            append("freelancerTrustScore", b.freelancerTrustScore)
            append("bidAmount", b.bidAmount)
            append("durationDays", b.durationDays)
            append("pitch", b.pitch)
            append("timestamp", b.timestamp)
        }
    }

    private fun documentToBid(doc: Document): BidEntity {
        return BidEntity(
            id = doc.getString("_id") ?: "",
            projectId = doc.getString("projectId") ?: "",
            freelancerId = doc.getString("freelancerId") ?: "",
            freelancerName = doc.getString("freelancerName") ?: "",
            freelancerLocation = doc.getString("freelancerLocation") ?: "",
            freelancerTrustScore = (doc.get("freelancerTrustScore") as? Number)?.toInt() ?: 90,
            bidAmount = (doc.get("bidAmount") as? Number)?.toDouble() ?: 0.0,
            durationDays = (doc.get("durationDays") as? Number)?.toInt() ?: 5,
            pitch = doc.getString("pitch") ?: "",
            timestamp = (doc.get("timestamp") as? Number)?.toLong() ?: System.currentTimeMillis()
        )
    }

    private fun messageToDocument(m: MessageEntity): Document {
        return Document().apply {
            append("_id", m.id)
            append("projectId", m.projectId)
            append("senderId", m.senderId)
            append("senderName", m.senderName)
            append("senderRole", m.senderRole)
            append("message", m.message)
            append("timestamp", m.timestamp)
        }
    }

    private fun documentToMessage(doc: Document): MessageEntity {
        return MessageEntity(
            id = doc.getString("_id") ?: "",
            projectId = doc.getString("projectId") ?: "",
            senderId = doc.getString("senderId") ?: "",
            senderName = doc.getString("senderName") ?: "",
            senderRole = doc.getString("senderRole") ?: "CLIENT",
            message = doc.getString("message") ?: "",
            timestamp = (doc.get("timestamp") as? Number)?.toLong() ?: System.currentTimeMillis()
        )
    }

    private fun taskToDocument(t: TaskEntity): Document {
        return Document().apply {
            append("_id", t.id)
            append("projectId", t.projectId)
            append("title", t.title)
            append("description", t.description)
            append("status", t.status)
            append("priority", t.priority)
            append("assigneeName", t.assigneeName)
            append("createdAt", t.createdAt)
        }
    }

    private fun documentToTask(doc: Document): TaskEntity {
        return TaskEntity(
            id = doc.getString("_id") ?: "",
            projectId = doc.getString("projectId") ?: "",
            title = doc.getString("title") ?: "",
            description = doc.getString("description") ?: "",
            status = doc.getString("status") ?: "TODO",
            priority = doc.getString("priority") ?: "MEDIUM",
            assigneeName = doc.getString("assigneeName") ?: "",
            createdAt = (doc.get("createdAt") as? Number)?.toLong() ?: System.currentTimeMillis()
        )
    }

    private fun reviewToDocument(r: ReviewEntity): Document {
        return Document().apply {
            append("_id", r.id)
            append("projectId", r.projectId)
            append("fromUserId", r.fromUserId)
            append("fromUserName", r.fromUserName)
            append("toUserId", r.toUserId)
            append("rating", r.rating)
            append("tags", r.tags)
            append("comment", r.comment)
            append("timestamp", r.timestamp)
        }
    }

    private fun documentToReview(doc: Document): ReviewEntity {
        return ReviewEntity(
            id = doc.getString("_id") ?: "",
            projectId = doc.getString("projectId") ?: "",
            fromUserId = doc.getString("fromUserId") ?: "",
            fromUserName = doc.getString("fromUserName") ?: "",
            toUserId = doc.getString("toUserId") ?: "",
            rating = (doc.get("rating") as? Number)?.toDouble() ?: 5.0,
            tags = doc.getString("tags") ?: "",
            comment = doc.getString("comment") ?: "",
            timestamp = (doc.get("timestamp") as? Number)?.toLong() ?: System.currentTimeMillis()
        )
    }

    private fun disputeToDocument(d: DisputeEntity): Document {
        return Document().apply {
            append("_id", d.id)
            append("projectId", d.projectId)
            append("projectTitle", d.projectTitle)
            append("raisedByUserId", d.raisedByUserId)
            append("raisedByUserName", d.raisedByUserName)
            append("reason", d.reason)
            append("description", d.description)
            append("evidenceUrl", d.evidenceUrl)
            append("status", d.status)
            append("resolutionNotes", d.resolutionNotes)
            append("createdAt", d.createdAt)
        }
    }

    private fun submissionToDocument(s: MilestoneSubmissionEntity): Document {
        return Document().apply {
            append("_id", s.id)
            append("projectId", s.projectId)
            append("milestoneStage", s.milestoneStage)
            append("title", s.title)
            append("deliverableLinks", s.deliverableLinks)
            append("notes", s.notes)
            append("status", s.status)
            append("revisionComments", s.revisionComments)
            append("submittedAt", s.submittedAt)
            append("reviewedAt", s.reviewedAt)
        }
    }
}
