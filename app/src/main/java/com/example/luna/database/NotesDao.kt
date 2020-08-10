package com.example.luna.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.luna.classes.Notes

@Dao
interface NotesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveNote(note: Notes)

    @Delete
    fun deleteNote(note: Notes)

    @Query("select * from Notes")
    fun readNotes() : LiveData<List<Notes>>

}