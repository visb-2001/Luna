package com.example.luna.api

import android.location.Location
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import okhttp3.*
import java.io.IOException
import java.lang.reflect.Type


//Wikipedia
fun wikipediaApiCall(type: String, resultHandler: (WikipediaApi) -> Unit){
    println("Connecting")

    val bUrl: String = "https://en.wikipedia.org/api/rest_v1/page/summary/"

    val url = bUrl + type
    Log.d("term",url)

    val request = Request.Builder().url(url).build()

    val client = OkHttpClient()
    client.newCall(request).enqueue(object: Callback {
        override fun onFailure(call: Call, e: IOException) {
            println("Failed$e")
        }

        override fun onResponse(call: Call, response: Response) {
            val body =  response.body?.string()
            println(body)

            val gson = GsonBuilder().create()

            resultHandler(gson.fromJson(body, WikipediaApi::class.java))
        }
    })
}

//Joke
fun jokeApiCall(rajini: Boolean,resultHandler: (JokeApi) -> Unit){
    println("Connecting")

    val aUrl: String = "https://sv443.net/jokeapi/v2/joke/Any?blacklistFlags=nsfw,religious,political,racist,sexist"

    val bUrl: String = "http://www.rajinikanthapi.co.in/random"
    var url = " "

    url = if(rajini){
        bUrl
    }else{
        aUrl
    }

    val request = Request.Builder().url(url).build()

    val client = OkHttpClient()
    client.newCall(request).enqueue(object: Callback {
        override fun onFailure(call: Call, e: IOException) {
            println("Failed$e")
        }

        override fun onResponse(call: Call, response: Response) {
            val body =  response.body?.string()
            println(body)

            val gson = GsonBuilder().create()

            resultHandler(gson.fromJson(body, JokeApi::class.java))
        }
    })
}

//Weather
fun locationApiCall(location: Location,resultHandler: (LocationApi) -> Unit){
    println("Connecting")

    val bUrl: String = "https://www.metaweather.com/api/location/search/?lattlong="
    var url = bUrl+location.latitude.toString()+","+location.longitude.toString()

    val request = Request.Builder().url(url).build()

    val client = OkHttpClient()
    client.newCall(request).enqueue(object: Callback {
        override fun onFailure(call: Call, e: IOException) {
            println("Failed$e")
        }

        override fun onResponse(call: Call, response: Response) {
            val body =  response.body?.string()
            println(body)

            val gson = GsonBuilder().create()
            val locationList: List<LocationApi> = gson.fromJson(body, Array<LocationApi>::class.java).toList()
            resultHandler(locationList[0])
        }
    })
}

fun weatherApiCall(woeid: Int,resultHandler: (WeatherApi) -> Unit){
    println("Connecting")

    val bUrl: String = "https://www.metaweather.com/api/location/"
    var url = bUrl+woeid.toString()

    val request = Request.Builder().url(url).build()

    val client = OkHttpClient()
    client.newCall(request).enqueue(object: Callback {
        override fun onFailure(call: Call, e: IOException) {
            println("Failed$e")
        }

        override fun onResponse(call: Call, response: Response) {
            val body =  response.body?.string()
            println(body)

            val gson = GsonBuilder().create()

            resultHandler(gson.fromJson(body, WeatherApi::class.java))
        }
    })
}

//Yoda
fun yodaApiCall(input: String,resultHandler: (YodaApi) -> Unit){
    println("Connecting")

    val bUrl: String = "https://api.funtranslations.com/translate/yoda.json?text"
    var url = bUrl+input

    val request = Request.Builder().url(url).build()

    val client = OkHttpClient()
    client.newCall(request).enqueue(object: Callback {
        override fun onFailure(call: Call, e: IOException) {
            println("Failed$e")
        }

        override fun onResponse(call: Call, response: Response) {
            val body =  response.body?.string()
            println(body)

            val gson = GsonBuilder().create()

            resultHandler(gson.fromJson(body, YodaApi::class.java))
        }
    })
}

//Jeopardy
fun jeopardyApiCall(resultHandler: (JeopardyApi) -> Unit){
    println("Connecting")

    val bUrl: String = "https://jservice.io/api/random"
    var url = bUrl

    val request = Request.Builder().url(url).build()

    val client = OkHttpClient()
    client.newCall(request).enqueue(object: Callback {
        override fun onFailure(call: Call, e: IOException) {
            println("Failed$e")
        }

        override fun onResponse(call: Call, response: Response) {
            val body =  response.body?.string()
            println(body)

            val gson = GsonBuilder().create()
            val quizList: List<JeopardyApi> = gson.fromJson(body, Array<JeopardyApi>::class.java).toList()
            resultHandler(quizList[0])
        }
    })
}

