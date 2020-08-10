package com.example.luna.classes

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class Notes (noteText: String, noteTitle: String){
    @PrimaryKey(autoGenerate = true)
    var id = 0

    var noteTitle: String = noteTitle
    var noteText: String = noteText

    constructor(): this("","")
}