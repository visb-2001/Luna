package com.example.luna.fragments

import android.Manifest
import android.app.Activity
import android.app.Instrumentation
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.ContactsContract
import android.provider.Settings
import android.speech.RecognizerIntent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.PopupWindow
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.luna.*
import com.example.luna.adapters.MessageAdapter
import com.example.luna.api.*
import com.example.luna.classes.Chats
import com.example.luna.classes.Notes
import com.example.luna.classes.Todo
import com.example.luna.classes.User
import com.example.luna.database.NotesDatabase
import com.example.luna.database.SketchDatabase
import com.example.luna.database.TodoDatabase
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_chat.*
import org.tartarus.snowball.SnowballProgram
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.lang.Exception
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.security.Permission
import java.text.DateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*


class ChatFragment : Fragment() {
    //Variables
    var bagOfWords = mutableListOf<String>()
    var labels = mutableListOf<String>()
    var responseMessageList = mutableListOf<Chats>()
    var notesList = mutableListOf<Notes>()
    var nextIsNote = false
    var todoList = mutableListOf<Todo>()
    var nextIsTodo = false
    var nextIsYoda = false
    var nextIsAnswer = false
    var answer = " "
    var userInput = ""

    //Json

    val gson = Gson()
    lateinit var intents : Intents

    //Lang Processing
    val stemClass = Class.forName("org.tartarus.snowball.ext." + "English" + "Stemmer")
    val stemmer = stemClass.newInstance() as SnowballProgram

    //TensorFlow
    lateinit var interpreter : Interpreter
    var modelInput = Array(1) { FloatArray(bagOfWords.size){0f} }
    var modelOutput = Array(1) { FloatArray(labels.size) }

