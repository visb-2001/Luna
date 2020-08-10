package com.example.luna.classes

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class Sketch (){
    @PrimaryKey(autoGenerate = false)
    var id = 0

    var sketchUrl: String = ""
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    var sketchBitmap: ByteArray? = null


}