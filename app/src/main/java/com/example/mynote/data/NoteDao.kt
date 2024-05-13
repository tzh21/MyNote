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
    @Query("SELECT * FROM notes ORDER BY lastModifiedTime DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE username = :username AND category = :category AND fileName = :fileName")
    fun getNoteByName(username: String, category: String, fileName: String): Flow<NoteEntity>

    @Query("DELETE FROM notes WHERE username = :username AND category = :category")
    suspend fun deleteAllNotes(username: String, category: String)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(note: NoteEntity)

    @Update
    suspend fun update(note: NoteEntity)

    @Delete
    suspend fun delete(note: NoteEntity)
}