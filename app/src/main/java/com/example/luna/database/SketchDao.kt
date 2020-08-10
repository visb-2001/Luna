package com.example.luna.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.luna.classes.Sketch
import com.example.luna.classes.Todo

@Dao
interface SketchDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveSketch(sketch: Sketch)

    @Delete
    fun deleteSketch(sketch: Sketch)

    @Query("select * from Sketch")
    fun readSketch() : List<Sketch>

    @Query("select count(*) from Sketch")
    fun sketchExist() : Int

    @Update
    fun sketchUpdate(sketch: Sketch)
}