package com.gigverse.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
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
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    // USERS
    @Query("SELECT * FROM users ORDER BY trustScore DESC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users")
    suspend fun getAllUsersSync(): List<UserEntity>

    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserById(userId: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserByIdSync(userId: String): UserEntity?

    @Query("SELECT * FROM users WHERE LOWER(TRIM(email)) = LOWER(TRIM(:email)) LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT COUNT(*) FROM users WHERE LOWER(TRIM(email)) = LOWER(TRIM(:email))")
    suspend fun countUsersByEmail(email: String): Int

    @Query("SELECT * FROM users WHERE role = 'FREELANCER'")
    fun getAllFreelancers(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUserById(userId: String)

    // Update freelancer work status
    @Query("UPDATE users SET workStatus = :workStatus, currentActiveProjectId = :projectId WHERE id = :userId")
    suspend fun updateUserWorkStatus(userId: String, workStatus: String, projectId: String?)

    // Update freelancer trust score and ratings
    @Query("UPDATE users SET trustScore = :score, rating = :rating, ratingCount = ratingCount + 1, completedProjectsCount = completedProjectsCount + 1 WHERE id = :userId")
    suspend fun updateFreelancerStats(userId: String, score: Int, rating: Double)

    // PROJECTS
    @Query("SELECT * FROM projects ORDER BY createdAt DESC")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects")
    suspend fun getAllProjectsSync(): List<ProjectEntity>

    @Query("SELECT * FROM projects WHERE status = 'OPEN' OR status = 'BID_ROOM_ACTIVE' ORDER BY createdAt DESC")
    fun getFloatedProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :projectId")
    fun getProjectById(projectId: String): Flow<ProjectEntity?>

    @Query("SELECT * FROM projects WHERE id = :projectId")
    suspend fun getProjectByIdSync(projectId: String): ProjectEntity?

    @Query("SELECT * FROM projects WHERE clientId = :clientId ORDER BY createdAt DESC")
    fun getProjectsByClient(clientId: String): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE assignedFreelancerId = :freelancerId ORDER BY createdAt DESC")
    fun getProjectsByFreelancer(freelancerId: String): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE (clientId = :userId OR assignedFreelancerId = :userId) ORDER BY createdAt DESC")
    fun getMyProjects(userId: String): Flow<List<ProjectEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity)

    @Update
    suspend fun updateProject(project: ProjectEntity)

    // Update milestone stage and notes
    @Query("UPDATE projects SET milestoneStage = :stage, stageNotes = :notes WHERE id = :projectId")
    suspend fun updateProjectMilestone(projectId: String, stage: Int, notes: String)

    // Complete project
    @Query("UPDATE projects SET status = 'COMPLETED', completedAt = :timestamp, milestoneStage = 5 WHERE id = :projectId")
    suspend fun completeProject(projectId: String, timestamp: Long)

    // PROPOSALS
    @Query("SELECT * FROM proposals WHERE projectId = :projectId ORDER BY submittedAt DESC")
    fun getProposalsForProject(projectId: String): Flow<List<ProposalEntity>>

    @Query("SELECT * FROM proposals WHERE freelancerId = :freelancerId ORDER BY submittedAt DESC")
    fun getProposalsByFreelancer(freelancerId: String): Flow<List<ProposalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProposal(proposal: ProposalEntity)

    @Query("UPDATE proposals SET status = :status WHERE id = :proposalId")
    suspend fun updateProposalStatus(proposalId: String, status: String)

    // LIVE BID ROOM BIDS
    @Query("SELECT * FROM bid_room_bids WHERE projectId = :projectId ORDER BY bidAmount ASC, timestamp DESC")
    fun getBidsForProject(projectId: String): Flow<List<BidEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBid(bid: BidEntity)

    // MESSAGES / COLLABORATION CHAT
    @Query("SELECT * FROM messages WHERE projectId = :projectId ORDER BY timestamp ASC")
    fun getMessagesForProject(projectId: String): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    // REVIEWS
    @Query("SELECT * FROM reviews WHERE toUserId = :userId ORDER BY timestamp DESC")
    fun getReviewsForUser(userId: String): Flow<List<ReviewEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: ReviewEntity)

    // GIG PACKAGES
    @Query("SELECT * FROM gig_packages ORDER BY rating DESC")
    fun getAllGigPackages(): Flow<List<GigPackageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGigPackage(pkg: GigPackageEntity)

    // KANBAN TASKS
    @Query("SELECT * FROM tasks WHERE projectId = :projectId ORDER BY createdAt ASC")
    fun getTasksForProject(projectId: String): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Query("UPDATE tasks SET status = :status WHERE id = :taskId")
    suspend fun updateTaskStatus(taskId: String, status: String)

    // DISPUTES
    @Query("SELECT * FROM disputes ORDER BY createdAt DESC")
    fun getAllDisputes(): Flow<List<DisputeEntity>>

    @Query("SELECT * FROM disputes WHERE projectId = :projectId ORDER BY createdAt DESC")
    fun getDisputesForProject(projectId: String): Flow<List<DisputeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDispute(dispute: DisputeEntity)

    @Query("UPDATE disputes SET status = :status, resolutionNotes = :resolution WHERE id = :disputeId")
    suspend fun updateDisputeStatus(disputeId: String, status: String, resolution: String)

    // MILESTONE SUBMISSIONS
    @Query("SELECT * FROM milestone_submissions WHERE projectId = :projectId ORDER BY submittedAt DESC")
    fun getSubmissionsForProject(projectId: String): Flow<List<MilestoneSubmissionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubmission(submission: MilestoneSubmissionEntity)

    @Query("UPDATE milestone_submissions SET status = :status, revisionComments = :comments, reviewedAt = :reviewedAt WHERE id = :submissionId")
    suspend fun updateSubmissionStatus(submissionId: String, status: String, comments: String, reviewedAt: Long = System.currentTimeMillis())

    // DATABASE PURGE / RESET
    @Query("DELETE FROM users")
    suspend fun clearUsers()

    @Query("DELETE FROM projects")
    suspend fun clearProjects()

    @Query("DELETE FROM proposals")
    suspend fun clearProposals()

    @Query("DELETE FROM bid_room_bids")
    suspend fun clearBids()

    @Query("DELETE FROM messages")
    suspend fun clearMessages()

    @Query("DELETE FROM reviews")
    suspend fun clearReviews()

    @Query("DELETE FROM gig_packages")
    suspend fun clearGigPackages()

    @Query("DELETE FROM tasks")
    suspend fun clearTasks()

    @Query("DELETE FROM disputes")
    suspend fun clearDisputes()

    @Query("DELETE FROM milestone_submissions")
    suspend fun clearMilestoneSubmissions()
}
