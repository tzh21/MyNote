//Repository 的作用参考
//https://developer.android.com/courses/pathways/android-basics-compose-unit-5-pathway-2

package com.example.mynote.data

import com.example.mynote.network.ErrorResponse
import com.example.mynote.network.LoginRequest
import com.example.mynote.network.LoginResponse
import com.example.mynote.network.User
import com.example.mynote.network.UserApiService
import kotlinx.serialization.json.Json
import retrofit2.Response

interface UserRepository {
    suspend fun login(username: String, password: String): Response<LoginResponse>
    suspend fun signup(username: String, password: String): String
}

class NetworkUserRepository(
    private val userApiService: UserApiService
) : UserRepository {
    override suspend fun login(username: String, password: String): Response<LoginResponse> {
        return userApiService.login(LoginRequest(username, password))
    }

    override suspend fun signup(username: String, password: String): String {
        return userApiService.signup(User(username, password)).message
    }
}