    //Adapter
    lateinit var messageAdapter : MessageAdapter



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }



    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        interpreter = Interpreter(loadModelFile())
        val jsonFileString = getJsonDataFromAsset(requireContext(), "intents.json")
        intents = gson.fromJson(jsonFileString, Intents::class.java)
        for (intent in intents.intents){
            labels.add(intent.tag)
            for(sentences in intent.patterns){
                val words = sentences.split(" ")
                for(word in words){
                    val re = Regex("[^A-Za-z0-9 ]")
                    var rWord = re.replace(word, "")
                    rWord = rWord.toLowerCase()
                    stemmer.current = rWord
                    stemmer.stem()
                    bagOfWords.add(stemmer.current)
                }
            }
        }
        labels.sort()
        bagOfWords = bagOfWords.sorted() as MutableList<String>
        bagOfWords = bagOfWords.distinct() as MutableList<String>
        for(i in bagOfWords){
            Log.d("bag", i)
        }
        modelInput = Array(1) { FloatArray(bagOfWords.size){0f} }
        modelOutput = Array(1) { FloatArray(labels.size) }

        messageAdapter = MessageAdapter(
            responseMessageList,
            requireContext()
        )

        //Logout
        options.setOnClickListener {
            val popupMenu = PopupMenu(context,options)
            popupMenu.menuInflater.inflate(R.menu.logout,popupMenu.menu)
            popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
                when(item.itemId) {
                    R.id.logout ->
                    {
                        FirebaseAuth.getInstance().signOut()
                        var intent = Intent(context, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                        requireActivity().startActivity(intent)
                        //Toast.makeText(context, "You Clicked : " + item.title, Toast.LENGTH_SHORT).show()
                        activity?.overridePendingTransition(R.anim.fade_in,R.anim.fade_out)
                    }

                }
                true
            })
            popupMenu.show()
        }



        //Input Handler

        chatInterface.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false
        )
        chatInterface.adapter = messageAdapter

        //Welcome Message
        val ref = FirebaseDatabase.getInstance().getReference("/users/${FirebaseAuth.getInstance().uid}")
        val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val run = preferences.getBoolean("First_Time",true)
        Log.d("call", run.toString())
        if(run){
            var welcomMsg = Chats("Hey there I'm luna your personal assistant.\nI can help you organise and get you whatever you need.\nP.S. I'm terrible at math.\nBut before we get started I'm gonna need a few permissions to help you more effectively.",false)
            ref.addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(error: DatabaseError) {
                    responseMessageList.add(welcomMsg)
                    messageAdapter.notifyDataSetChanged()
                    chatInterface.smoothScrollToPosition(messageAdapter.itemCount - 1)
                    Input.setText("")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)
                    if (user != null) {
                        welcomMsg.textMessage = "Hey ${user.username} I'm luna your personal assistant.\nI can help you organise and get you whatever you need.\nP.S. I'm terrible at math.\nBut before we get started I'm gonna need a few permissions to help you more effectively."
                        responseMessageList.add(welcomMsg)
                        messageAdapter.notifyDataSetChanged()
                        chatInterface.smoothScrollToPosition(messageAdapter.itemCount - 1)
                        Input.setText("")
                    }
                }
            })
            Log.d("call", "this shouldnt run")
            Handler().postDelayed({
                if(!(ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED
                            && ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
                            && ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            && ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)){
                    requestPermissions(arrayOf(Manifest.permission.CALL_PHONE,Manifest.permission.READ_CONTACTS,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION), 1)
                }
            },8000)
            preferences.edit().putBoolean("First_Time",false).apply()
        }else{
            if(!(ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED)){
                preferences.edit().putBoolean("Can_Call",false).apply()
            }
            if(ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                preferences.edit().putBoolean("Can_Locate",false).apply()
            }
            var welcomMsg = Chats("Hey there good to see you again!",false)
            ref.addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(error: DatabaseError) {
                    responseMessageList.add(welcomMsg)
                    messageAdapter.notifyDataSetChanged()
                    chatInterface.smoothScrollToPosition(messageAdapter.itemCount - 1)
                    Input.setText("")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)
                    if (user != null) {
                        welcomMsg.textMessage = "Hey ${user.username} good to see you again!"
                        responseMessageList.add(welcomMsg)
                        messageAdapter.notifyDataSetChanged()
                        chatInterface.smoothScrollToPosition(messageAdapter.itemCount - 1)
                        Input.setText("")
                    }
                }
            })

        }



        mic.setOnClickListener {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak")
            try {
                startActivityForResult(intent, 1)
            } catch (e: ActivityNotFoundException) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Your device does not support STT.", Toast.LENGTH_LONG).show()
            }
        }

        Input.setOnEditorActionListener { _, actionId, _ ->
            return@setOnEditorActionListener when (actionId) {
                EditorInfo.IME_ACTION_SEND -> {
                    //To check if the input is note data
                    if(Input.text.toString().isNotEmpty()){
                        userInput = Input.text.toString()
                        mainLogic()
                        //userInput = ""
                        true
                    }
                    else{
                        false
                    }
                }
                else -> false
            }
        }
        
    }
    private fun loadModelFile(): MappedByteBuffer {
        val fileDescriptor = resources.assets.openFd("model.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel: FileChannel = inputStream.channel
        val startOffset: Long = fileDescriptor.startOffset
        val declaredLength: Long = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        when (requestCode) {
            1 -> {

                if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(context, "Call Permission Granted", Toast.LENGTH_SHORT).show()
                    PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean("Can_Call",true).apply()
                    if(grantResults[2] == PackageManager.PERMISSION_GRANTED && grantResults[3] == PackageManager.PERMISSION_GRANTED){
                        Toast.makeText(context, "Location Permission Granted", Toast.LENGTH_SHORT).show()
                        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean("Can_Locate",true).apply()
                    }
                    else{
                        Toast.makeText(context, "Location Permission Denied", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Call Permission Denied", Toast.LENGTH_SHORT).show()
                    Toast.makeText(context, "Location Permission Denied", Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            1 -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    if (!result.isNullOrEmpty()) {
                        val recognizedText = result[0]
                        userInput = recognizedText
                        mainLogic()

                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun mainLogic() {
        if(nextIsNote){
            var currentDate = LocalDate.now()
            var formatter = DateTimeFormatter.ofPattern("dd-MM")
            var newNote = Notes(
                userInput,
                currentDate.format(formatter)
            )
            Thread{
                NotesDatabase.getInstance(
                    requireContext()
                ).notesDao().saveNote(newNote)
            }.start()


            val uid = FirebaseAuth.getInstance().uid
            val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
            ref.addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    var nList = snapshot.getValue(User::class.java)
                    //Log.d("list",nList!!.notes!!.size.toString())
                    if(nList?.notes != null){
                        nList.notes!!.add(newNote)
                        ref.child("notes").setValue(nList.notes)
                    }
                    else{
                        ref.child("notes").setValue(mutableListOf(newNote))
                    }
                }

            })

            notesList.add(newNote)
            val message = Chats(
                userInput,
                true
            )
            responseMessageList.add(message)
            val response =
                Chats("Done", false)
            responseMessageList.add(response)
            messageAdapter.notifyDataSetChanged()
            chatInterface.smoothScrollToPosition(messageAdapter.itemCount - 1)
            Input.setText("")
            nextIsNote = false
        }
        else if(nextIsYoda){
            val message = Chats(
                userInput,
                true
            )
            yodaApiCall(userInput){yodaApi ->
                var responseYoda = if(!yodaApi.contents?.translated.isNullOrEmpty()){
                    Chats(yodaApi.contents?.translated.toString(),false)
                }else{
                    Chats("Understand I can not",false)
                }
                requireActivity().runOnUiThread {
                    responseMessageList.add(message)
                    responseMessageList.add(responseYoda)
                    messageAdapter.notifyDataSetChanged()
                    chatInterface.smoothScrollToPosition(messageAdapter.itemCount - 1)
                    Input.setText("")
                    nextIsYoda = false
                }
            }
        }
        else if(nextIsAnswer){
            val message = Chats(
                userInput,
                true
            )

            val responseQuiz = if(answer.equals(userInput,true)){
                Chats("That is correct!\n You sure are as smart as you sound",false)
            }
            else{
                Chats("That is incorrect, looks like I've won\nThe correct answer is $answer",false)
            }
            responseMessageList.add(message)
            responseMessageList.add(responseQuiz)
            messageAdapter.notifyDataSetChanged()
            chatInterface.smoothScrollToPosition(messageAdapter.itemCount - 1)
            Input.setText("")
            nextIsAnswer = false

        }
        else if(nextIsTodo){
            var newTodo = Todo(
                userInput,
                false,
                0
            )
            Thread{
                TodoDatabase.getInstance(
                    requireContext()
                ).todoDao().saveTodo(newTodo)
            }.start()

            val uid = FirebaseAuth.getInstance().uid
            val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
            ref.addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    var tList = snapshot.getValue(User::class.java)
                    //Log.d("list",nList!!.notes!!.size.toString())
                    if(tList?.todo != null){
                        tList.todo!!.add(newTodo)
                        ref.child("todo").setValue(tList.todo)
                    }
                    else{
                        ref.child("todo").setValue(mutableListOf(newTodo))
                    }
                }

            })

            todoList.add(newTodo)
            val message = Chats(
                userInput,
                true
            )
            responseMessageList.add(message)
            val response =
                Chats("Done", false)
            responseMessageList.add(response)
            messageAdapter.notifyDataSetChanged()
            chatInterface.smoothScrollToPosition(messageAdapter.itemCount - 1)
            Input.setText("")
            nextIsTodo = false
        }
        //Else run the normal loop
        else{
            //User Input
            val message = Chats(
                userInput,
                true
            )
            responseMessageList.add(message)
            //Response
            val response = Chats(
                userInput,
                false
            )
            val responseTwo = Chats(
                userInput,
                false
            )
            var input = userInput
            modelInput = Array(1) { FloatArray(bagOfWords.size){0f} }
            if(input.isNotEmpty()){
                var inputWords = input.split(" ")
                for(word in inputWords){
                    val re = Regex("[^A-Za-z0-9 ]")
                    var rWord = re.replace(word, "")
                    rWord = rWord.toLowerCase()
                    stemmer.current = rWord
                    stemmer.stem()
                    rWord = stemmer.current
                    for(i in bagOfWords){
                        if(i == rWord){
                            var position = bagOfWords.indexOf(i)
                            modelInput[0][position-1] = 1f
                        }
                    }
                }
                Log.d("term",modelInput.size.toString())
                //ML model running
                interpreter.run(modelInput,modelOutput)
                var result = modelOutput[0].max()?.let { it1 -> modelOutput[0].indexOf(it1)}
                var tag = labels[result!!]

                //Output
                for(i in intents.intents){
                    if(i.tag == tag){
                        if(modelOutput[0].max()!! > 0.9f){
                            response.textMessage = i.responses.random()
                        }
                        else{
                            response.textMessage = "Sorry i didn't get that"
                        }
                        responseMessageList.add(response)
                        messageAdapter.notifyDataSetChanged()
                        chatInterface.smoothScrollToPosition(messageAdapter.itemCount - 1)
                        var inputWords = userInput.split(" ")
                        Input.setText("")
                        if(tag == "search" && modelOutput[0].max()!! > 0.9f){
                            var searchTerm = ""
                            for(i in inputWords){
                                val re = Regex("[^A-Za-z0-9 ]")
                                var rWord = re.replace(i, "")
                                rWord = rWord.toLowerCase()
                                stemmer.current = rWord
                                stemmer.stem()
                                if(!bagOfWords.contains(stemmer.current)){
                                    searchTerm = i
                                }
                            }
                            if(searchTerm.isNotEmpty()){
                                wikipediaApiCall(
                                    searchTerm
                                ) { result ->
                                    responseTwo.title = result.title.toString()
                                    responseTwo.extract = result.extract.toString()
                                    responseTwo.isResult = true

                                    activity?.runOnUiThread {
                                        if (result.thumbnail != null) {
                                            if (result.thumbnail.source!!.isNotEmpty()) {
                                                Glide.with(this)
                                                    .asBitmap()
                                                    .load(result.thumbnail.source!!)
                                                    .into(object : CustomTarget<Bitmap>(){
                                                        override fun onLoadCleared(placeholder: Drawable?) {
                                                        }

                                                        override fun onResourceReady(
                                                            resource: Bitmap,
                                                            transition: Transition<in Bitmap>?
                                                        ) {
                                                            responseTwo.image = resource
                                                            responseMessageList.add(
                                                                responseTwo
                                                            )
                                                            Input.setText("")
                                                            messageAdapter.notifyDataSetChanged()
                                                            chatInterface.smoothScrollToPosition(
                                                                messageAdapter.itemCount - 1
                                                            )
                                                        }
                                                    })
                                            }
                                        } else {
                                            responseTwo.image = null
                                            responseMessageList.add(responseTwo)
                                            messageAdapter.notifyDataSetChanged()
                                            chatInterface.smoothScrollToPosition(
                                                messageAdapter.itemCount - 1
                                            )

                                        }
                                    }

                                }
                            }else{
                                responseTwo.textMessage = "Sorry I am unable to understand you, could you try again?"
                                responseMessageList.add(responseTwo)
                                messageAdapter.notifyDataSetChanged()
                                chatInterface.smoothScrollToPosition(
                                    messageAdapter.itemCount - 1
                                )
                            }

                        }
                        else if(tag == "joke" && modelOutput[0].max()!! > 0.9f){
                            jokeApiCall(false) { jokeApi ->
                                val type = jokeApi.type

                                requireActivity().runOnUiThread {
                                    if(type == "single"){
                                        responseTwo.textMessage = jokeApi.joke.toString()
                                        responseMessageList.add(responseTwo)
                                        messageAdapter.notifyDataSetChanged()
                                        chatInterface.smoothScrollToPosition(
                                            messageAdapter.itemCount - 1
                                        )
                                    }
                                    else{
                                        responseTwo.textMessage = jokeApi.setup.toString()
                                        val responseThree = Chats(jokeApi.delivery.toString(),false)
                                        responseMessageList.add(responseTwo)
                                        responseMessageList.add(responseThree)
                                        messageAdapter.notifyDataSetChanged()
                                        chatInterface.smoothScrollToPosition(
                                            messageAdapter.itemCount - 1
                                        )
                                    }
                                }

                            }

                        }
                        else if(tag == "rajini" && modelOutput[0].max()!! > 0.9f){
                            jokeApiCall(true) { jokeApi ->
                                requireActivity().runOnUiThread {
                                    responseTwo.textMessage = jokeApi.joke.toString()
                                    responseMessageList.add(responseTwo)
                                    messageAdapter.notifyDataSetChanged()
                                    chatInterface.smoothScrollToPosition(
                                        messageAdapter.itemCount - 1
                                    )
                                }
                            }

                        }
                        else if(tag == "call" && modelOutput[0].max()!! > 0.9f) {
                            var found = false
                            var searchName = ""

                            for (i in inputWords) {
                                val re = Regex("[^A-Za-z0-9 ]")
                                var rWord = re.replace(i, "")
                                rWord = rWord.toLowerCase()
                                stemmer.current = rWord
                                stemmer.stem()
                                if (!bagOfWords.contains(stemmer.current)) {
                                    searchName = i
                                }
                            }
                            if(searchName.isEmpty()){
                                Log.d("bruh", "bruh2test")
                                responseTwo.textMessage = "Sorry I don't think you mentioned any name"
                                responseMessageList.add(responseTwo)
                                messageAdapter.notifyDataSetChanged()
                                chatInterface.smoothScrollToPosition(messageAdapter.itemCount - 1)
                            }
                            if(PreferenceManager.getDefaultSharedPreferences(requireContext()).getBoolean("Can_Call",false)){
                                val contacts = requireActivity().contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,null,null,null)
                                var number = " "
                                while(contacts!!.moveToNext())
                                {
                                    if(contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)).toLowerCase().contains(searchName.toLowerCase(),true)){
                                        found = true
                                        number = contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                                        break
                                    }
                                }
                                if(!found){

                                    responseTwo.textMessage = "Sorry there is no contact with that name"
                                    responseMessageList.add(responseTwo)
                                    messageAdapter.notifyDataSetChanged()
                                    chatInterface.smoothScrollToPosition(messageAdapter.itemCount - 1)
                                }
                                else{
                                    val uri = "tel:" + number
                                    Log.d("num", number)
                                    val intent = Intent(Intent.ACTION_CALL)
                                    intent.setData(Uri.parse(uri))
                                    startActivity(intent)
                                }
                            }

                        }
                        else if(tag == "weather" && modelOutput[0].max()!! > 0.9f){

                            if(PreferenceManager.getDefaultSharedPreferences(requireContext()).getBoolean("Can_Locate",false)){
                                fun getLocation(): Location? {
                                    var locationGps: Location? = null
                                    var locationManager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
                                    var hasGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                                    if (hasGps) {
                                        val localGpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                                        if (localGpsLocation != null){
                                            locationGps = localGpsLocation
                                        }else{
                                            if (ActivityCompat.checkSelfPermission(
                                                    requireContext(),
                                                    Manifest.permission.ACCESS_FINE_LOCATION
                                                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                                                    requireContext(),
                                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                                ) != PackageManager.PERMISSION_GRANTED
                                            ) {
                                                return null
                                            }
                                            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0F, object :
                                                LocationListener {
                                                override fun onLocationChanged(location: Location?) {
                                                    if (location != null) {
                                                        locationGps = location
                                                    }
                                                }
                                                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
                                                }
                                                override fun onProviderEnabled(provider: String?) {
                                                }
                                                override fun onProviderDisabled(provider: String?) {
                                                }

                                            })
                                        }

                                    }
                                    return locationGps
                                }
                                var curLocation = getLocation()
                                if (curLocation != null) {
                                    locationApiCall(curLocation) { locationApi ->
                                        Log.d("works",locationApi.woeid.toString())
                                        locationApi.woeid?.let {
                                            weatherApiCall(it){ weatherApi ->
                                                responseTwo.title = weatherApi.consolidated_weather[0].weather_state_name.toString()
                                                responseTwo.extract = "Temp: "+weatherApi.consolidated_weather[0].the_temp.toString()+"°C"+"\nHumidity: "+weatherApi.consolidated_weather[0].humidity.toString()+"%"
                                                responseTwo.isResult = true
                                                val bUrl = "https://www.metaweather.com/static/img/weather/png/"
                                                var eUrl = ".png"
                                                val url = bUrl + weatherApi.consolidated_weather[0].weather_state_abbr + eUrl
                                                activity?.runOnUiThread {
                                                    if (url.isNotEmpty()) {
                                                        Glide.with(this)
                                                            .asBitmap()
                                                            .load(url)
                                                            .into(object : CustomTarget<Bitmap>(){
                                                                override fun onLoadCleared(placeholder: Drawable?) {
                                                                }

                                                                override fun onResourceReady(
                                                                    resource: Bitmap,
                                                                    transition: Transition<in Bitmap>?
                                                                ) {
                                                                    responseTwo.image = resource
                                                                    responseMessageList.add(
                                                                        responseTwo
                                                                    )
                                                                    Input.setText("")
                                                                    messageAdapter.notifyDataSetChanged()
                                                                    chatInterface.smoothScrollToPosition(
                                                                        messageAdapter.itemCount - 1
                                                                    )
                                                                }
                                                            })
                                                    } else {
                                                        responseTwo.image = null
                                                        responseMessageList.add(responseTwo)
                                                        messageAdapter.notifyDataSetChanged()
                                                        chatInterface.smoothScrollToPosition(
                                                            messageAdapter.itemCount - 1
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }


                            }
                        }
                        else if(tag == "timer" && modelOutput[0].max()!! > 0.9f){
                            var time = wordExtract(input, bagOfWords)
                            Log.d("nice", time)
                            var h = false
                            var ht = 0
                            var m = false
                            var mt = 0
                            var s = true
                            var st = 0
                            if(time.contains("min", true)){
                                m = true
                                s = false
                            }
                            if(time.contains("hour", true)){
                                h = true
                                s = false
                            }
                            if(time.contains("sec", true)){
                                s = true
                            }
                            for(i in time){
                                if(i.isDigit()){
                                    when {
                                        h -> {
                                            ht = ht*10 + i.toString().toInt()
                                        }
                                        m -> {
                                            mt = mt*10 + i.toString().toInt()
                                        }
                                        else -> {
                                            st = st*10 + i.toString().toInt()
                                        }
                                    }
                                }
                                else{
                                    if(h){
                                        h = false
                                    }
                                    if(m){
                                        m = false
                                    }
                                    if(s){
                                        s = false
                                    }
                                }
                            }
                            Log.d("nice", "$ht:$mt:$st")
                            responseTwo.isTimer = true
                            responseTwo.time = (ht*3600 + mt*60 + st)*1000
                            Input.setText("")
                            responseMessageList.add(responseTwo)
                            messageAdapter.notifyDataSetChanged()
                            chatInterface.smoothScrollToPosition(messageAdapter.itemCount - 1)
                        }
                        else if(tag == "note" && modelOutput[0].max()!! > 0.9f) {
                            nextIsNote = true
                        }
                        else if(tag == "todo" && modelOutput[0].max()!! > 0.9f){
                            nextIsTodo = true
                        }
                        else if(tag == "yoda" && modelOutput[0].max()!! > 0.9f){
                            nextIsYoda=true
                        }
                        else if(tag == "quiz" && modelOutput[0].max()!! > 0.9f){
                            jeopardyApiCall(){jeopardyApi ->
                                if(!jeopardyApi.question.isNullOrEmpty()){
                                    responseTwo.textMessage = jeopardyApi.question
                                    answer = jeopardyApi.answer.toString()
                                    nextIsAnswer=true
                                }else{
                                    responseTwo.textMessage = "Unable to fetch the question now"
                                }
                                requireActivity().runOnUiThread {
                                    Input.setText("")
                                    responseMessageList.add(responseTwo)
                                    messageAdapter.notifyDataSetChanged()
                                    chatInterface.smoothScrollToPosition(messageAdapter.itemCount - 1)
                                }
                            }
                        }
                    }
                }
            }
        }
    }


}
