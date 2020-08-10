package com.example.luna

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.widget.Toast


class Service : Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Task(applicationContext)

        return START_STICKY
    }

    private fun Task(context: Context) {
        Log.d("main","still running")
        Toast.makeText(context,"heyooo",Toast.LENGTH_SHORT).show()
    }

    override fun onBind(intent: Intent?): IBinder {
        // TODO: Return the communication channel to the service.
        throw UnsupportedOperationException("Not yet implemented")
    }
}