package com.example.luna

import android.annotation.SuppressLint
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import androidx.preference.PreferenceManager
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.luna.classes.User
import com.example.luna.database.NotesDatabase
import com.example.luna.database.SketchDatabase
import com.example.luna.database.TodoDatabase
import com.example.luna.fragments.ChatFragment
import com.example.luna.fragments.NoteFragment
import com.example.luna.fragments.SketchFragment
import com.example.luna.fragments.TodoFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_main.*


@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    var fragmentList : MutableList<Fragment> = ArrayList()
    var isLocked = false


    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("ShortAlarm")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        //Run once

        val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        if(preferences.getBoolean("Scheduler",true)){
            Scheduler()
            preferences.edit().putBoolean("Scheduler",false).apply()
        }
        if(preferences.getBoolean("First_Time",true)){

            var string = preferences.getString("Current_Date","")
            if(!string.isNullOrEmpty()){
                Log.d("main",string)
            }

        }
        val uid = FirebaseAuth.getInstance().uid
        var ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, "Error loading data try again later", Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                Log.d("username",user?.username)
                if (user != null) {
                    if(user.todo != null){
                        Thread{
                            TodoDatabase.getInstance(applicationContext).clearAllTables()
                            for(i in user.todo!!)
                            {
                                TodoDatabase.getInstance(applicationContext).todoDao().saveTodo(i)
                            }
                        }.start()
                    }
                    if(user.notes != null){
                        Thread{
                            NotesDatabase.getInstance(applicationContext).clearAllTables()
                            for(i in user.notes!!) {
                                NotesDatabase.getInstance(applicationContext).notesDao().saveNote(i)
                            }
                        }.start()
                    }
                    if(user.sketch != null){
                        if(user.sketch!!.sketchUrl.isNotEmpty()){
                            Log.d("hello",user.sketch!!.sketchUrl)
                            Glide.with(this@MainActivity)
                                .asBitmap()
                                .load(user.sketch!!.sketchUrl)
                                .into(object : CustomTarget<Bitmap>(){
                                    override fun onLoadCleared(placeholder: Drawable?) {
                                    }

                                    override fun onResourceReady(
                                        resource: Bitmap,
                                        transition: Transition<in Bitmap>?
                                    ) {
                                        user.sketch!!.sketchBitmap = Converter.getBytes(resource)
                                        Thread{
                                            SketchDatabase.getInstance(applicationContext).sketchDao().saveSketch(user.sketch!!)
                                        }.start()
                                    }
                                })
                        }

                    }

                }
            }

        })
        //Main View
        val adapter = MyViewPagerAdapter(supportFragmentManager)

        fragmentList.add(ChatFragment())
        fragmentList.add(TodoFragment())
        fragmentList.add(NoteFragment())
        fragmentList.add(SketchFragment())
        adapter.addFragment(fragmentList)
        viewPager.adapter = adapter
        tabs.setupWithViewPager(viewPager)




        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

            override fun onPageScrollStateChanged(state: Int) {
                Log.d("asdda", isLocked.toString())
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }
            override fun onPageSelected(position: Int) {

            }

        })

    }


    class MyViewPagerAdapter(manager: FragmentManager) : FragmentPagerAdapter(manager){

        private var fragmentList : MutableList<Fragment> = ArrayList()

        override fun getItem(position: Int): Fragment {
            return fragmentList[position]
        }

        override fun getCount(): Int {
            return fragmentList.size
        }

        fun addFragment(fragment: MutableList<Fragment>){
            fragmentList = fragment
        }


        override fun getPageTitle(position: Int): CharSequence? {
            return " "
        }

    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun Scheduler(){
        val componentName = ComponentName(this, JobService::class.java)
        val info: JobInfo = JobInfo.Builder(123, componentName)
            .setPeriodic(3 * 60 * 60 * 1000)
            .setPersisted(true)
            .build()
        val scheduler: JobScheduler =
            getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        val resultCode: Int = scheduler.schedule(info)
        if (resultCode == JobScheduler.RESULT_SUCCESS) {
            Log.d("main", "Job scheduled")
        } else {
            Log.d("main", "Job scheduling failed")
        }
    }

}
