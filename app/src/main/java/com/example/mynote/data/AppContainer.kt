package com.example.mynote.data

import android.content.Context
import com.example.mynote.network.MyNoteApiService
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

interface AppContainer {
    val myNoteApiService: MyNoteApiService
    val noteDao: NoteDao
}

class AppDataContainer(context: Context): AppContainer {
    private val BASE_URL = "http://8.130.86.9:8080"

    private val retrofit = Retrofit.Builder()
        .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
        .baseUrl(BASE_URL)
        .build()

    override val myNoteApiService: MyNoteApiService by lazy {
        retrofit.create(MyNoteApiService::class.java)
    }

    override val noteDao = MyNoteDatabase.getDatabase(context).noteDao()
}