package com.example.luna

import org.tartarus.snowball.SnowballProgram

val stemClass = Class.forName("org.tartarus.snowball.ext." + "English" + "Stemmer")
val stemmer = stemClass.newInstance() as SnowballProgram

fun wordExtract(input:String, bagOfWords:MutableList<String>): String {
    var inputWords = input.split(" ")
    var term = ""
    for(i in inputWords) {
        val re = Regex("[^A-Za-z0-9 ]")
        var rWord = re.replace(i, "")
        rWord = rWord.toLowerCase()
        stemmer.current = rWord
        stemmer.stem()
        if (!bagOfWords.contains(stemmer.current)) {
            term += i
        }
    }
    return term
}