package com.gigverse.app.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class UserRole {
    CLIENT,
    FREELANCER,
    ADMIN
}

enum class WorkStatus {
    AVAILABLE,
    ON_WORK
}

enum class ProjectStatus {
    OPEN,
    IN_PROGRESS,
    UNDER_REVIEW,
    COMPLETED,
    BID_ROOM_ACTIVE
}

@Entity(
    tableName = "users",
    indices = [Index(value = ["email"], unique = true)]
)
data class UserEntity(
    @PrimaryKey val id: String,
    val role: String, // CLIENT or FREELANCER
    val fullName: String,
    val email: String,
    val phone: String,
    val avatarUrl: String = "",
    val locationCountry: String = "United States",
    val locationState: String = "California",
    val locationDistrict: String = "San Francisco",
    val headline: String = "",
    val bio: String = "",
    val hourlyRate: Double = 45.0,
    val skills: String = "Kotlin, Android, Jetpack Compose",
    // Freelancer Verification & Socials
    val govIdType: String = "National ID / Passport",
    val govIdNumber: String = "US-ID-884920",
    val govIdDocUrl: String = "",
    val isGovIdVerified: Boolean = true,
    val linkedInUrl: String = "https://linkedin.com/in/profile",
    val portfolioWebsite: String = "https://portfolio.dev",
    val githubUrl: String = "https://github.com/developer",
    val passwordHash: String = "",
    // Work status constraint (at a time freelancer can only work with one client)
    val workStatus: String = "AVAILABLE", // AVAILABLE or ON_WORK
    val currentActiveProjectId: String? = null,
    // AI Trust Score factors
    val trustScore: Int = 94, // 0 - 100
    val onTimeDeliveryRate: Int = 98, // Percentage
    val rating: Double = 4.9,
    val ratingCount: Int = 28,
    val disputeCount: Int = 0,
    val completedProjectsCount: Int = 24,
    val badgeLevel: String = "Top Rated Plus", // Upwork/Fiverr style badge
    // Client profile fields
    val companyName: String = "",
    val industry: String = "Software & AI Tech",
    val totalSpent: Double = 12500.0,
    val projectsPostedCount: Int = 8
)

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val category: String, // Mobile, Web, AI/ML, Design, Cloud
    val requiredSkills: String,
    val budget: Double,
    val budgetType: String = "FIXED", // FIXED or HOURLY
    val deadlineDays: Int = 14,
    val scope: String = "Medium", // Small, Medium, Large
    val clientId: String,
    val clientName: String,
    val clientCompany: String = "",
    val clientLocation: String = "California, USA",
    val status: String = "OPEN", // OPEN, IN_PROGRESS, UNDER_REVIEW, COMPLETED, BID_ROOM_ACTIVE
    val assignedFreelancerId: String? = null,
    val assignedFreelancerName: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val startedAt: Long? = null,
    val completedAt: Long? = null,
    // Live Amazon Parcel-Style Progress Tracking (Milestone 1 to 5)
    val milestoneStage: Int = 1,
    val stageNotes: String = "Project created and waiting for talent onboarding.",
    // Live Bid Room details
    val isBidRoomEnabled: Boolean = false,
    val bidRoomAllowedLocation: String = "All Regions", // Filter by State, District, Country
    // AI Duration & Complexity
    val aiEstimatedDays: Int = 12,
    val aiComplexityScore: String = "Moderate",
    val aiVelocityFactor: Double = 1.15
)

@Entity(tableName = "proposals")
data class ProposalEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val freelancerId: String,
    val freelancerName: String,
    val freelancerAvatar: String = "",
    val freelancerTrustScore: Int = 92,
    val freelancerHourlyRate: Double = 50.0,
    val bidAmount: Double,
    val estimatedDays: Int,
    val coverLetter: String,
    val status: String = "PENDING", // PENDING, ACCEPTED, REJECTED
    val submittedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "bid_room_bids")
data class BidEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val freelancerId: String,
    val freelancerName: String,
    val freelancerLocation: String,
    val freelancerTrustScore: Int,
    val bidAmount: Double,
    val durationDays: Int,
    val pitch: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val senderId: String,
    val senderName: String,
    val senderRole: String, // CLIENT or FREELANCER
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "reviews")
data class ReviewEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val fromUserId: String,
    val fromUserName: String,
    val toUserId: String,
    val rating: Double,
    val tags: String, // e.g. "On-Time, Clean Architecture, Great Communication"
    val comment: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "gig_packages")
data class GigPackageEntity(
    @PrimaryKey val id: String,
    val freelancerId: String,
    val freelancerName: String,
    val title: String,
    val category: String,
    val coverImageUrl: String = "",
    val basicTitle: String = "Standard Tier",
    val basicPrice: Double = 99.0,
    val basicDeliveryDays: Int = 3,
    val standardTitle: String = "Pro Tier",
    val standardPrice: Double = 249.0,
    val standardDeliveryDays: Int = 7,
    val premiumTitle: String = "Enterprise Custom",
    val premiumPrice: Double = 599.0,
    val premiumDeliveryDays: Int = 14,
    val rating: Double = 4.9,
    val reviewsCount: Int = 36
)

// UI and AI Analysis Data Models
data class TrustScoreBreakdown(
    val totalScore: Int,
    val tierLabel: String,
    val deadlineAdherenceScore: Int,
    val clientFeedbackScore: Int,
    val credentialsScore: Int,
    val disputePenalty: Int,
    val summaryInsight: String
)

data class MilestoneTrackStep(
    val stage: Int,
    val title: String,
    val description: String,
    val isCompleted: Boolean,
    val isCurrent: Boolean,
    val timestampString: String,
    val notes: String
)

data class DurationPredictionGraphPoint(
    val milestoneIndex: Int,
    val milestoneName: String,
    val estimatedDay: Float,
    val optimisticDay: Float,
    val conservativeDay: Float
)

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val title: String,
    val description: String = "",
    val status: String = "TODO", // TODO, IN_PROGRESS, REVIEW, DONE
    val priority: String = "MEDIUM", // LOW, MEDIUM, HIGH
    val assigneeName: String = "Alex Mercer",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "disputes")
data class DisputeEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val projectTitle: String,
    val raisedByUserId: String,
    val raisedByUserName: String,
    val reason: String,
    val description: String,
    val evidenceUrl: String = "",
    val status: String = "OPEN", // OPEN, UNDER_REVIEW, RESOLVED
    val resolutionNotes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "milestone_submissions")
data class MilestoneSubmissionEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val milestoneStage: Int,
    val title: String,
    val deliverableLinks: String,
    val notes: String,
    val status: String = "SUBMITTED", // SUBMITTED, APPROVED, REVISION_REQUESTED
    val revisionComments: String = "",
    val submittedAt: Long = System.currentTimeMillis(),
    val reviewedAt: Long? = null
)
