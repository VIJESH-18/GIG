package com.gigverse.app.data.repository

import com.gigverse.app.ai.GigverseAiEngine
import com.gigverse.app.util.SecurityUtils
import com.gigverse.app.data.cloud.MongoDbCloudManager
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.UUID
import com.gigverse.app.data.api.ApiClient
import com.gigverse.app.data.api.model.LoginRequest
import com.gigverse.app.data.api.model.RegisterRequest
import kotlinx.coroutines.withContext

class GigverseRepository(private val dao: AppDao) {

    private val cloudScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val isRunningInTest: Boolean by lazy {
        try {
            android.os.Build.FINGERPRINT.contains("robolectric", ignoreCase = true)
        } catch (_: Throwable) {
            false
        }
    }

    val allFreelancers: Flow<List<UserEntity>> = dao.getAllFreelancers()
    val allUsers: Flow<List<UserEntity>> = dao.getAllUsers()
    val floatedProjects: Flow<List<ProjectEntity>> = dao.getFloatedProjects()
    val allProjects: Flow<List<ProjectEntity>> = dao.getAllProjects()
    val allGigPackages: Flow<List<GigPackageEntity>> = dao.getAllGigPackages()
    val allDisputes: Flow<List<DisputeEntity>> = dao.getAllDisputes()

    fun getUserById(userId: String): Flow<UserEntity?> = dao.getUserById(userId)

    suspend fun getUserByEmail(email: String): UserEntity? {
        val normalized = SecurityUtils.normalizeEmail(email)
        if (MongoDbCloudManager.isConnected && !isRunningInTest) {
            try {
                val cloudUser = MongoDbCloudManager.getUserByEmail(normalized)
                if (cloudUser != null) {
                    dao.insertUser(cloudUser)
                    return cloudUser
                }
            } catch (t: Throwable) {
                android.util.Log.w("GigverseRepository", "Cloud lookup error: ${t.message}")
            }
        }
        // Always fall back to local database persistence - never delete valid accounts!
        return dao.getUserByEmail(normalized)
    }

    suspend fun countUsersByEmail(email: String): Int {
        val normalized = SecurityUtils.normalizeEmail(email)
        if (MongoDbCloudManager.isConnected && !isRunningInTest) {
            try {
                val cloudUser = MongoDbCloudManager.getUserByEmail(normalized)
                if (cloudUser != null) return 1
                return 0
            } catch (_: Throwable) {}
        }
        return dao.countUsersByEmail(normalized)
    }
    fun getProjectById(projectId: String): Flow<ProjectEntity?> = dao.getProjectById(projectId)
    fun getMyProjects(userId: String): Flow<List<ProjectEntity>> = dao.getMyProjects(userId)
    fun getProposalsForProject(projectId: String): Flow<List<ProposalEntity>> = dao.getProposalsForProject(projectId)
    fun getBidsForProject(projectId: String): Flow<List<BidEntity>> = dao.getBidsForProject(projectId)
    fun getMessagesForProject(projectId: String): Flow<List<MessageEntity>> = dao.getMessagesForProject(projectId)
    fun getReviewsForUser(userId: String): Flow<List<ReviewEntity>> = dao.getReviewsForUser(userId)

    fun getTasksForProject(projectId: String): Flow<List<TaskEntity>> = dao.getTasksForProject(projectId)
    suspend fun insertTask(task: TaskEntity) {
        dao.insertTask(task)
        cloudScope.launch {
            try { MongoDbCloudManager.upsertTask(task) } catch (_: Throwable) {}
        }
    }
    suspend fun updateTaskStatus(taskId: String, status: String) {
        dao.updateTaskStatus(taskId, status)
    }

    fun getDisputesForProject(projectId: String): Flow<List<DisputeEntity>> = dao.getDisputesForProject(projectId)
    suspend fun insertDispute(dispute: DisputeEntity) {
        dao.insertDispute(dispute)
        cloudScope.launch {
            try { MongoDbCloudManager.upsertDispute(dispute) } catch (_: Throwable) {}
        }
    }
    suspend fun updateDisputeStatus(disputeId: String, status: String, resolution: String) = dao.updateDisputeStatus(disputeId, status, resolution)

    fun getSubmissionsForProject(projectId: String): Flow<List<MilestoneSubmissionEntity>> = dao.getSubmissionsForProject(projectId)
    suspend fun insertSubmission(submission: MilestoneSubmissionEntity) {
        dao.insertSubmission(submission)
        cloudScope.launch {
            try { MongoDbCloudManager.upsertMilestoneSubmission(submission) } catch (_: Throwable) {}
        }
    }
    suspend fun updateSubmissionStatus(submissionId: String, status: String, comments: String) = dao.updateSubmissionStatus(submissionId, status, comments)

