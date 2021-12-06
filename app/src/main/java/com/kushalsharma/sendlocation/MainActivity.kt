package com.kushalsharma.sendlocation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.*
import com.kushalsharma.sendlocation.databinding.ActivityMainBinding
import com.squareup.seismic.ShakeDetector
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import com.vmadalin.easypermissions.models.PermissionRequest
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), ShakeDetector.Listener,
    EasyPermissions.PermissionCallbacks {

    private lateinit var binding: ActivityMainBinding

    // for location
    var mFusedLocationClient: FusedLocationProviderClient? = null
    private lateinit var mLocationRequest: LocationRequest

    //seismic library
    var sd: ShakeDetector? = null


    companion object {
        const val PERMISSION_REQUEST_CODE = 1
    }


    @SuppressLint("WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //method for shake detection
        startShakeDetector()

        // for location
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        //method for requesting permission
        requestPermissions()


        // method to get the location
        getLastLocation()

        //intent for askPhoneNo activity
        binding.btnEditNumber.setOnClickListener {
            val intent = Intent(this, AskPhoneNo::class.java)
            startActivity(intent)
        }

        //this method helps to check if user
        //had written emergency numbers.
        //If there are no emergency numbers saved by user
        // then user will be redirected to AskPhoneNo Activity
        updateUi()


    }

    @SuppressLint("WrongConstant")
    private fun updateUi() {

        //getSharedPreferencesData() method returns sharedPref of
        // those emergency numbers saved by the user.
        val sh = getSharedPreferencesData()

        val s1 = sh!!.getString("number1", null)
        val s2 = sh.getString("number2", null)
        val s3 = sh.getString("number3", null)

        if (s1 == null && s2 == null && s3 == null) {
            val askPhonNoIntent = Intent(this, AskPhoneNo::class.java)
            startActivity(askPhonNoIntent)
        } else {
            //we will save these s1 s2 s3
            //by shaking the phone a message will be sent to those numbers
            Log.d("nnnn", "just a message")
        }

    }

    private fun startShakeDetector() {
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sd = ShakeDetector(this)
        sd!!.start(sensorManager)

    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        // check if permissions are given
        if (hasPermission()) {

            // check if location is enabled
            if (isLocationEnabled()) {

                // getting last location from FusedLocationClient object
                mFusedLocationClient!!.lastLocation.addOnCompleteListener {
                    val location: Location = it.result
                    if (location == null) {
                        requestNewLocationData()
//                        Toast.makeText(this, "Your Location is null", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.d("Traceing", location.getLatitude().toString())
                        val latitude = location.latitude.toString()
                        val longitude = location.longitude.toString()
                        val gMapUrl =
                            "https://www.google.com/maps/search/?api=1&query=$latitude,$longitude"
                        currentLocation.text = gMapUrl


                    }
                }
            } else {
                Toast.makeText(this, "Please turn on" + " your location...", Toast.LENGTH_LONG)
                    .show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            // if permissions aren't available,
            // request for permissions
            requestPermissions()
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {

        // Initializing LocationRequest
        // object with appropriate methods

        mLocationRequest = LocationRequest()
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        mLocationRequest.setInterval(5)
        mLocationRequest.setFastestInterval(0)
        mLocationRequest.setNumUpdates(1)

        // setting LocationRequest
        // on FusedLocationClient
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationClient!!.requestLocationUpdates(
            mLocationRequest,
            mLocationCallback,
            Looper.myLooper()
        )
    }

    private val mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation = locationResult.lastLocation
            val latitude = mLastLocation.latitude.toString()
            val longitude = mLastLocation.longitude.toString()
            val gMapUrl =
                "https://www.google.com/maps/search/?api=1&query=$latitude,$longitude"
            currentLocation.text = gMapUrl
        }
    }


    // method to check if location is enabled
    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }


    //When user shake the phone this method will called
    override fun hearShake() {

        //taking all the saved emergency number
        val sh = getSharedPreferencesData()
        val s1 = sh!!.getString("number1", null)
        val s2 = sh.getString("number2", null)
        val s3 = sh.getString("number3", null)


//        val number = binding.phNoET.getText().toString()

        //taking the current location of the user
        val gMapUrl = currentLocation.getText().toString()

        // message for sms
        val msg =
            "Hi, I'm in trouble and need your help, please reach out to me. Here's my exact location: $gMapUrl"


        //below code is for sending the sms to emergency numbers
        try {
            val smsManager: SmsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(s1, null, msg, null, null)
            smsManager.sendTextMessage(s2, null, msg, null, null)
            smsManager.sendTextMessage(s3, null, msg, null, null)
            Toast.makeText(applicationContext, "Message Sent", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Log.d("error", "${e.toString()}, \n ${e.message}")
            Toast.makeText(
                applicationContext,
                "Your Phone number is not correct!",
                Toast.LENGTH_LONG
            ).show()
        }

//        Toast.makeText(this, "phone shaked", Toast.LENGTH_SHORT).show()
    }


    private fun hasPermission() =
        EasyPermissions.hasPermissions(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.SEND_SMS
        )


    private fun requestPermissions() {
        val request = PermissionRequest.Builder(this)
            .code(PERMISSION_REQUEST_CODE)
            .perms(
                arrayOf(
                    Manifest.permission.SEND_SMS,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
            .rationale("This app will not work out without these Permissions.")
            .build()
        EasyPermissions.requestPermissions(this, request)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {

        if (EasyPermissions.somePermissionDenied(this, perms.first())) {
            SettingsDialog.Builder(this).build().show()
        } else {
            requestPermissions()

        }


    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {

        Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
    }

    //start the shakeDetector again
    override fun onResume() {
        super.onResume()
        startShakeDetector()
    }

    // stop the shakeDetector
    override fun onPause() {
        super.onPause()
        sd!!.stop()
    }

    @SuppressLint("WrongConstant")
    fun getSharedPreferencesData(): SharedPreferences? {
        val sh = getSharedPreferences("Number", MODE_APPEND)
        return sh
    }
}