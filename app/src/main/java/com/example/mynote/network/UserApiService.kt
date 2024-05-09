//ApiService 作用参考
//https://developer.android.com/courses/pathways/android-basics-compose-unit-5-pathway-1

package com.example.mynote.network

import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)

@Serializable
data class User(
    val username: String,
    val password: String
)

@Serializable
data class LoginResponse(
    val message: String
)

@Serializable
data class ErrorResponse(
    val error: String
)

interface UserApiService {
    @POST("login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>
    @POST("signup")
    suspend fun signup(@Body user: User): LoginResponse
}