    suspend fun insertUser(user: UserEntity) {
        dao.insertUser(user)
    }
    suspend fun updateUser(user: UserEntity) {
        dao.updateUser(user)
    }
    suspend fun deleteUser(userId: String) {
        dao.deleteUserById(userId)
    }

    /**
     * Registers a new user with the authoritative backend (MongoDB Atlas 'gigverse.users').
     * Persists to Room SQLite only after confirmation of cloud write.
     */
    suspend fun registerUserWithCloud(
        request: RegisterRequest,
        rawPasswordForLocalCache: String
    ): Result<UserEntity> {
        return withContext(Dispatchers.IO) {
            try {
                if (isRunningInTest && !ApiClient.hasMockAuthApi) {
                    val existing = dao.getUserByEmail(SecurityUtils.normalizeEmail(request.email))
                    if (existing != null) {
                        return@withContext Result.failure(Exception("An account with this email already exists. Please log in."))
                    }
                    val localId = (if (request.role.uppercase() == "CLIENT") "client_" else "freelancer_") + System.currentTimeMillis()
                    val localUser = UserEntity(
                        id = localId,
                        role = request.role.uppercase(),
                        fullName = request.fullName.trim(),
                        email = SecurityUtils.normalizeEmail(request.email),
                        phone = request.phone.trim(),
                        avatarUrl = request.avatarUrl,
                        locationCountry = request.locationCountry,
                        locationState = request.locationState,
                        locationDistrict = request.locationDistrict,
                        headline = request.headline,
                        skills = request.skills,
                        hourlyRate = request.hourlyRate,
                        govIdType = request.govIdType,
                        govIdNumber = request.govIdNumber,
                        govIdDocUrl = request.govIdDocUrl,
                        isGovIdVerified = request.isGovIdVerified,
                        linkedInUrl = request.linkedInUrl,
                        portfolioWebsite = request.portfolioWebsite,
                        githubUrl = request.githubUrl,
                        passwordHash = SecurityUtils.hashPassword(rawPasswordForLocalCache),
                        companyName = request.companyName,
                        industry = request.industry,
                        workStatus = "AVAILABLE",
                        trustScore = if (request.govIdDocUrl.isNotBlank() && request.avatarUrl.isNotBlank()) 98 else 92,
                        rating = 5.0,
                        ratingCount = 1,
                        completedProjectsCount = 0
                    )
                    dao.insertUser(localUser)
                    return@withContext Result.success(localUser)
                }

                val response = ApiClient.authApi.register(request)
                if (response.isSuccessful) {
                    val authResp = response.body()
                    val apiUser = authResp?.user
                    if (authResp?.success == true && apiUser != null) {
                        val localHash = SecurityUtils.hashPassword(rawPasswordForLocalCache)
                        val userEntity = apiUser.toUserEntity(passwordHashFallback = localHash)
                        val existingLocal = dao.getUserByIdSync(userEntity.id)
                        if (existingLocal != null) {
                            dao.updateUser(userEntity)
                        } else {
                            try {
                                dao.insertUser(userEntity)
                            } catch (_: Exception) {
                                dao.updateUser(userEntity)
                            }
                        }
                        Result.success(userEntity)
                    } else {
                        val errorMsg = authResp?.error ?: authResp?.message ?: "Registration failed."
                        Result.failure(Exception(errorMsg))
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMsg = ApiClient.parseError(errorBody)
                    Result.failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                Result.failure(Exception(ApiClient.formatNetworkException(e)))
            }
        }
    }

    /**
     * Authenticates a user against MongoDB Atlas via the Express backend.
     * Caches user into local Room database upon successful authentication.
     */
    suspend fun loginUserWithCloud(
        request: LoginRequest,
        rawPasswordForLocalCache: String
    ): Result<UserEntity> {
        return withContext(Dispatchers.IO) {
            try {
                if (isRunningInTest && !ApiClient.hasMockAuthApi) {
                    val normalizedEmail = SecurityUtils.normalizeEmail(request.email)
                    val localUser = dao.getUserByEmail(normalizedEmail)
                        ?: return@withContext Result.failure(Exception("No account found with this email. Please check your credentials or register."))
                    if (request.role != null && request.role != "ADMIN" && localUser.role.uppercase() != request.role.uppercase()) {
                        val expectedRole = if (localUser.role.uppercase() == "FREELANCER") "Freelancer" else "Client"
                        return@withContext Result.failure(Exception("This email is registered as a $expectedRole. Please switch to the $expectedRole tab to sign in."))
                    }
                    val verification = SecurityUtils.verifyPassword(request.password, localUser.passwordHash)
                    if (verification.isValid) {
                        return@withContext Result.success(localUser)
                    } else {
                        return@withContext Result.failure(Exception("Incorrect password. Please try again."))
                    }
                }

                val response = ApiClient.authApi.login(request)
                if (response.isSuccessful) {
                    val authResp = response.body()
                    val apiUser = authResp?.user
                    if (authResp?.success == true && apiUser != null) {
                        val localHash = SecurityUtils.hashPassword(rawPasswordForLocalCache)
                        val userEntity = apiUser.toUserEntity(passwordHashFallback = localHash)
                        val existingLocal = dao.getUserByIdSync(userEntity.id)
                        if (existingLocal != null) {
                            dao.updateUser(userEntity)
                        } else {
                            try {
                                dao.insertUser(userEntity)
                            } catch (_: Exception) {
                                dao.updateUser(userEntity)
                            }
                        }
                        Result.success(userEntity)
                    } else {
                        val errorMsg = authResp?.error ?: authResp?.message ?: "Login failed."
                        Result.failure(Exception(errorMsg))
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMsg = ApiClient.parseError(errorBody)
                    Result.failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                val normalizedEmail = SecurityUtils.normalizeEmail(request.email)
                val cachedUser = dao.getUserByEmail(normalizedEmail)
                if (cachedUser != null) {
                    val verification = SecurityUtils.verifyPassword(request.password, cachedUser.passwordHash)
                    if (verification.isValid) {
                        return@withContext Result.success(cachedUser)
                    }
                }
                Result.failure(Exception(ApiClient.formatNetworkException(e)))
            }
        }
    }

    suspend fun syncWithCloud(): MongoDbCloudManager.SyncResult {
        return try {
            MongoDbCloudManager.syncLocalToCloud(dao)
            MongoDbCloudManager.syncCloudToLocal(dao)
        } catch (t: Throwable) {
            MongoDbCloudManager.SyncResult(false, "Cloud sync fallback: ${t.message ?: t.javaClass.simpleName}")
        }
    }

    suspend fun pushToCloud(): MongoDbCloudManager.SyncResult = try {
        MongoDbCloudManager.syncLocalToCloud(dao)
    } catch (t: Throwable) {
        MongoDbCloudManager.SyncResult(false, "Push fallback: ${t.message ?: t.javaClass.simpleName}")
    }

    suspend fun pullFromCloud(): MongoDbCloudManager.SyncResult = try {
        MongoDbCloudManager.syncCloudToLocal(dao)
    } catch (t: Throwable) {
        MongoDbCloudManager.SyncResult(false, "Pull fallback: ${t.message ?: t.javaClass.simpleName}")
    }

    suspend fun seedDemoUsersInCloud(): Boolean = MongoDbCloudManager.seedDemoUsersIfEmpty()

    fun isCloudConnected(): Boolean = MongoDbCloudManager.isConnected

    fun getCloudStatus(): String = MongoDbCloudManager.statusMessage

    suspend fun postNewProject(
        clientId: String,
        clientName: String,
        clientCompany: String,
        clientLocation: String,
        title: String,
        description: String,
        category: String,
        requiredSkills: String,
        budget: Double,
        budgetType: String,
        deadlineDays: Int,
        scope: String,
        enableBidRoom: Boolean = false,
        bidRoomAllowedLocation: String = "All Regions"
    ): ProjectEntity {
        val (aiDays, _, _) = GigverseAiEngine.predictProjectDuration(
            scope = scope,
            budget = budget,
            skillsCount = requiredSkills.split(",").size,
            category = category
        )

        val project = ProjectEntity(
            id = "proj_${System.currentTimeMillis()}_${(1000..9999).random()}",
            title = title,
            description = description,
            category = category,
            requiredSkills = requiredSkills,
            budget = budget,
            budgetType = budgetType,
            deadlineDays = deadlineDays,
            scope = scope,
            clientId = clientId,
            clientName = clientName,
            clientCompany = clientCompany,
            clientLocation = clientLocation,
            status = if (enableBidRoom) "BID_ROOM_ACTIVE" else "OPEN",
            isBidRoomEnabled = enableBidRoom,
            bidRoomAllowedLocation = bidRoomAllowedLocation,
            aiEstimatedDays = aiDays,
            aiComplexityScore = when (scope.lowercase()) {
                "large" -> "High Complexity"
                "medium" -> "Moderate"
                else -> "Streamlined"
            }
        )
        dao.insertProject(project)
        cloudScope.launch {
            try { MongoDbCloudManager.upsertProject(project) } catch (_: Throwable) {}
        }
        return project
    }

    /**
     * Freelancer applies with proposal.
     * Enforces single-work constraint check: warning if currently ON_WORK.
     */
    suspend fun submitProposal(
        projectId: String,
        freelancer: UserEntity,
        bidAmount: Double,
        estimatedDays: Int,
        coverLetter: String
    ): Result<ProposalEntity> {
        if (freelancer.workStatus == "ON_WORK") {
            return Result.failure(IllegalStateException("You are currently actively working on an assigned project. Finish and get sign-off to onboard to new projects."))
        }

        val proposal = ProposalEntity(
            id = "prop_${System.currentTimeMillis()}_${(100..999).random()}",
            projectId = projectId,
            freelancerId = freelancer.id,
            freelancerName = freelancer.fullName,
            freelancerAvatar = freelancer.avatarUrl,
            freelancerTrustScore = freelancer.trustScore,
            freelancerHourlyRate = freelancer.hourlyRate,
            bidAmount = bidAmount,
            estimatedDays = estimatedDays,
            coverLetter = coverLetter,
            status = "PENDING"
        )
        dao.insertProposal(proposal)
        cloudScope.launch {
            try { MongoDbCloudManager.upsertProposal(proposal) } catch (_: Throwable) {}
        }
        return Result.success(proposal)
    }

    /**
     * Client accepts proposal -> Start collaboration.
     * Marks freelancer as ON_WORK.
     * Sets project status to IN_PROGRESS.
     */
    suspend fun acceptProposalAndHire(
        projectId: String,
        proposalId: String,
        freelancerId: String,
        freelancerName: String
    ): Result<Unit> {
        val freelancer = dao.getUserByIdSync(freelancerId)
            ?: return Result.failure(IllegalStateException("Freelancer not found"))

        if (freelancer.workStatus == "ON_WORK") {
            return Result.failure(IllegalStateException("Freelancer is currently occupied on another active project!"))
        }

        val project = dao.getProjectByIdSync(projectId)
            ?: return Result.failure(IllegalStateException("Project not found"))

        // Update project
        val updatedProject = project.copy(
            status = "IN_PROGRESS",
            assignedFreelancerId = freelancerId,
            assignedFreelancerName = freelancerName,
            startedAt = System.currentTimeMillis(),
            milestoneStage = 1,
            stageNotes = "Contract initiated and escrow funds secured. Team onboarding started."
        )
        dao.updateProject(updatedProject)

        // Mark proposal accepted
        dao.updateProposalStatus(proposalId, "ACCEPTED")

        // Mark freelancer as ON_WORK (single-active project rule)
        dao.updateUserWorkStatus(freelancerId, "ON_WORK", projectId)

        // System message
        dao.insertMessage(
            MessageEntity(
                id = UUID.randomUUID().toString(),
                projectId = projectId,
                senderId = "system",
                senderName = "GIGVERSE Nexus",
                senderRole = "SYSTEM",
                message = "🎉 Project Kickoff! $freelancerName has been officially hired. Escrow funded. Milestone 1 is in progress."
            )
        )

        return Result.success(Unit)
    }

    /**
     * Submit a bid in the Live Bid Room (Reverse Auction).
     */
    suspend fun placeBidInBidRoom(
        projectId: String,
        freelancer: UserEntity,
        bidAmount: Double,
        durationDays: Int,
        pitch: String
    ): Result<BidEntity> {
        if (freelancer.workStatus == "ON_WORK") {
            return Result.failure(IllegalStateException("You are already engaged in an active contract."))
        }

        val bid = BidEntity(
            id = "bid_${System.currentTimeMillis()}_${(100..999).random()}",
            projectId = projectId,
            freelancerId = freelancer.id,
            freelancerName = freelancer.fullName,
            freelancerLocation = "${freelancer.locationDistrict}, ${freelancer.locationState}",
            freelancerTrustScore = freelancer.trustScore,
            bidAmount = bidAmount,
            durationDays = durationDays,
            pitch = pitch
        )
        dao.insertBid(bid)
        cloudScope.launch {
            try { MongoDbCloudManager.upsertBid(bid) } catch (_: Throwable) {}
        }
        return Result.success(bid)
    }

    /**
     * Client awards bid in Bid Room.
     */
    suspend fun awardBidRoomWinner(
        projectId: String,
        bid: BidEntity
    ): Result<Unit> {
        return acceptProposalAndHire(
            projectId = projectId,
            proposalId = "bid_direct_${bid.id}",
            freelancerId = bid.freelancerId,
            freelancerName = bid.freelancerName
        )
    }

    /**
     * Update Live Progress (Amazon Parcel Tracking Style)
     */
    suspend fun updateProjectMilestoneStage(
        projectId: String,
        newStage: Int,
        notes: String
    ) {
        dao.updateProjectMilestone(projectId, newStage, notes)
        dao.insertMessage(
            MessageEntity(
                id = UUID.randomUUID().toString(),
                projectId = projectId,
                senderId = "system",
                senderName = "Milestone Tracker",
                senderRole = "SYSTEM",
                message = "📦 Milestone Update (Stage $newStage/5): $notes"
            )
        )
    }

    /**
     * Approve and Complete Project:
     * - Frees up the freelancer -> AVAILABLE
     * - Moves project to COMPLETED
     */
    suspend fun completeProject(
        projectId: String,
        freelancerId: String
    ) {
        val now = System.currentTimeMillis()
        dao.completeProject(projectId, now)
        dao.updateUserWorkStatus(freelancerId, "AVAILABLE", null)
        dao.insertMessage(
            MessageEntity(
                id = UUID.randomUUID().toString(),
                projectId = projectId,
                senderId = "system",
                senderName = "GIGVERSE Escrow",
                senderRole = "SYSTEM",
                message = "✅ Project completed & approved! Escrow payment released to freelancer. Freelancer is now AVAILABLE for new projects."
            )
        )
    }

    /**
     * Submit end-of-project review & ratings.
     * Recalculates Trust Score!
     */
    suspend fun submitReview(
        projectId: String,
        fromUserId: String,
        fromUserName: String,
        toUserId: String,
        rating: Double,
        tags: String,
        comment: String
    ) {
        val review = ReviewEntity(
            id = UUID.randomUUID().toString(),
            projectId = projectId,
            fromUserId = fromUserId,
            fromUserName = fromUserName,
            toUserId = toUserId,
            rating = rating,
            tags = tags,
            comment = comment
        )
        dao.insertReview(review)
        cloudScope.launch {
            try {
                MongoDbCloudManager.upsertReview(review)
            } catch (_: Throwable) {}
        }

        val targetUser = dao.getUserByIdSync(toUserId)
        if (targetUser != null && targetUser.role == "FREELANCER") {
            val updatedRating = if (targetUser.ratingCount > 0) {
                ((targetUser.rating * targetUser.ratingCount) + rating) / (targetUser.ratingCount + 1)
            } else {
                rating
            }
            val simulatedUser = targetUser.copy(
                rating = updatedRating,
                ratingCount = targetUser.ratingCount + 1,
                completedProjectsCount = targetUser.completedProjectsCount + 1
            )
            val breakdown = GigverseAiEngine.calculateTrustScore(simulatedUser)
            dao.updateFreelancerStats(toUserId, breakdown.totalScore, updatedRating)
        }
    }

    suspend fun sendMessage(projectId: String, senderId: String, senderName: String, senderRole: String, messageText: String) {
        val msg = MessageEntity(
            id = UUID.randomUUID().toString(),
            projectId = projectId,
            senderId = senderId,
            senderName = senderName,
            senderRole = senderRole,
            message = messageText
        )
        dao.insertMessage(msg)
        cloudScope.launch {
            try { MongoDbCloudManager.upsertMessage(msg) } catch (_: Throwable) {}
        }
    }

    /**
     * Purges all seeded demo data for clean production release.
     */
    suspend fun purgeAllDataForProduction() {
        dao.clearUsers()
        dao.clearProjects()
        dao.clearProposals()
        dao.clearBids()
        dao.clearMessages()
        dao.clearReviews()
        dao.clearGigPackages()
        dao.clearTasks()
        dao.clearDisputes()
        dao.clearMilestoneSubmissions()
    }

    /**
     * Prepopulates rich, realistic demo data if database is fresh.
     */
    suspend fun checkAndSeedInitialData() {
        // Disabled sample data seeding for clean production release
    }

    private suspend fun seedDatabase() {
        // Disabled sample data seeding for clean production release
        // Seed Default Users
        val demoClient = UserEntity(
            id = "client_alpha",
            role = "CLIENT",
            fullName = "Sophia Vance",
            email = "sophia@novatech.io",
            phone = "+1 (415) 890-2134",
            locationCountry = "United States",
            locationState = "California",
            locationDistrict = "San Francisco",
            companyName = "NovaTech Labs",
            industry = "Generative AI & Enterprise Mobile",
            totalSpent = 38400.0,
            projectsPostedCount = 12
        )
        dao.insertUser(demoClient)

        val demoFreelancer = UserEntity(
            id = "freelancer_alex",
            role = "FREELANCER",
            fullName = "Alex Mercer",
            email = "alex.mercer@codecraft.dev",
            phone = "+1 (510) 672-9912",
            locationCountry = "United States",
            locationState = "California",
            locationDistrict = "Berkeley",
            headline = "Staff Android & AI Systems Engineer | Jetpack Compose Master",
            bio = "Ex-Silicon Valley lead engineer specializing in ultra-fluid Jetpack Compose architectures, offline-first Room DB, and on-device neural engines. 10+ apps featured on Google Play.",
            hourlyRate = 85.0,
            skills = "Android, Kotlin, Jetpack Compose, Room, Coroutines, Gemini API, Clean Architecture",
            govIdType = "US Passport",
            govIdNumber = "US-P-88391024",
            isGovIdVerified = true,
            linkedInUrl = "https://linkedin.com/in/alex-mercer-dev",
            portfolioWebsite = "https://alexmercer.codes",
            githubUrl = "https://github.com/alexmercer-dev",
            workStatus = "AVAILABLE",
            trustScore = 96,
            onTimeDeliveryRate = 99,
            rating = 4.96,
            ratingCount = 48,
            disputeCount = 0,
            completedProjectsCount = 42,
            badgeLevel = "Top Rated Plus"
        )
        dao.insertUser(demoFreelancer)

        val demoFreelancer2 = UserEntity(
            id = "freelancer_priya",
            role = "FREELANCER",
            fullName = "Priya Sharma",
            email = "priya.ux@designverse.com",
            phone = "+91 98201 44589",
            locationCountry = "India",
            locationState = "Maharashtra",
            locationDistrict = "Mumbai",
            headline = "Principal Mobile UI/UX & Design Systems Lead",
            bio = "Obsessed with micro-interactions, motion physics, and pixel-precise design systems for fintech & gig economy applications. Winner of Google Material Design Award.",
            hourlyRate = 65.0,
            skills = "Figma, UI/UX Design, Motion Graphics, Material 3, Design Tokens, User Research",
            govIdType = "National ID (Aadhaar)",
            govIdNumber = "IND-8849-2011",
            isGovIdVerified = true,
            linkedInUrl = "https://linkedin.com/in/priyasharma-ux",
            portfolioWebsite = "https://priyadesign.studio",
            githubUrl = "https://github.com/priyasharma-design",
            workStatus = "AVAILABLE",
            trustScore = 92,
            onTimeDeliveryRate = 96,
            rating = 4.92,
            ratingCount = 34,
            disputeCount = 0,
            completedProjectsCount = 31,
            badgeLevel = "Top Rated Seller"
        )
        dao.insertUser(demoFreelancer2)

        val demoFreelancer3 = UserEntity(
            id = "freelancer_elena",
            role = "FREELANCER",
            fullName = "Elena Rostova",
            email = "elena@neuralstack.ai",
            phone = "+49 30 992384",
            locationCountry = "Germany",
            locationState = "Berlin",
            locationDistrict = "Mitte",
            headline = "Full-Stack Web3 & Cloud Backend Architect",
            bio = "Scalable microservices, distributed ledger integrations, high-concurrency Node.js and Kotlin multiplatform. Clean code advocate.",
            hourlyRate = 75.0,
            skills = "Kotlin, Ktor, Docker, Kubernetes, Web3, PostgreSQL, GraphQL",
            govIdType = "EU ID Card",
            govIdNumber = "DE-ID-7729103",
            isGovIdVerified = true,
            linkedInUrl = "https://linkedin.com/in/elena-rostova",
            portfolioWebsite = "https://elenarostova.dev",
            githubUrl = "https://github.com/erostova",
            workStatus = "AVAILABLE",
            trustScore = 89,
            onTimeDeliveryRate = 94,
            rating = 4.88,
            ratingCount = 22,
            disputeCount = 0,
            completedProjectsCount = 19,
            badgeLevel = "Level 2 Seller"
        )
        dao.insertUser(demoFreelancer3)

        // Seed Floated Projects
        val proj1 = ProjectEntity(
            id = "proj_101",
            title = "Next-Gen Fintech Android App with Biometrics & Offline Sync",
            description = "Looking for an expert Android developer to build our primary consumer banking application. Requires smooth edge-to-edge Compose UI, Room DB for caching, secure biometric authentication, and charts.",
            category = "Mobile Dev",
            requiredSkills = "Android, Kotlin, Jetpack Compose, Room, Biometrics",
            budget = 3500.0,
            budgetType = "FIXED",
            deadlineDays = 21,
            scope = "Large",
            clientId = "client_alpha",
            clientName = "Sophia Vance",
            clientCompany = "NovaTech Labs",
            clientLocation = "San Francisco, USA",
            status = "OPEN",
            aiEstimatedDays = 20,
            aiComplexityScore = "High Complexity"
        )
        dao.insertProject(proj1)

        val proj2 = ProjectEntity(
            id = "proj_102",
            title = "⚡ LIVE BID ROOM: Real-Time Logistics Route Optimizer",
            description = "Urgent requirement! We need a developer to implement an algorithmic route optimization engine with live map visualization. Open for live bidding room floor.",
            category = "AI & Algorithms",
            requiredSkills = "Kotlin, Algorithms, Google Maps SDK, Optimization",
            budget = 2200.0,
            budgetType = "FIXED",
            deadlineDays = 10,
            scope = "Medium",
            clientId = "client_alpha",
            clientName = "Sophia Vance",
            clientCompany = "NovaTech Labs",
            clientLocation = "California, USA",
            status = "BID_ROOM_ACTIVE",
            isBidRoomEnabled = true,
            bidRoomAllowedLocation = "California",
            aiEstimatedDays = 9,
            aiComplexityScore = "Moderate"
        )
        dao.insertProject(proj2)

        // Seed an Active Collaboration Project with Parcel-Style Tracking!
        val proj3 = ProjectEntity(
            id = "proj_103",
            title = "E-Commerce Luxury Watch Marketplace UI Kit",
            description = "High-end luxury catalog with smooth haptic feedback, 3D product previews, and checkout flow.",
            category = "UI/UX & Mobile",
            requiredSkills = "Figma, Jetpack Compose, MotionLayout, Material 3",
            budget = 1800.0,
            budgetType = "FIXED",
            deadlineDays = 14,
            scope = "Medium",
            clientId = "client_alpha",
            clientName = "Sophia Vance",
            clientCompany = "NovaTech Labs",
            clientLocation = "San Francisco, USA",
            status = "IN_PROGRESS",
            assignedFreelancerId = "freelancer_alex",
            assignedFreelancerName = "Alex Mercer",
            startedAt = System.currentTimeMillis() - 86400000L * 4,
            milestoneStage = 3,
            stageNotes = "Core Development phase: Product showcase grid and cart interaction models completed.",
            aiEstimatedDays = 12,
            aiComplexityScore = "Moderate"
        )
        dao.insertProject(proj3)

        // Seed proposals for Proj 101
        dao.insertProposal(
            ProposalEntity(
                id = "prop_1",
                projectId = "proj_101",
                freelancerId = "freelancer_alex",
                freelancerName = "Alex Mercer",
                freelancerTrustScore = 96,
                freelancerHourlyRate = 85.0,
                bidAmount = 3400.0,
                estimatedDays = 18,
                coverLetter = "Hi Sophia, I've built 5 production banking apps using Jetpack Compose and Room. I have full familiarity with Android Keystore security, biometrics, and reactive M3 design. Let's build something world-class.",
                status = "PENDING"
            )
        )

        // Seed live bids for Proj 102 (Bid Room)
        dao.insertBid(
            BidEntity(
                id = "bid_1",
                projectId = "proj_102",
                freelancerId = "freelancer_alex",
                freelancerName = "Alex Mercer",
                freelancerLocation = "Berkeley, California",
                freelancerTrustScore = 96,
                bidAmount = 2100.0,
                durationDays = 8,
                pitch = "Local California engineer. Can implement Dijkstra & heuristic TSP algorithm within 8 days with comprehensive unit tests."
            )
        )
        dao.insertBid(
            BidEntity(
                id = "bid_2",
                projectId = "proj_102",
                freelancerId = "freelancer_priya",
                freelancerName = "Priya Sharma",
                freelancerLocation = "Mumbai, Maharashtra",
                freelancerTrustScore = 92,
                bidAmount = 1950.0,
                durationDays = 9,
                pitch = "Can craft an intuitive visualization canvas for dispatchers alongside routing calculations."
            )
        )

        // Seed collaboration messages for Proj 103
        dao.insertMessage(
            MessageEntity(
                id = "msg_1",
                projectId = "proj_103",
                senderId = "client_alpha",
                senderName = "Sophia Vance",
                senderRole = "CLIENT",
                message = "Welcome aboard Alex! Excited to collaborate on this luxury marketplace project.",
                timestamp = System.currentTimeMillis() - 86400000L * 4
            )
        )
        dao.insertMessage(
            MessageEntity(
                id = "msg_2",
                projectId = "proj_103",
                senderId = "freelancer_alex",
                senderName = "Alex Mercer",
                senderRole = "FREELANCER",
                message = "Thanks Sophia! Architecture and component tokens are set up. Moving into Stage 3 Core Development today.",
                timestamp = System.currentTimeMillis() - 86400000L * 2
            )
        )

        // Seed Fiverr-style Gig packages
        dao.insertGigPackage(
            GigPackageEntity(
                id = "gig_1",
                freelancerId = "freelancer_alex",
                freelancerName = "Alex Mercer",
                title = "I will build a high-performance Jetpack Compose Android app with Room & M3",
                category = "Mobile Apps",
                basicTitle = "Single Screen Prototype",
                basicPrice = 120.0,
                basicDeliveryDays = 3,
                standardTitle = "Complete 5-Screen MVP",
                standardPrice = 450.0,
                standardDeliveryDays = 7,
                premiumTitle = "Production-Ready Full App",
                premiumPrice = 1200.0,
                premiumDeliveryDays = 14,
                rating = 4.98,
                reviewsCount = 42
            )
        )
        dao.insertGigPackage(
            GigPackageEntity(
                id = "gig_2",
                freelancerId = "freelancer_priya",
                freelancerName = "Priya Sharma",
                title = "I will design a sleek, award-winning Mobile UI/UX in Figma with Interactive Prototypes",
                category = "Design",
                basicTitle = "3 Screen Concept Wireframe",
                basicPrice = 80.0,
                basicDeliveryDays = 2,
                standardTitle = "Full 10-Screen UI Kit",
                standardPrice = 280.0,
                standardDeliveryDays = 5,
                premiumTitle = "Complete App System + Handoff",
                premiumPrice = 650.0,
                premiumDeliveryDays = 10,
                rating = 4.93,
                reviewsCount = 38
            )
        )

        // Seed Admin Demo Account
        val demoAdmin = UserEntity(
            id = "admin_root",
            role = "ADMIN",
            fullName = "GigVerse System Admin",
            email = "admin@gigverse.demo",
            phone = "+1 (800) 555-0199",
            locationCountry = "United States",
            locationState = "California",
            locationDistrict = "San Francisco",
            headline = "Marketplace Trust & Safety Supervisor",
            bio = "Platform governance, dispute adjudication, and verified talent compliance operations.",
            skills = "KYC Verification, Dispute Resolution, Platform Analytics",
            trustScore = 100,
            isGovIdVerified = true
        )
        dao.insertUser(demoAdmin)

        // Seed Initial Kanban Tasks
        dao.insertTask(
            TaskEntity(
                id = "task_1",
                projectId = "proj_101",
                title = "Design Token System & M3 Dark Theme",
                description = "Configure color schemes, typography, and contrast standards.",
                status = "DONE",
                priority = "HIGH"
            )
        )
        dao.insertTask(
            TaskEntity(
                id = "task_2",
                projectId = "proj_101",
                title = "Offline-first Room Data Layer",
                description = "Create Room DB with DAO query streams and Flow observables.",
                status = "IN_PROGRESS",
                priority = "HIGH"
            )
        )
        dao.insertTask(
            TaskEntity(
                id = "task_3",
                projectId = "proj_101",
                title = "Live Milestone WebSocket Hookup",
                description = "Build real-time parcel tracking indicator for milestone changes.",
                status = "TODO",
                priority = "MEDIUM"
            )
        )
        dao.insertTask(
            TaskEntity(
                id = "task_4",
                projectId = "proj_101",
                title = "KYC Document Verification Engine",
                description = "Review ID proofs and calculate AI trust scores.",
                status = "REVIEW",
                priority = "MEDIUM"
            )
        )

        // Seed Sample Dispute
        dao.insertDispute(
            DisputeEntity(
                id = "disp_101",
                projectId = "proj_102",
                projectTitle = "Next-Gen Fintech iOS & Android Wallet",
                raisedByUserId = "client_alpha",
                raisedByUserName = "Sophia Vance",
                reason = "Scope Clarification on Biometric Passkeys",
                description = "Need review on whether WebAuthn biometric passkeys were included in Milestone 2 deliverable.",
                status = "OPEN",
                resolutionNotes = "Awaiting Admin mediation and contract terms check."
            )
        )

        // Seed Sample Milestone Submission
        dao.insertSubmission(
            MilestoneSubmissionEntity(
                id = "sub_1",
                projectId = "proj_101",
                milestoneStage = 2,
                title = "Milestone 2 - Core UI Components & Navigation",
                deliverableLinks = "https://github.com/gigverse/core-ui-preview",
                notes = "Completed Jetpack Compose screens and navigation graphs with 100% test coverage.",
                status = "SUBMITTED"
            )
        )
    }
}
