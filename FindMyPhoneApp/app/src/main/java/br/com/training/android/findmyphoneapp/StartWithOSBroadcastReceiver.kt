package br.com.training.android.findmyphoneapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class StartWithOSBroadcastReceiver: BroadcastReceiver() {

    override fun onReceive(p0: Context?, p1: Intent?) {
        if(p1!!.action == Intent.ACTION_BOOT_COMPLETED) {
            p0!!.startService(Intent(p0, MyService::class.java))
        }
    }

}