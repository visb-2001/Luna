package com.example.luna.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.luna.classes.Notes
import com.example.luna.classes.Todo

@Dao
interface TodoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveTodo(todo: Todo)

    @Delete
    fun deleteTodo(todo: Todo)

    @Query("select * from Todo ORDER BY position ASC")
    fun readTodo() : LiveData<List<Todo>>

    @Query("select count(*) from Todo WHERE isChecked = 0 ")
    fun todoNumber() : Int

    @Update
    fun updateTodo(todo: Todo)
}