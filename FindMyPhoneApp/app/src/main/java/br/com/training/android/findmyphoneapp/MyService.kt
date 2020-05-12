package br.com.training.android.findmyphoneapp

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class MyService: Service() {
    private var dbReference: DatabaseReference? = null
    private var myLocation: Location? = null

    companion object {
        var isServiceRunning = false
    }

    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onCreate() {
        super.onCreate()

        dbReference = FirebaseDatabase.getInstance().reference
        isServiceRunning = true
    }

    @SuppressLint("MissingPermission")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val myUserLocation = MyLocationListener()
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val userData = UserData(applicationContext)
        val myPhoneNumber = userData.loadPhoneNumber()
        val dateFormat = SimpleDateFormat("yyyy/MMM/dd HH:MM:ss")
        val date = Date()

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3, 3f, myUserLocation)

        dbReference!!.child(LoginActivity.userDBChildPath).child(myPhoneNumber)
            .child(LoginActivity.requestingDBChildPath).addValueEventListener(
                object: ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {}

                    override fun onDataChange(p0: DataSnapshot) {
                        dbReference!!.child(LoginActivity.userDBChildPath).child(myPhoneNumber)
                            .child(MainActivity.locationDBChildPath).child(MainActivity.latitudePath)
                            .setValue(myLocation!!.latitude)
                        dbReference!!.child(LoginActivity.userDBChildPath).child(myPhoneNumber)
                            .child(MainActivity.locationDBChildPath).child(MainActivity.longitudePath)
                            .setValue(myLocation!!.longitude)
                        dbReference!!.child(LoginActivity.userDBChildPath).child(myPhoneNumber)
                            .child(MainActivity.locationDBChildPath).child(MainActivity.lastOnlinePath)
                            .setValue(dateFormat.format(date).toString())
                    }

                })

        return START_NOT_STICKY
    }

    inner class MyLocationListener: LocationListener {

        init {
            myLocation = Location("me")

            myLocation!!.longitude = 0.0
            myLocation!!.latitude = 0.0
        }

        override fun onLocationChanged(p0: Location?) {
            myLocation = p0
        }

        override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {}

        override fun onProviderEnabled(p0: String?) {}

        override fun onProviderDisabled(p0: String?) {}

    }

}