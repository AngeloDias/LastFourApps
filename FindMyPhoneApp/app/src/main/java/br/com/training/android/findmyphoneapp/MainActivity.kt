package br.com.training.android.findmyphoneapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import br.com.training.android.findmyphoneapp.LoginActivity.Companion.requestingDBChildPath
import br.com.training.android.findmyphoneapp.LoginActivity.Companion.userDBChildPath
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.contact_ticket.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity() {
    private var listOfContacts = ArrayList<UserContact>()
    private var contactsAdapter: ContactMainAdapter? = null
    private var dbReference: DatabaseReference? = null
    private val _contactRequestCode = 178
    private val _locationRequestCode = 112
    private var hashListOfContacts = HashMap<String, String>()
    private var myLocation: Location? = null
    private var isAccessLocation = false
    private val locationDBChildPath = "location"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        dbReference = FirebaseDatabase.getInstance().reference
        val userData = UserData(this)

        userData.isFirstLoad()

        contactsAdapter = ContactMainAdapter(applicationContext, listOfContacts)
        listViewMainContact.adapter = contactsAdapter
        listViewMainContact.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, id ->
            val userInfo = listOfContacts[i]

        }
    }

    override fun onResume() {
        super.onResume()
        refreshUsers()

        if(isAccessLocation) {
            return
        }

        checkContactPermission()
        checkLocationPermission()
    }

    private fun refreshUsers() {
        val userData = UserData(this)

        if(userData.loadPhoneNumber() == "") {
            return
        }

        dbReference!!.child(userDBChildPath).child(userData.loadPhoneNumber())
            .child(LoginActivity.findingDBChildPath).addValueEventListener(
                object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {}

                    override fun onDataChange(p0: DataSnapshot) {
                        val tableMap = p0.value as HashMap<String, Any>

                        listOfContacts.clear()

                        for(key in tableMap.values) {
                            listOfContacts.add(UserContact(hashListOfContacts[key]!!, key as String))
                        }

                        contactsAdapter!!.notifyDataSetChanged()
                    }
                }
            )
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater

        inflater.inflate(R.menu.main_menu, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menuAddTracker -> {
                val intent = Intent(this, MyTrackersPeopleActivity::class.java)

                startActivity(intent)
            }

            R.id.menuHelp -> {}

            else -> {
                return super.onOptionsItemSelected(item)
            }
        }

        return true
    }

    private fun checkContactPermission() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS), _contactRequestCode)
        }

        loadContact()
    }

    private fun loadContact(){
        hashListOfContacts.clear()

        val contactsCursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null, null, null, null)
        contactsCursor!!.moveToFirst()

        do {
            val name = contactsCursor.getString(contactsCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            val phoneNumber = contactsCursor.getString(contactsCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))

            hashListOfContacts[UserData.formatPhoneNumber(phoneNumber)] = name

        } while (contactsCursor.moveToNext())

        contactsCursor.close()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode) {
            _contactRequestCode -> {
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadContact()
                } else {
                    Toast.makeText(applicationContext, "Cannot access to contact", Toast.LENGTH_LONG).show()
                }
            }

            _locationRequestCode -> {
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getUserLocation()
                } else {
                    Toast.makeText(applicationContext, "Cannot access location", Toast.LENGTH_LONG).show()
                }
            }

            else -> {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }

    }

    inner class ContactMainAdapter(var context: Context, var contacts: ArrayList<UserContact>): BaseAdapter() {

        override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
//            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val userContact = contacts[p0]
            val contactTicketView = layoutInflater.inflate(R.layout.contact_ticket, null)

            contactTicketView.textViewName.text = userContact.name
            contactTicketView.textViewPhone.text = userContact.phoneNumber

            return contactTicketView
        }

        override fun getItem(p0: Int): Any {
            return contacts[p0]
        }

        override fun getItemId(p0: Int): Long {
            return p0.toLong()
        }

        override fun getCount(): Int {
            return contacts.size
        }
    }

    private fun checkLocationPermission() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), _locationRequestCode)
        }

        getUserLocation()
    }

    private fun getUserLocation() {
        val myUserLocation = MyLocationListener()
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val userData = UserData(applicationContext)
        val myPhoneNumber = userData.loadPhoneNumber()
        val dateFormat = SimpleDateFormat("yyyy/MMM/dd HH:MM:ss")
        val date = Date()

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3, 3f, myUserLocation)

        dbReference!!.child(userDBChildPath).child(myPhoneNumber)
            .child(requestingDBChildPath).addValueEventListener(
                object: ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {}

                    override fun onDataChange(p0: DataSnapshot) {
                        dbReference!!.child(userDBChildPath).child(myPhoneNumber)
                            .child(locationDBChildPath).child("latitude")
                            .setValue(myLocation!!.latitude)
                        dbReference!!.child(userDBChildPath).child(myPhoneNumber)
                            .child(locationDBChildPath).child("longitude")
                            .setValue(myLocation!!.longitude)
                        dbReference!!.child(userDBChildPath).child(myPhoneNumber)
                            .child(locationDBChildPath).child("lastOnline")
                            .setValue(dateFormat.format(date).toString())
                    }

                })
    }

    inner class MyLocationListener: LocationListener {

        init {
            isAccessLocation = true
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
