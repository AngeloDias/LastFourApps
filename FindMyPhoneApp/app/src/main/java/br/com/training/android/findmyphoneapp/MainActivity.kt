package br.com.training.android.findmyphoneapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.contact_ticket.view.*

class MainActivity : AppCompatActivity() {
    private var listOfContacts = ArrayList<UserContact>()
    private var contactsAdapter: ContactMainAdapter? = null
    private var dbReference: DatabaseReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        dbReference = FirebaseDatabase.getInstance().reference
        val userData = UserData(this)

        userData.loadPhoneNumber()

        contactsAdapter = ContactMainAdapter(applicationContext, listOfContacts)
        listViewMainContact.adapter = contactsAdapter
        listViewMainContact.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, id ->
            val userInfo = listOfContacts[i]

        }

    }

    override fun onResume() {
        super.onResume()
        refreshUsers()
    }

    private fun refreshUsers() {
        val userData = UserData(this)

        if(userData.loadPhoneNumber() == "") {
            return
        }

        dbReference!!.child(LoginActivity.userDBChildPath).child(userData.loadPhoneNumber())
            .child(LoginActivity.findingDBChildPath).addValueEventListener(
                object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {}

                    override fun onDataChange(p0: DataSnapshot) {
                        val tableMap = p0.value as HashMap<String, Any>

                        listOfContacts.clear()

                        for(key in tableMap.values) {
                            listOfContacts.add(UserContact("no_name", key as String))
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

}
