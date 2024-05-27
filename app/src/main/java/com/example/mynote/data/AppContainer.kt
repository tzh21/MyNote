package com.example.mynote.data

import android.content.Context
import com.example.mynote.network.LLMApiService
import com.example.mynote.network.MyNoteApiService
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit

interface AppContainer {
    val myNoteApiService: MyNoteApiService
    val llmApiService: LLMApiService
    val noteDao: NoteDao
}

class AppDataContainer(context: Context): AppContainer {
    private val SERVER_BASE = "http://8.130.86.9:8080"

    private val json = Json { ignoreUnknownKeys = true }

    private val serverRetrofit = Retrofit.Builder()
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .baseUrl(SERVER_BASE)
        .build()

    override val myNoteApiService: MyNoteApiService by lazy {
        serverRetrofit.create(MyNoteApiService::class.java)
    }

    private val LLM_BASE = "https://api.moonshot.cn"

    private val llmRetrofit = Retrofit.Builder()
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .baseUrl(LLM_BASE)
        .build()

    override val llmApiService: LLMApiService by lazy {
        llmRetrofit.create(LLMApiService::class.java)
    }

    override val noteDao = MyNoteDatabase.getDatabase(context).noteDao()
}