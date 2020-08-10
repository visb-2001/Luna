package com.example.luna.classes

import android.graphics.Bitmap
import android.widget.ImageView

class Chats (textMessage: String, isMe: Boolean){
    var textMessage: String = textMessage
    var isMe: Boolean = isMe
    //wiki&weather
    var isResult = false
    var title = ""
    var image: Bitmap? = null
    var extract = ""
    //timer
    var isTimer = false
    var timerStarted = false
    var time = 0
    var spoken = false
}