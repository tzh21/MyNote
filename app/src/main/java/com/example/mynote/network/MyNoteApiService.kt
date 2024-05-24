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

@Serializable
data class MottoRequest(
    val motto: String
)

@Serializable
data class MottoResponse(
    val motto: String
)

@Serializable
data class NicknameRequest(
    val nickname: String
)

@Serializable
data class NicknameResponse(
    val nickname: String
)

interface MyNoteApiService {
    @POST("login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>

    @POST("signup")
    suspend fun signup(@Body signupRequest: SignupRequest): Response<LoginResponse>

    @GET("list/{username}")
    suspend fun list(@Path("username") username: String): Response<FileList>

    @POST("upload/{path}")
    @Multipart
    suspend fun upload(@Path("path") path: String, @Part file: MultipartBody.Part): Response<ResponseBody>

    @GET("download/{path}")
    @Streaming
    suspend fun download(@Path("path") path: String): Response<ResponseBody>

    @GET("motto/{username}")
    suspend fun getMotto(@Path("username") username: String): Response<MottoResponse>

    @POST("motto/{username}")
    suspend fun postMotto(@Path("username") username: String, @Body motto: MottoRequest): Response<ResponseBody>

    @GET("nickname/{username}")
    suspend fun getNickname(@Path("username") username: String): Response<NicknameResponse>

    @POST("nickname/{username}")
    suspend fun postNickname(@Path("username") username: String, @Body nickname: NicknameRequest): Response<ResponseBody>

    @POST("avatar/{username}")
    @Multipart
    suspend fun postAvatar(@Path("username") username: String, @Part avatar: MultipartBody.Part): Response<ResponseBody>

    @GET("avatar/{username}")
    @Streaming
    suspend fun getAvatar(@Path("username") username: String): Response<ResponseBody>
}