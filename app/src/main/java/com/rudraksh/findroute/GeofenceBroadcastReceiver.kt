package com.rudraksh.findroute

import android.app.IntentService
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.support.v4.media.MediaBrowserCompat
import android.text.TextUtils
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver : BroadcastReceiver(){

    companion object {
        val TAG = "GeofenceBroadcastReceiverIS"
    }

    val TAG="oneNotification"
    init {
        Log.d(TAG, "===============> GeofenceTransitionsIntentService()")
    }

    override fun onReceive(p0: Context?, p1: Intent?) {
       val intent=Intent(p0,LocationReach::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        p0?.startActivity(intent)


    }




}