package com.example.luna.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.luna.classes.Notes
import com.example.luna.classes.Sketch
import com.example.luna.classes.Todo


@Database(entities = [(Notes::class)], version = 1)
abstract class NotesDatabase: RoomDatabase() {
    abstract fun notesDao(): NotesDao
    companion object {

        private var INSTANCE: NotesDatabase? = null

        fun getInstance(context: Context): NotesDatabase {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(context, NotesDatabase::class.java, "NotesDB").build()
            }
            return INSTANCE!!
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }
}


@Database(entities = [(Todo::class)], version = 1)
abstract class TodoDatabase: RoomDatabase() {
    abstract fun todoDao(): TodoDao
    companion object {

        private var INSTANCE: TodoDatabase? = null

        fun getInstance(context: Context): TodoDatabase {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(context, TodoDatabase::class.java, "TodoDB").build()
            }
            return INSTANCE!!
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }
}

@Database(entities = [(Sketch::class)], version = 1)
abstract class SketchDatabase: RoomDatabase() {
    abstract fun sketchDao(): SketchDao
    companion object {

        private var INSTANCE: SketchDatabase? = null

        fun getInstance(context: Context): SketchDatabase {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(context, SketchDatabase::class.java, "SketchDB").build()
            }
            return INSTANCE!!
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }
}