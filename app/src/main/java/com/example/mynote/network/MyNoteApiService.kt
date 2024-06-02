package com.example.mynote.network

import kotlinx.serialization.Serializable
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface MyNoteApiService {
    @POST("login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>

    @POST("signup")
    suspend fun signup(@Body signupRequest: SignupRequest): Response<LoginResponse>

    @GET("list/{username}")
    suspend fun list(@Path("username") username: String): Response<FileList>

    @GET("blocks/{username}/{fileName}")
    suspend fun getBlocks(@Path("username") username: String, @Path("fileName") fileName: String): Response<ResponseBody>

    @GET("image/{username}/{fileName}")
    suspend fun getImage(@Path("username") username: String, @Path("fileName") fileName: String): Response<ResponseBody>

    @GET("audio/{username}/{fileName}")
    suspend fun getAudio(@Path("username") username: String, @Path("fileName") fileName: String): Response<ResponseBody>

    @POST("blocks/{username}/{fileName}")
    suspend fun uploadBlocks(@Path("username") username: String, @Path("fileName") fileName: String, @Body blocks: RequestBody): Response<ResponseBody>

    @POST("image/{username}/{fileName}")
    suspend fun uploadImage(@Path("username") username: String, @Path("fileName") fileName: String, @Body image: RequestBody): Response<ResponseBody>

    @POST("audio/{username}/{fileName}")
    suspend fun uploadAudio(@Path("username") username: String, @Path("fileName") fileName: String, @Body audio: RequestBody): Response<ResponseBody>

    @DELETE("note/{username}/{fileName}")
    suspend fun deleteNote(@Path("username") username: String, @Path("fileName") fileName: String): Response<ResponseBody>

    @POST("motto/{username}")
    suspend fun postMotto(@Path("username") username: String, @Body motto: MottoRequest): Response<ResponseBody>

    @POST("nickname/{username}")
    suspend fun postNickname(@Path("username") username: String, @Body nickname: NicknameRequest): Response<ResponseBody>

    @POST("avatar-name/{username}")
    suspend fun postAvatarName(@Path("username") username: String, @Body fileName: AvatarNameRequest): Response<ResponseBody>

    @POST("avatar/{username}/{fileName}")
    suspend fun postAvatar(@Path("username") username: String, @Path("fileName") fileName: String,  @Body avatar: RequestBody): Response<ResponseBody>

    @GET("avatar/{username}/{fileName}")
    suspend fun getAvatar(@Path("username") username: String, @Path("fileName") fileName: String): Response<ResponseBody>

    @GET("profile/{username}")
    suspend fun getProfile(@Path("username") username: String): Response<ProfileResponse>

    @POST("password/{username}")
    suspend fun changePassword(@Path("username") username: String, @Body password: ChangePasswordRequest): Response<ResponseBody>
}

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
data class NicknameRequest(
    val nickname: String
)
@Serializable
data class AvatarNameRequest(
    val avatar: String
)

@Serializable
data class ProfileResponse(
    val username: String,
    val motto: String,
    val nickname: String,
    val avatar: String
)

@Serializable
data class ChangePasswordRequest(
    val password: String
)