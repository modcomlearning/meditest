package com.modcom.meditest

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_maps1.*
import java.io.IOException


class Maps1Activity() : FragmentActivity(), OnMapReadyCallback, LocationListener,
    GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private var mMap: GoogleMap? = null
    internal lateinit var mLastLocation: Location
    internal var mCurrLocationMarker: Marker? = null
    internal var mGoogleApiClient: GoogleApiClient? = null
    internal lateinit var mLocationRequest: LocationRequest
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQ_CODE = 1000;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps1)
        progress.visibility = View.GONE
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        getCurrentLocation()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient()
                mMap!!.isMyLocationEnabled = true

            }
        } else {
            buildGoogleApiClient()
            mMap!!.isMyLocationEnabled = true


        }

         mMap!!.setOnMyLocationButtonClickListener {

                getCurrentLocation();

           true
         }

        var addressList1: List<Address>? = null
        mMap!!.setOnMarkerClickListener { marker ->

//            val geoCoder = Geocoder(this)
//            try {
//                addressList1 = geoCoder.getFromLocation(marker.position.latitude, marker.position.latitude,1)
//
//            } catch (e: IOException) {
//                e.printStackTrace()
//            }
//
//            if (addressList1 != null) {
//                if(addressList1!!.isNotEmpty()) {
//                    val address = addressList1!![0]
                    val sharedPref: SharedPreferences = getSharedPreferences("mediprefs", Context.MODE_PRIVATE)
                    val builder = AlertDialog.Builder(this)
                    //set title for alert dialog
                    builder.setTitle("Location")
                    //set message for alert dialog
                    builder.setMessage("Use Selected Location?")
                    builder.setIcon(android.R.drawable.ic_dialog_alert)
                    //performing positive action
                    builder.setPositiveButton("Yes"){dialogInterface, which ->
                        val editor = sharedPref.edit()
                        editor.putString("lat", marker.position.latitude.toString())
                        editor.putString("lon", marker.position.longitude.toString())

                       // if (!address.locality.isNullOrEmpty()){
                            editor.putString("locality", "Unknown")
                       // }



                       // if (!address.adminArea.isNullOrEmpty()){
                            editor.putString("admin_area", "Unknown")
                       // }

                        //editor.putString("sub_admin_area", address.subAdminArea.toString())
                        editor.apply()
                        onBackPressed()
                        Toast.makeText(applicationContext,"Location Captured",Toast.LENGTH_SHORT).show()

                    }
                    //performing negative action
                    builder.setNegativeButton("No"){dialogInterface, which ->
                        Toast.makeText(applicationContext,"Not Found, Please provide more description of a place",Toast.LENGTH_LONG).show()
                    }
                    // Create the AlertDialog
                    val alertDialog: AlertDialog = builder.create()
                    // Set other dialog properties
                    alertDialog.setCancelable(false)
                    alertDialog.show()
//                }
//
//                else {
//                    Toast.makeText(applicationContext,"Not Found, Please Select another location or press back",Toast.LENGTH_LONG).show()
//                    progress.visibility = View.GONE
//                }
//            }
//            else {
//                Toast.makeText(applicationContext,"Not Found, Please Select another location or press back",Toast.LENGTH_LONG).show()
//                progress.visibility = View.GONE
//            }


            true
        }

    }

    @Synchronized
    protected fun buildGoogleApiClient() {
        mGoogleApiClient = GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API).build()
        mGoogleApiClient!!.connect()
    }

    override fun onConnected(bundle: Bundle?) {

        mLocationRequest = LocationRequest()
        mLocationRequest.interval = 1000
        mLocationRequest.fastestInterval = 1000
        mLocationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.getFusedLocationProviderClient(this)
        }
    }

    override fun onConnectionSuspended(i: Int) {
        val builder = AlertDialog.Builder(this)
        //set title for alert dialog
        builder.setTitle("Location Error")
        //set message for alert dialog
        builder.setMessage("No Connection, Please activate your internet and GPS")
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        //performing positive action
        builder.setPositiveButton("OK"){dialogInterface, which ->
            finish()
        }

        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(false)
        alertDialog.show()
    }


    override fun onLocationChanged(location: Location) {

            mLastLocation = location
            if (mCurrLocationMarker != null) {
                mCurrLocationMarker!!.remove()
            }
            //Place current location marker
            val latLng = LatLng(location.latitude, location.longitude)
           // Toast.makeText(applicationContext, ""+location.latitude+""+location.longitude, Toast.LENGTH_LONG).show()
            val markerOptions = MarkerOptions()
            markerOptions.position(latLng)
            markerOptions.title("Current Position")
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
            markerOptions.draggable(true)
            mCurrLocationMarker = mMap!!.addMarker(markerOptions)

            //move map camera
            mMap!!.moveCamera(CameraUpdateFactory.newLatLng(latLng))
            mMap!!.animateCamera(CameraUpdateFactory.zoomTo(11f))

            //stop location updates
            if (mGoogleApiClient != null) {
                LocationServices.getFusedLocationProviderClient(this)
            }




    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        val builder = AlertDialog.Builder(this)
        //set title for alert dialog
        builder.setTitle("Location Error")
        //set message for alert dialog
        builder.setMessage("No Connection, Please activate your internet and GPS")
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        //performing positive action
        builder.setPositiveButton("OK"){dialogInterface, which ->
           finish()
        }

        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    fun needHelp(view: View) {
        val builder = AlertDialog.Builder(this)
        //set title for alert dialog
        builder.setTitle("Help")
        //set message for alert dialog
        builder.setMessage("1. Enter location on the search input, press search button, long press the RED marker drag/drop to preferred location. click on marker to pin a location \n\n" +
                "2. Activate GPS in your phone settings, once GPS is ON,  click on Location icon on top right of this screen, click the marker to use your current location.")
        builder.setIcon(android.R.drawable.ic_dialog_info)

        //performing positive action
        builder.setPositiveButton("OK"){dialogInterface, which ->

            // Toast.makeText(applicationContext,"Location Captured",Toast.LENGTH_SHORT).show()
        }

        //performing positive action
        builder.setNegativeButton("Activate GPS"){dialogInterface, which ->
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            finish()
            // Toast.makeText(applicationContext,"Location Captured",Toast.LENGTH_SHORT).show()
        }

        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(false)
        alertDialog.show()

    }

    fun searchLocation(view: View) {
        val sharedPref: SharedPreferences = getSharedPreferences("mediprefs", Context.MODE_PRIVATE)
        progress.visibility = View.VISIBLE
        val locationSearch:EditText = findViewById<EditText>(R.id.editText)
        lateinit var location: String
        location = locationSearch.text.toString()
        var addressList: List<Address>? = null

        if (location == null || location == "") {
            Toast.makeText(applicationContext,"Please type your preferred location",Toast.LENGTH_SHORT).show()
            progress.visibility = View.GONE
        }
        else{

            val geoCoder = Geocoder(this)
            try {
                addressList = geoCoder.getFromLocationName(location, 1)

            } catch (e: IOException) {
                e.printStackTrace()
            }

            if (addressList != null) {
                if(addressList.isNotEmpty()){


                        val address = addressList!![0]

                        val latLng = LatLng(address.latitude, address.longitude)
                        //Toast.makeText(applicationContext,"Test: l:"+address.locality+" AA:"+address.adminArea+" ad:"+address.subAdminArea,Toast.LENGTH_LONG).show()
                        mMap!!.addMarker(
                            MarkerOptions().position(latLng).title(location).draggable(true)
                        )
                        mMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17f))
                        progress.visibility = View.GONE
                       Toast.makeText(applicationContext,"Long press on the Red marker, drag to preferred place and tap on it to pin a location.",Toast.LENGTH_LONG).show()
                    //Use Searched Location? if No, long press the marker, drag to preferred place and tap it.

                        val builder = AlertDialog.Builder(this)
                        //set title for alert dialog
                        builder.setTitle("Location")
                        //set message for alert dialog
                        builder.setMessage("Use Searched Location? if No, long press the marker, drag to preferred place and tap it. ")
                        builder.setIcon(android.R.drawable.ic_dialog_alert)

                        //performing positive action
                        builder.setPositiveButton("Yes") { dialogInterface, which ->
                            val editor = sharedPref.edit()

                               editor.putString("lat", address.latitude.toString())
                               editor.putString("lon", address.longitude.toString())

                                if (!address.locality.isNullOrEmpty()) {
                                    editor.putString("locality", address.locality.toString())
                                }

                                if (!address.adminArea.isNullOrEmpty()) {
                                    editor.putString("admin_area", address.adminArea.toString())
                                }
//                        editor.putString("sub_admin_area", address.subAdminArea.toString())
                                editor.apply()
                                onBackPressed()
                                Toast.makeText(
                                    applicationContext,
                                    "Location Captured",
                                    Toast.LENGTH_LONG
                                ).show()



                        }

                        //performing negative action
                        builder.setNegativeButton("No") { dialogInterface, which ->
                            Toast.makeText(
                                applicationContext,
                                "Not Found, Please provide more description of a place",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        // Create the AlertDialog
                        val alertDialog: AlertDialog = builder.create()
                        // Set other dialog properties
                        alertDialog.setCancelable(false)
                        //alertDialog.show()
                        //Toast.makeText(applicationContext, address.latitude.toString() + " " + address.longitude, Toast.LENGTH_LONG).show()


                }

                else {
                    Toast.makeText(applicationContext,"Please provide more description of a place and search",Toast.LENGTH_LONG).show()
                    progress.visibility = View.GONE
                }

            }

            else {
                Toast.makeText(applicationContext,"Please provide more description of a place and search",Toast.LENGTH_LONG).show()
                progress.visibility = View.GONE
            }

        }
    }


    private fun getCurrentLocation() {
        progress.visibility = View.VISIBLE
        val sharedPref: SharedPreferences = getSharedPreferences("mediprefs", Context.MODE_PRIVATE)

        // checking location permission
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // request permission
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQ_CODE);
            return
        }

        var addressList: List<Address>? = null

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                // getting the last known or current location
                if (location != null){
                val latLng = LatLng(location.latitude, location.longitude)


                val geoCoder = Geocoder(this)
                try {
                    addressList = geoCoder.getFromLocation(location.latitude, location.longitude, 1)

                } catch (e: IOException) {
                    e.printStackTrace()
                }

                if (addressList != null) {
                    if (addressList!!.isNotEmpty()) {
                        val address = addressList!![0]


                        mMap!!.addMarker(
                            MarkerOptions().position(latLng).title("You are here").draggable(true)
                        )
                        mMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
                        val builder = AlertDialog.Builder(this)
                        //set title for alert dialog
                        builder.setTitle("Location")
                        //set message for alert dialog
                        builder.setMessage("Use Current Location, if No, long press the marker, drag to preferred place and click on it. ")
                        builder.setIcon(android.R.drawable.ic_dialog_map)

                        //performing positive action
                        builder.setPositiveButton("Yes") { dialogInterface, which ->
                            val editor = sharedPref.edit()
                            editor.putString("lat", location.latitude.toString())
                            editor.putString("lon", location.longitude.toString())
                            if (!address.locality.isNullOrEmpty()){
                                editor.putString("locality", address.locality.toString())
                            }

                            if (!address.adminArea.isNullOrEmpty()){
                                editor.putString("admin_area", address.adminArea.toString())
                            }
                           // editor.putString("sub_admin_area", address.subAdminArea.toString())
                            editor.apply()
                            onBackPressed()
                            Toast.makeText(applicationContext,"Location Captured",Toast.LENGTH_LONG).show()
                        }

                        //performing negative action
                        builder.setNegativeButton("No") { dialogInterface, which ->
                            Toast.makeText(
                                applicationContext,
                                "Please Enter another location or long press, move the marker and tap it",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        // Create the AlertDialog
                        val alertDialog: AlertDialog = builder.create()
                        // Set other dialog properties
                        alertDialog.setCancelable(false)
                        alertDialog.show()


                    } else {
                        Toast.makeText(
                            applicationContext,
                            "Please Enter another location or long press, move the marker and tap it",
                            Toast.LENGTH_LONG
                        ).show()
                        progress.visibility = View.GONE
                    }
                } else {
                    Toast.makeText(
                        applicationContext,
                        "Not Found, Please enter another location or press back",
                        Toast.LENGTH_LONG
                    ).show()
                    progress.visibility = View.GONE
                }
                progress.visibility = View.GONE
            }
                else {
                    Toast.makeText(applicationContext,"Activate GPS and CLICK on Location icon on top right to use current location",Toast.LENGTH_LONG).show()
                    progress.visibility = View.GONE
//                    val builder = AlertDialog.Builder(this)
//                    //set title for alert dialog
//                    builder.setTitle("Information")
//                    //set message for alert dialog
//                    builder.setMessage("Please Activate Your GPS")
//                    builder.setIcon(android.R.drawable.ic_dialog_alert)
//
//                    //performing positive action
//                    builder.setPositiveButton("OK"){dialogInterface, which ->
//
//                         startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
//                         finish()
//                       // Toast.makeText(applicationContext,"Location Captured",Toast.LENGTH_SHORT).show()
//                    }
//
//                    // Create the AlertDialog
//                    val alertDialog: AlertDialog = builder.create()
//
//                    // Set other dialog properties
//                    alertDialog.setCancelable(false)
//                    alertDialog.show()
                }

            }
            .addOnFailureListener {
                progress.visibility = View.GONE
                val builder = AlertDialog.Builder(this)
                //set title for alert dialog
                builder.setTitle("Information")
                //set message for alert dialog
                builder.setMessage("Please Activate Your GPS")
                builder.setIcon(android.R.drawable.ic_dialog_alert)

                //performing positive action
                builder.setPositiveButton("OK"){dialogInterface, which ->
                    finish()
                    // Toast.makeText(applicationContext,"Location Captured",Toast.LENGTH_SHORT).show()
                }

                // Create the AlertDialog
                val alertDialog: AlertDialog = builder.create()
                // Set other dialog properties
                alertDialog.setCancelable(false)
                alertDialog.show()
            }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        when (requestCode) {
            LOCATION_PERMISSION_REQ_CODE -> {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission granted

                } else {
                    progress.visibility = View.GONE
                    // permission denied
                    val builder = AlertDialog.Builder(this)
                    //set title for alert dialog
                    builder.setTitle("Information")
                    //set message for alert dialog
                    builder.setMessage("Please Activate Your GPS & accept Permissions")
                    builder.setIcon(android.R.drawable.ic_dialog_alert)

                    //performing positive action
                    builder.setPositiveButton("OK"){dialogInterface, which ->
                        finish()
                        // Toast.makeText(applicationContext,"Location Captured",Toast.LENGTH_SHORT).show()
                    }

                    // Create the AlertDialog
                    val alertDialog: AlertDialog = builder.create()
                    // Set other dialog properties
                    alertDialog.setCancelable(false)
                    alertDialog.show()
                    finish()
                }
            }
        }
    }

}