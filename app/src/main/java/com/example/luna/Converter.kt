package com.example.luna

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream

object Converter {
    fun getBytes(bitmap: Bitmap):ByteArray{
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG,0, stream)
        return stream.toByteArray()
    }
    fun getImage(image:ByteArray?):Bitmap{
        return BitmapFactory.decodeByteArray(image,0,image!!.size)
    }
}