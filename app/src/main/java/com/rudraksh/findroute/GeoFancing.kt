package com.rudraksh.findroute

import com.google.android.gms.location.Geofence
import android.R.attr.radius
import android.R.id
import java.util.*
import java.util.UUID.randomUUID


fun geofence(latitude:Double,longitude:Double): Geofence {
    val id = UUID.randomUUID().toString()
    return Geofence.Builder()
        .setRequestId(id)
        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
        .setCircularRegion(latitude, longitude, radius.toFloat())
        .setExpirationDuration(Geofence.NEVER_EXPIRE)
        .build()
}