package com.example.luna.api

//Wikipedia
class WikipediaApi(val title:String?, val extract:String?, val thumbnail: Thumbnail?)

class Thumbnail(val source: String?)

//Jokes
class JokeApi(val type:String?, val setup:String?, val delivery:String?, val joke:String?)

//Weather
class LocationApi(val woeid:Int?)

class WeatherApi(val consolidated_weather:List<WeatherAttributes>)

class WeatherAttributes(val weather_state_name:String?, val weather_state_abbr:String?, val the_temp:Float?, val humidity:Float?)