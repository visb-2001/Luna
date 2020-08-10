package com.example.luna.classes

class User(uid: String,username: String) {
    var uid = uid
    var username = username
    var notes : MutableList<Notes>? = null
    var todo : MutableList<Todo>? = null
    var sketch: Sketch? = null

    constructor(): this("","")
}