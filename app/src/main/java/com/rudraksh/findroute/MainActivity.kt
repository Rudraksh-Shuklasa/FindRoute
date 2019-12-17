package com.rudraksh.findroute

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Observer
import com.rudraksh.findroute.databinding.ActivityMainBinding
import android.location.Geocoder
import com.google.android.libraries.places.api.Places
import java.util.*
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.libraries.places.widget.Autocomplete
import android.content.Intent
import com.google.android.libraries.places.api.model.Place
import java.util.Arrays.asList
import android.R.attr.data
import android.app.Activity
import android.app.PendingIntent
import android.net.Uri
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*
import com.google.android.libraries.places.widget.AutocompleteActivity
import java.lang.Exception


class MainActivity : AppCompatActivity(),GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
    ResultCallback<Status> {

    lateinit var mapViewmodel:MapScreenViewModel
    val locationRequest=12
    val AUTOCOMPLETE_REQUEST_CODE=13
    var apiKey = ""
    var sourceLatlong=""
    private lateinit var mGeofenceList: ArrayList<Geofence>
    private lateinit var mGoogleApiClient: GoogleApiClient
    private val TAG="log"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        apiKey=getString(R.string.api_key)
        val binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        mapViewmodel = ViewModelProviders.of(this).get(MapScreenViewModel::class.java!!)
        binding.setLifecycleOwner(this)
        binding.mapViewModel=mapViewmodel
        val permission = ContextCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_COARSE_LOCATION)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            makeRequest()
        }


        mGeofenceList = ArrayList<Geofence>()
        buildGoogleApiClient()

        mapViewmodel.startLocationlistener.observe(this, Observer {
            if(it)
            {
                startLocationUpdate()
            }
        })


        mapViewmodel.startSearching.observe(this, Observer {
            if(it)
            {
                onSearchCalled()
            }
        })


        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), apiKey);
        }
        else{ }

        val placesClient = Places.createClient(this)


    }

    private fun makeRequest() {
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION),
            locationRequest)
    }

    private fun startLocationUpdate() {
        mapViewmodel.getLocationData().observe(this, Observer {

            sourceLatlong=it.latitude.toString()+","+it.longitude.toString()
            val gcd = Geocoder(this, Locale.getDefault())
            val addresses = gcd.getFromLocation(it.latitude, it.longitude, 1)

            if(addresses.size>0)
            {
                val locality=addresses[0].locality
                val address=addresses[0].getAddressLine(0)

                if (address != null) {
                    mapViewmodel.currentLocation.value=address+","+locality
//                    val strs = address.split(",").toTypedArray()
//                    if (strs.count() == 1) {
//                        if (locality != null && locality != "") {
//
//                            mapViewmodel.currentLocation.value="$locality, ${strs[0]}"
//                        }
//                        mapViewmodel.currentLocation.value="$strs[0]"
//
//                    }
//                    for (str in strs) {
//                        var tempStr = ""
//                        if (tempStr != str) {
//                            tempStr = tempStr.trim()
//                            if (locality != null && locality != "" && locality != tempStr) {
//                                mapViewmodel.currentLocation.value="$locality"
//                            }
//                            else{
//                                mapViewmodel.currentLocation.value="$tempStr"
//
//
//                            }
//
//                        }
//                    }
                }
            }
        })
    }


    fun onSearchCalled() {
        // Set the fields to specify which types of place data to return.
        val fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG)
        // Start the autocomplete intent.
        val intent = Autocomplete.IntentBuilder(
            AutocompleteActivityMode.FULLSCREEN, fields
        ).setCountry("IN") //INDIA
            .build(this)
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
    }


    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            locationRequest -> {

                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(this,"Without permission you can not access",Toast.LENGTH_LONG).show()

                }
                else {

                }


            }

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode)
        {
            AUTOCOMPLETE_REQUEST_CODE->{
                if (resultCode === Activity.RESULT_OK) {
                    val place = Autocomplete.getPlaceFromIntent(data!!)


                    if (!mGoogleApiClient.isConnected) {
                        Toast.makeText(this@MainActivity, "Google API Client not connected!", Toast.LENGTH_SHORT)
                            .show()
                    }
                    else{

//                        populateGeofenceList(23.0257744,72.5009945)
                        populateGeofenceList(place.latLng?.latitude!!,place.latLng!!.longitude)
                        addGeofencesHandler()
                        val toPlace=place.latLng?.latitude.toString()+","+place.latLng!!.longitude.toString()

                        val uriToPass="http://maps.google.com/maps?saddr="+sourceLatlong+"&daddr="+toPlace
                        val intent = Intent(
                            android.content.Intent.ACTION_VIEW,
                            Uri.parse(uriToPass)
                        )
                        startActivity(intent)

                    }






                } else if (resultCode === AutocompleteActivity.RESULT_ERROR) {
                    // TODO: Handle the error.
                    val status = Autocomplete.getStatusFromIntent(data!!)
                    Toast.makeText(
                        this,
                        "Error: " + status.statusMessage,
                        Toast.LENGTH_LONG
                    ).show()
                    Log.i("a", status.statusMessage)
                } else if (resultCode === Activity.RESULT_CANCELED) {
                    // The user canceled the operation.
                }
            }
        }
    }





    private fun addGeofencesHandler() {
        try{
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        LocationServices.getGeofencingClient(this).addGeofences(getGeofencingRequest(), getGeofencePendingIntent())?.run {
           addOnSuccessListener {

           }
            addOnFailureListener {

            }

        }
    }catch (e:Exception)
    {
        e.message
    }
    // Result processed in onResult().
    }

    // Check for permission to access Location


    private fun populateGeofenceList(latitude:Double,longitude:Double) {

        try{
            mGeofenceList.add(Geofence.Builder().setRequestId("GeoLocation")
                .setCircularRegion(latitude,longitude, 500F)
                .setExpirationDuration(Geofence.NEVER_EXPIRE )
                .setTransitionTypes(
                   Geofence.GEOFENCE_TRANSITION_DWELL )
                .setLoiteringDelay(1000)
                .build())
        }catch (e:Exception)
        {
            e.message
        }


    }

    private fun buildGoogleApiClient() {
        try{
        mGoogleApiClient = GoogleApiClient.Builder(this).addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API)
            .build()
    }catch (e:Exception)
    {
        e.message
    }

}




    private fun getGeofencingRequest(): GeofencingRequest {
        val builder = GeofencingRequest.Builder()
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL)
        builder.addGeofences(mGeofenceList)
        return builder.build()
    }

    private fun getGeofencePendingIntent(): PendingIntent {
        Log.d(TAG, "===============> getGeofencePendingIntent()")
        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling addgeoFences()
        return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    override fun onStart() {
        super.onStart()
        if (!mGoogleApiClient.isConnecting || !mGoogleApiClient.isConnected) {
            mGoogleApiClient.connect()
        }
    }

    override fun onStop() {
        super.onStop()
        if (mGoogleApiClient.isConnecting || mGoogleApiClient.isConnected) {
            mGoogleApiClient.disconnect()
        }
    }

    override fun onConnected(bundle: Bundle?) {
    }

    override fun onConnectionSuspended(i: Int) {

    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {

    }

    override fun onResult(status: Status) {
        Log.d(TAG, "===============> onResult()")
        if (status.isSuccess) {

        } else {
            // Get the status code for the error and log it using a user-friendly message.
            val errorMessage = GeofenceStatusCodes.getStatusCodeString(status.statusCode)
            Log.d(TAG, "===============> errorMessage: " + errorMessage)
        }
    }


}
