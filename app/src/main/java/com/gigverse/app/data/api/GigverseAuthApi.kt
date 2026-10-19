package com.gigverse.app.data.api

import com.gigverse.app.data.api.model.AuthResponse
import com.gigverse.app.data.api.model.LoginRequest
import com.gigverse.app.data.api.model.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface GigverseAuthApi {

    @POST("api/auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<AuthResponse>

    @GET("api/health")
    suspend fun healthCheck(): Response<Map<String, Any>>

    @GET("api/users/by-email/{email}")
    suspend fun getUserByEmail(
        @Path("email") email: String
    ): Response<AuthResponse>
}
