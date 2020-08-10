package com.example.luna

data class Intents(val intents: List<Entity>)

data class Entity(val tag: String, val patterns: List<String>, val responses: List<String>)