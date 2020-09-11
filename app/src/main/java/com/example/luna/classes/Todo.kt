package com.example.luna.classes

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class Todo (todoText: String,isChecked: Boolean,position: Int){
    @PrimaryKey(autoGenerate = true)
    var id = 0

    var todoText: String = todoText
    var isChecked: Boolean = isChecked
    var position: Int = position
    var persist: Boolean = false

    constructor(): this("",false,0)
}