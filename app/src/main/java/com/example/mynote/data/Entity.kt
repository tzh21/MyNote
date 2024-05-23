package com.example.mynote.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notes",
    indices = [Index(value = ["username", "fileName"], unique = true)]
)
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val username: String,
    val fileName: String,
    val category: String,
    val title: String,
    val keyword: String, // 正文的第一段
    val coverImage: String, // 封面图片
    val lastModifiedTime: String
)

@Entity(
    tableName = "profiles",
    indices = [Index(value = ["username"], unique = true)]
)
data class ProfileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val username: String = "",
    val nickname: String = "",
    val motto: String = "",
    val avatar: String = ""
)

@Entity(
    tableName = "categories",
    indices = [Index(value = ["username", "category"], unique = true)]
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val username: String = "",
    val category: String = "",
    val lastUsedTime: String = ""
)