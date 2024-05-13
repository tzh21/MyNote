//AppContainer 作用参考
//https://developer.android.com/courses/pathways/android-basics-compose-unit-5-pathway-2

package com.example.mynote.data

import android.content.Context
import com.example.mynote.network.UserApiService
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit

interface AppContainer {
    val userRepository: UserRepository
    val noteDao: NoteDao
}

class AppDataContainer(context: Context): AppContainer {
    private val BASE_URL = "http://8.130.86.9:8080"

    private val retrofit = Retrofit.Builder()
        .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
        .baseUrl(BASE_URL)
        .build()

    private val userApiService: UserApiService by lazy {
        retrofit.create(UserApiService::class.java)
    }

    override val userRepository: UserRepository by lazy {
        NetworkUserRepository(userApiService)
    }

    override val noteDao = MyNoteDatabase.getDatabase(context).noteDao()
}