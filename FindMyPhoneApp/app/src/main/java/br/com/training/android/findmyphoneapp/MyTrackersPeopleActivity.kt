package br.com.training.android.findmyphoneapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.view.*
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_my_trackers_people.*
import kotlinx.android.synthetic.main.contact_ticket.view.*

class MyTrackersPeopleActivity : AppCompatActivity() {
    private var listOfContacts = ArrayList<UserContact>()
    private var contactsAdapter: ContactAdapter? = null
    private val _contactCode = 156
    private val _pickCode = 171
    private var userData: UserData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_trackers_people)

        userData = UserData(applicationContext)
        contactsAdapter = ContactAdapter(applicationContext, listOfContacts)
        listViewContact.adapter = contactsAdapter
        listViewContact.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, id ->
            val userInfo = listOfContacts[i]
            val mDatabaseRef = FirebaseDatabase.getInstance().reference

            UserData.myTrackers.remove(userInfo.phoneNumber)
            userData!!.saveContactInfo()
            refreshData()

            mDatabaseRef.child(LoginActivity.userDBChildPath).child(userInfo.phoneNumber)
                .child(LoginActivity.findingDBChildPath).child(userData!!.loadPhoneNumber())
                .removeValue()
        }

        userData!!.loadContactInfo()
        refreshData()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.tracker_menu, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.finishActivity -> {
                finish()
            }

            R.id.addContactMenu -> {
                checkPermission()
            }

            else -> {
                return super.onOptionsItemSelected(item)
            }
        }

        return true
    }

    private fun checkPermission() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            ActivityCompat.checkSelfPermission(applicationContext, android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.READ_CONTACTS), _contactCode)
        }

        pickContact()
    }

    private fun pickContact(){
        val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)

        startActivityForResult(intent, _pickCode)
    }

    private fun refreshData() {
        listOfContacts.clear()

        for((key, value) in UserData.myTrackers){
            listOfContacts.add(UserContact(value, key))
        }

        contactsAdapter!!.notifyDataSetChanged()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when(requestCode) {
            _pickCode -> {
                if(resultCode == Activity.RESULT_OK) {
                    val contactData = data!!.data
                    val contactsCursor = contentResolver.query(contactData!!, null, null, null, null, null)

                    if(contactsCursor!!.moveToFirst()) {
                        val id = contactsCursor.getString(contactsCursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
                        val hasPhoneNumber = contactsCursor.getString(contactsCursor.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER))

                        if(hasPhoneNumber == "1") {
                            val phonesCursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID}=$id",
                                null, null)

                            phonesCursor!!.moveToFirst()

                            val phoneNumber = phonesCursor.getString(phonesCursor.getColumnIndex("data1"))
                            val name = contactsCursor.getString(contactsCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                            val cleanPhoneNumber = UserData.formatPhoneNumber(phoneNumber)
                            UserData.myTrackers[cleanPhoneNumber] = name

                            refreshData()
                            phonesCursor.close()
                            userData!!.saveContactInfo()

                            val userData = UserData(applicationContext)
                            val mDatabaseRef = FirebaseDatabase.getInstance().reference

                            mDatabaseRef.child(LoginActivity.userDBChildPath).child(cleanPhoneNumber)
                                .child(LoginActivity.findingDBChildPath).child(userData.loadPhoneNumber())
                                .setValue(true)
                        }
                    }

                    contactsCursor.close()
                }
            }

            else -> {
                super.onActivityResult(requestCode, resultCode, data)
            }
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode) {
            _contactCode -> {
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickContact()
                } else {
                    Toast.makeText(applicationContext, "Cannot access to contact", Toast.LENGTH_LONG).show()
                }
            }

            else -> {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }

    }

    inner class ContactAdapter(var context: Context, var contacts: ArrayList<UserContact>): BaseAdapter() {

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

}
