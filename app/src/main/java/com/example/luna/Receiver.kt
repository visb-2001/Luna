package com.example.luna

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log


class Receiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("main","still running")

        val intent = Intent(context, Service::class.java)
        context!!.startService(intent)
    }
}