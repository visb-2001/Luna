package com.example.luna

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.job.JobParameters

import android.app.job.JobService
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.Handler
import androidx.preference.PreferenceManager
import android.util.Log
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.os.HandlerCompat.postDelayed
import com.example.luna.database.NotesDatabase
import com.example.luna.database.TodoDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAccessor


@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class JobService : JobService() {
    private val channelId = "i.apps.notifications"
    private val description = "notification"
    private var jobCancelled = false
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartJob(params: JobParameters): Boolean {
        Log.d("main", "Job started")
        doBackgroundWork(params)
        return true
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun doBackgroundWork(params: JobParameters) {
        val intent = Intent(this,MainActivity::class.java)
        lateinit var notificationManager : NotificationManager
        lateinit var builder: Notification.Builder
        var notificationText = ""
        val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val dateStr = preferences.getString("Current_Date","")
        if(!dateStr.isNullOrEmpty()){
            val date = LocalDate.parse(dateStr)
            var dateDiff = ChronoUnit.DAYS.between(date,LocalDate.now())
            if(dateDiff > 0){
                Thread{
                    TodoDatabase.getInstance(this).todoDao().readNonPersistTodo().forEach { todo ->
                        TodoDatabase.getInstance(this).todoDao().deleteTodo(todo)
                    }
                    val uid = FirebaseAuth.getInstance().uid
                    if(!uid.isNullOrEmpty()){
                        FirebaseDatabase.getInstance().getReference("/users/$uid/todo").setValue(null)
                    }
                    preferences.edit().putString("Current_Date",LocalDate.now().toString()).commit()
                }.start()
            }

        }else{
            preferences.edit().putString("Current_Date",LocalDate.now().toString()).commit()
        }
        if(preferences.getBoolean("First_Time_Notif",true)){
            notificationText = "Welcome to Luna"
            preferences.edit().putBoolean("First_Time_Notif",false).commit()
        }else{
            Thread{
                //TodoDatabase.getInstance(this).clearAllTables()
                val number = TodoDatabase.getInstance(this).todoDao().todoNumber()
                Log.d("hey",number.toString())
                if(number == 0){
                    notificationText = "Luna misses you!"
                }
                else{
                    notificationText = "You still have $number tasks on your to-do list."
                }
            }.start()
        }


        Handler().postDelayed({
            val pendingIntent = PendingIntent.getActivity(this,
                0,intent,PendingIntent.FLAG_UPDATE_CURRENT)

            //RemoteViews are used to use the content of
            // some different layout apart from the current activity layout

            val contentView = RemoteViews(packageName,
                R.layout.activity_main)

            //checking if android version is greater than oreo(API 26) or not
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                var notificationChannel = NotificationChannel(
                    channelId,description, NotificationManager.IMPORTANCE_HIGH)
                notificationChannel.enableLights(true)
                notificationChannel.lightColor = Color.GREEN
                notificationChannel.enableVibration(false)
                notificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(notificationChannel)

                builder = Notification.Builder(this,channelId)
                    .setContentText(notificationText)
                    .setSmallIcon(R.drawable.logo)
                    .setContentIntent(pendingIntent)
            }else{

                builder = Notification.Builder(this)
                    .setContentText(notificationText)
                    .setSmallIcon(R.drawable.logo)
                    .setContentIntent(pendingIntent)
            }
            notificationManager.notify(1234,builder.build())
            jobFinished(params, false)
        }, 2000)
    }

    override fun onStopJob(params: JobParameters): Boolean {
        return false
    }

}