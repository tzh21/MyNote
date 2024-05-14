//ApiService 作用参考
//https://developer.android.com/courses/pathways/android-basics-compose-unit-5-pathway-1

package com.example.mynote.network

import kotlinx.serialization.Serializable
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Streaming

@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)

@Serializable
data class SignupRequest(
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

@Serializable
data class FileList(
    val files: List<String>
)

interface MyNoteApiService {
    @POST("login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>

    @POST("signup")
    suspend fun signup(@Body signupRequest: SignupRequest): LoginResponse

    @GET("list/{username}")
    suspend fun list(@Path("username") username: String): Response<FileList>

    @Multipart
    @POST("upload/{path}")
    suspend fun upload(@Path("path") path: String, @Part file: MultipartBody.Part): Response<ResponseBody>

    @GET("download/{path}")
    @Streaming
    suspend fun download(@Path("path") path: String): Response<ResponseBody>
}
