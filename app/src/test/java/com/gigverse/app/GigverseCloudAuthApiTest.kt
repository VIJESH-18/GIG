package com.gigverse.app

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.gigverse.app.data.api.ApiClient
import com.gigverse.app.data.api.GigverseAuthApi
import com.gigverse.app.data.api.model.ApiUser
import com.gigverse.app.data.api.model.AuthResponse
import com.gigverse.app.data.api.model.LoginRequest
import com.gigverse.app.data.api.model.RegisterRequest
import com.gigverse.app.data.local.AppDatabase
import com.gigverse.app.data.local.SessionManager
import com.gigverse.app.data.model.UserRole
import com.gigverse.app.data.repository.GigverseRepository
import com.gigverse.app.ui.viewmodel.GigverseViewModel
import com.gigverse.app.ui.viewmodel.ScreenNav
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import retrofit2.Response

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class GigverseCloudAuthApiTest {

    private lateinit var app: Application
    private lateinit var sessionManager: SessionManager

    @Before
    fun setUp() {
        app = ApplicationProvider.getApplicationContext()
        sessionManager = SessionManager(app)
        sessionManager.clearSession(preserveRememberMePreference = false)
        ApiClient.setMockAuthApi(null)
    }

    @After
    fun tearDown() {
        ApiClient.setMockAuthApi(null)
        sessionManager.clearSession(preserveRememberMePreference = false)
    }

    @Test
    fun testRegisterRequestSerialization() {
        val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
        val adapter = moshi.adapter(RegisterRequest::class.java)

        val request = RegisterRequest(
            role = "FREELANCER",
            fullName = "Cloud Talent",
            email = "talent@cloud.io",
            password = "SecurePassword123!",
            skills = "Kotlin, MongoDB, Jetpack Compose",
            hourlyRate = 65.0
        )

        val json = adapter.toJson(request)
        assertTrue("JSON must contain role", json.contains("\"role\":\"FREELANCER\""))
        assertTrue("JSON must contain email", json.contains("\"email\":\"talent@cloud.io\""))
        assertTrue("JSON must contain password", json.contains("\"password\":\"SecurePassword123!\""))
        assertTrue("JSON must contain skills", json.contains("\"skills\":\"Kotlin, MongoDB, Jetpack Compose\""))
    }

    @Test
    fun testAuthResponseDeserialization() {
        val json = """
            {
                "success": true,
                "token": "mock_jwt_token_xyz_987",
                "message": "Welcome to GIGVERSE!",
                "user": {
                    "_id": "freelancer_1700000001",
                    "role": "FREELANCER",
                    "fullName": "Atlas Talent",
                    "email": "atlas.talent@gigverse.cloud",
                    "hourlyRate": 75.0,
                    "trustScore": 98,
                    "rating": 5.0
                }
            }
        """.trimIndent()

        val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
        val adapter = moshi.adapter(AuthResponse::class.java)
        val response = adapter.fromJson(json)

        assertNotNull("Response must deserialize", response)
        assertTrue(response!!.success)
        assertEquals("mock_jwt_token_xyz_987", response.token)
        assertNotNull(response.user)
        assertEquals("freelancer_1700000001", response.user?.getResolvedId())
        assertEquals("Atlas Talent", response.user?.fullName)
        assertEquals("atlas.talent@gigverse.cloud", response.user?.email)
        assertEquals(75.0, response.user?.hourlyRate ?: 0.0, 0.01)

        val entity = response.user?.toUserEntity("pbkdf2:10000:dummy:hash")
        assertNotNull("Must convert to UserEntity", entity)
        assertEquals("freelancer_1700000001", entity?.id)
        assertEquals("FREELANCER", entity?.role)
        assertEquals(98, entity?.trustScore)
    }

    @Test
    fun testErrorParsingHelper() {
        val json = """{"success":false,"error":"An account with this email already exists. Please log in."}"""
        val parsed = ApiClient.parseError(json)
        assertEquals("An account with this email already exists. Please log in.", parsed)

        val emptyParsed = ApiClient.parseError(null)
        assertTrue(emptyParsed.contains("unexpected server error", ignoreCase = true))
    }

    @Test
    fun testCloudRegistrationAndLoginFlowWithMockApi() = runBlocking {
        val mockApi = object : GigverseAuthApi {
            override suspend fun register(request: RegisterRequest): Response<AuthResponse> {
                if (request.email == "dup@gigverse.io") {
                    val errJson = """{"success":false,"error":"An account with this email already exists. Please log in."}"""
                    return Response.error(409, errJson.toResponseBody("application/json".toMediaType()))
                }
                val mockUser = ApiUser(
                    id = "freelancer_mock_123",
                    role = request.role,
                    fullName = request.fullName,
                    email = request.email,
                    hourlyRate = request.hourlyRate
                )
                return Response.success(AuthResponse(success = true, token = "test_token", user = mockUser))
            }

            override suspend fun login(request: LoginRequest): Response<AuthResponse> {
                if (request.password != "CorrectPass123!") {
                    val errJson = """{"success":false,"error":"Incorrect password. Please try again."}"""
                    return Response.error(401, errJson.toResponseBody("application/json".toMediaType()))
                }
                val mockUser = ApiUser(
                    id = "freelancer_mock_123",
                    role = request.role ?: "FREELANCER",
                    fullName = "Logged In User",
                    email = request.email
                )
                return Response.success(AuthResponse(success = true, token = "test_token_login", user = mockUser))
            }

            override suspend fun healthCheck(): Response<Map<String, Any>> {
                return Response.success(mapOf("status" to "ok", "mongodb_connected" to true))
            }

            override suspend fun getUserByEmail(email: String): Response<AuthResponse> {
                return Response.success(AuthResponse(success = true))
            }
        }

        ApiClient.setMockAuthApi(mockApi)

        val db = AppDatabase.getDatabase(app)
        val repo = GigverseRepository(db.appDao())

        // 1. Duplicate email registration rejected
        val dupReq = RegisterRequest(
            role = "FREELANCER",
            fullName = "Duplicate Talent",
            email = "dup@gigverse.io",
            password = "Pass123!"
        )
        val dupResult = repo.registerUserWithCloud(dupReq, "Pass123!")
        assertTrue("Duplicate registration must fail", dupResult.isFailure)
        assertTrue("Error must explain duplicate account", dupResult.exceptionOrNull()?.message?.contains("already exists") == true)

        // 2. Successful Registration
        val regReq = RegisterRequest(
            role = "FREELANCER",
            fullName = "Valid Cloud Talent",
            email = "valid.cloud@gigverse.io",
            password = "CorrectPass123!"
        )
        val regResult = repo.registerUserWithCloud(regReq, "CorrectPass123!")
        assertTrue("Valid registration must succeed", regResult.isSuccess)
        val user = regResult.getOrThrow()
        assertEquals("freelancer_mock_123", user.id)
        assertEquals("valid.cloud@gigverse.io", user.email)

        // 3. User cached in Room SQLite
        val cached = db.appDao().getUserByIdSync("freelancer_mock_123")
        assertNotNull("User must be cached into local Room SQLite", cached)
        assertEquals("valid.cloud@gigverse.io", cached?.email)

        // 4. Login with incorrect password rejected
        val badLogin = repo.loginUserWithCloud(LoginRequest("valid.cloud@gigverse.io", "WrongPassword!"), "WrongPassword!")
        assertTrue("Bad password login must fail", badLogin.isFailure)
        assertTrue(badLogin.exceptionOrNull()?.message?.contains("Incorrect password") == true)

        // 5. Login with correct password succeeds
        val goodLogin = repo.loginUserWithCloud(LoginRequest("valid.cloud@gigverse.io", "CorrectPass123!"), "CorrectPass123!")
        assertTrue("Valid password login must succeed", goodLogin.isSuccess)
        assertEquals("Logged In User", goodLogin.getOrThrow().fullName)
    }
}
