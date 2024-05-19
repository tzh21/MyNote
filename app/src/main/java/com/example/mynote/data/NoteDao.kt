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
    fun getAllNotes(username: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE username = :username AND category = :category AND fileName = :fileName")
    fun getNoteByName(username: String, category: String, fileName: String): Flow<NoteEntity>

    @Query("DELETE FROM notes WHERE username = :username AND category = :category")
    suspend fun deleteAllNotes(username: String, category: String)

    @Query("DELETE FROM notes WHERE username = :username AND category = :category AND fileName = :fileName")
    suspend fun deleteNote(username: String, category: String, fileName: String)

//    @Query("SELECT * FROM notes WHERE title LIKE '%' || :keyword || '%' OR category LIKE '%' || :keyword || '%'")
    @Query("SELECT * FROM notes WHERE username = :username AND title LIKE '%' || :keyword || '%' ORDER BY lastModifiedTime DESC")
    fun filterNotes(username: String, keyword: String): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: NoteEntity)

    @Update
    suspend fun update(note: NoteEntity)

    @Delete
    suspend fun delete(note: NoteEntity)
}