package com.example.mynote.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE username = :username AND category = :category ORDER BY lastModifiedTime DESC")
    fun getAllNotesInCategory(username: String, category: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE username = :username ORDER BY lastModifiedTime DESC")
    fun getAllNotesFlow(username: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE username = :username ORDER BY lastModifiedTime DESC")
    fun getAllNotes(username: String): List<NoteEntity>

    @Query("SELECT * FROM notes WHERE username = :username AND category = :category AND fileName = :fileName")
    fun getNoteByName(username: String, category: String, fileName: String): Flow<NoteEntity>

    @Query("DELETE FROM notes WHERE username = :username AND category = :category")
    suspend fun deleteAllNotesInCategory(username: String, category: String)

    @Query("DELETE FROM notes WHERE username = :username")
    suspend fun deleteAllNotes(username: String)

    @Query("DELETE FROM notes WHERE username = :username AND category = :category AND fileName = :fileName")
    suspend fun deleteNote(username: String, category: String, fileName: String)

    @Query("SELECT * FROM notes WHERE username = :username AND title LIKE '%' || :keyword || '%' ORDER BY lastModifiedTime DESC")
    fun filterNotes(username: String, keyword: String): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)

    @Update
    suspend fun update(note: NoteEntity)

    @Delete
    suspend fun delete(note: NoteEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertProfile(profile: ProfileEntity)

    @Query("SELECT * FROM profiles WHERE username = :username")
    fun getProfile(username: String): Flow<ProfileEntity?>

    @Query("SELECT COUNT(*) > 0 FROM profiles WHERE username = :username")
    suspend fun doesProfileExist(username: String): Boolean

    @Query("UPDATE profiles SET motto = :motto WHERE username = :username")
    suspend fun updateMotto(username: String, motto: String)

    @Query("UPDATE profiles SET nickname = :nickname WHERE username = :username")
    suspend fun updateNickname(username: String, nickname: String)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategory(category: CategoryEntity)

    @Query("SELECT category FROM categories WHERE username = :username")
    fun getAllCategories(username: String): Flow<List<String>>
}