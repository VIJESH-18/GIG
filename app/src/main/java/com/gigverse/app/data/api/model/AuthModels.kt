package com.gigverse.app.data.api.model

import com.gigverse.app.data.model.UserEntity
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RegisterRequest(
    @Json(name = "role") val role: String,
    @Json(name = "fullName") val fullName: String,
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String,
    @Json(name = "phone") val phone: String = "",
    @Json(name = "avatarUrl") val avatarUrl: String = "",
    @Json(name = "locationCountry") val locationCountry: String = "United States",
    @Json(name = "locationState") val locationState: String = "California",
    @Json(name = "locationDistrict") val locationDistrict: String = "San Francisco",
    @Json(name = "headline") val headline: String = "",
    @Json(name = "bio") val bio: String = "",
    @Json(name = "hourlyRate") val hourlyRate: Double = 45.0,
    @Json(name = "skills") val skills: String = "Kotlin, Android, Jetpack Compose",
    @Json(name = "govIdType") val govIdType: String = "National ID / Passport",
    @Json(name = "govIdNumber") val govIdNumber: String = "",
    @Json(name = "govIdDocUrl") val govIdDocUrl: String = "",
    @Json(name = "isGovIdVerified") val isGovIdVerified: Boolean = true,
    @Json(name = "linkedInUrl") val linkedInUrl: String = "",
    @Json(name = "portfolioWebsite") val portfolioWebsite: String = "",
    @Json(name = "githubUrl") val githubUrl: String = "",
    @Json(name = "companyName") val companyName: String = "",
    @Json(name = "industry") val industry: String = "Software & AI Tech"
)

@JsonClass(generateAdapter = true)
data class LoginRequest(
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String,
    @Json(name = "role") val role: String? = null
)

@JsonClass(generateAdapter = true)
data class ApiUser(
    @Json(name = "id") val id: String? = null,
    @Json(name = "_id") val mongoId: String? = null,
    @Json(name = "role") val role: String = "FREELANCER",
    @Json(name = "fullName") val fullName: String = "",
    @Json(name = "email") val email: String = "",
    @Json(name = "phone") val phone: String = "",
    @Json(name = "avatarUrl") val avatarUrl: String = "",
    @Json(name = "locationCountry") val locationCountry: String = "United States",
    @Json(name = "locationState") val locationState: String = "California",
    @Json(name = "locationDistrict") val locationDistrict: String = "San Francisco",
    @Json(name = "headline") val headline: String = "",
    @Json(name = "bio") val bio: String = "",
    @Json(name = "hourlyRate") val hourlyRate: Double = 45.0,
    @Json(name = "skills") val skills: String = "",
    @Json(name = "govIdType") val govIdType: String = "National ID / Passport",
    @Json(name = "govIdNumber") val govIdNumber: String = "",
    @Json(name = "govIdDocUrl") val govIdDocUrl: String = "",
    @Json(name = "isGovIdVerified") val isGovIdVerified: Boolean = true,
    @Json(name = "linkedInUrl") val linkedInUrl: String = "",
    @Json(name = "portfolioWebsite") val portfolioWebsite: String = "",
    @Json(name = "githubUrl") val githubUrl: String = "",
    @Json(name = "workStatus") val workStatus: String = "AVAILABLE",
    @Json(name = "currentActiveProjectId") val currentActiveProjectId: String? = null,
    @Json(name = "trustScore") val trustScore: Int = 94,
    @Json(name = "onTimeDeliveryRate") val onTimeDeliveryRate: Int = 98,
    @Json(name = "rating") val rating: Double = 5.0,
    @Json(name = "ratingCount") val ratingCount: Int = 1,
    @Json(name = "disputeCount") val disputeCount: Int = 0,
    @Json(name = "completedProjectsCount") val completedProjectsCount: Int = 0,
    @Json(name = "badgeLevel") val badgeLevel: String = "Rising Talent",
    @Json(name = "companyName") val companyName: String = "",
    @Json(name = "industry") val industry: String = "",
    @Json(name = "totalSpent") val totalSpent: Double = 0.0,
    @Json(name = "projectsPostedCount") val projectsPostedCount: Int = 0
) {
    fun getResolvedId(): String = (id ?: mongoId ?: "").ifBlank { "user_${System.currentTimeMillis()}" }

    fun toUserEntity(passwordHashFallback: String = ""): UserEntity {
        return UserEntity(
            id = getResolvedId(),
            role = role,
            fullName = fullName,
            email = email,
            phone = phone,
            avatarUrl = avatarUrl,
            locationCountry = locationCountry,
            locationState = locationState,
            locationDistrict = locationDistrict,
            headline = headline,
            bio = bio,
            hourlyRate = hourlyRate,
            skills = skills,
            govIdType = govIdType,
            govIdNumber = govIdNumber,
            govIdDocUrl = govIdDocUrl,
            isGovIdVerified = isGovIdVerified,
            linkedInUrl = linkedInUrl,
            portfolioWebsite = portfolioWebsite,
            githubUrl = githubUrl,
            passwordHash = passwordHashFallback,
            workStatus = workStatus,
            currentActiveProjectId = currentActiveProjectId,
            trustScore = trustScore,
            onTimeDeliveryRate = onTimeDeliveryRate,
            rating = rating,
            ratingCount = ratingCount,
            disputeCount = disputeCount,
            completedProjectsCount = completedProjectsCount,
            badgeLevel = badgeLevel,
            companyName = companyName,
            industry = industry,
            totalSpent = totalSpent,
            projectsPostedCount = projectsPostedCount
        )
    }
}

@JsonClass(generateAdapter = true)
data class AuthResponse(
    @Json(name = "success") val success: Boolean = false,
    @Json(name = "token") val token: String? = null,
    @Json(name = "message") val message: String? = null,
    @Json(name = "error") val error: String? = null,
    @Json(name = "user") val user: ApiUser? = null
)

@JsonClass(generateAdapter = true)
data class ApiErrorResponse(
    @Json(name = "success") val success: Boolean? = false,
    @Json(name = "error") val error: String? = null,
    @Json(name = "message") val message: String? = null
)
