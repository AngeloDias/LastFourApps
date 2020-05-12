package br.com.training.android.findmyphoneapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.com.training.android.findmyphoneapp.MainActivity.Companion.locationDBChildPath
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private var dbReference: DatabaseReference? = null
    private var sydney = LatLng(-34.0, 151.0)
    private var lastOnline = "not defined"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        val bundle: Bundle = intent.extras!!
        val phoneNumber = bundle.getString(MainActivity.phoneExtraToActivity)
        dbReference = FirebaseDatabase.getInstance().reference

        dbReference!!.child(LoginActivity.userDBChildPath).child(phoneNumber!!)
            .child(locationDBChildPath).addValueEventListener(
                object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {}

                    override fun onDataChange(p0: DataSnapshot) {
                        try {
                            val hashData = p0.value as HashMap<String, Any>
                            val latitude = hashData[MainActivity.latitudePath].toString()
                            val longitude = hashData[MainActivity.longitudePath].toString()
                            lastOnline = hashData[MainActivity.lastOnlinePath].toString()
                            sydney = LatLng(latitude.toDouble(), longitude.toDouble())

                            loadMap()
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                        }

                    }

                })
    }

    private fun loadMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 15f))
    }
}
