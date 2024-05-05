//Repository 的作用参考
//https://developer.android.com/courses/pathways/android-basics-compose-unit-5-pathway-2

package com.example.mynote.data

import com.example.mynote.network.LoginRequest
import com.example.mynote.network.User
import com.example.mynote.network.UserApiService

interface UserRepository {
    suspend fun login(username: String, password: String): String
    suspend fun signup(username: String, password: String): String
}

class NetworkUserRepository(
    private val userApiService: UserApiService
) : UserRepository {
    override suspend fun login(username: String, password: String): String {
        return userApiService.login(LoginRequest(username, password)).message
    }

    override suspend fun signup(username: String, password: String): String {
        return userApiService.signup(User(username, password)).message
    }
}