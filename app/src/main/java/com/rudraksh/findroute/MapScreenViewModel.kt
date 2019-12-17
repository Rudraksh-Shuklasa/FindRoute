package com.rudraksh.findroute

import android.app.Application
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

class MapScreenViewModel(application: Application) : AndroidViewModel(application)
{
    val currentLocation=MutableLiveData<String>()

    private val locationData = LocationLiveData(application)

    val startLocationlistener=MutableLiveData<Boolean>()

    val startSearching=MutableLiveData<Boolean>()

    fun getLocationData() = locationData

    init {


        startLocationlistener.value=false
    }


    fun getCurrentLocation(view: View)
    {
        startLocationlistener.value=true
    }

    fun openSearch(view:View)
    {
        startSearching.value=true
    }
}