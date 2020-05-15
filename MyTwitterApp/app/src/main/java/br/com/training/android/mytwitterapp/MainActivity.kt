package br.com.training.android.mytwitterapp

import android.app.Activity
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.add_ticket.view.*
import kotlinx.android.synthetic.main.tweets_ticket.view.*
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    private var listTweets = ArrayList<Ticket>()
    private var adapter: MyTweetAdapter? = null
    private var myRef: DatabaseReference? = null
    private val _pickImageCode = 156
    private val _firebaseStorageFolderPath = "gs://mytwitterapp-fcb88.appspot.com"
    private var downloadURL: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val saveSettings = SaveSettings(applicationContext)
        adapter = MyTweetAdapter(applicationContext, listTweets)

        saveSettings.loadSettings()
        listViewTweets.adapter = adapter
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)

        val searchView = menu!!.findItem(R.id.app_bar_search).actionView as SearchView
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager

        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                Toast.makeText(applicationContext, p0!!, Toast.LENGTH_LONG).show()

                return false
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                return false
            }
        })

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.homePage -> {

            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun loadImage() {
        startActivityForResult(Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI), _pickImageCode)
    }

    inner class MyTweetAdapter(var context: Context, var listTicketsAdapter: ArrayList<Ticket>) :
        BaseAdapter() {

        override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
            val myTweet = listTicketsAdapter[p0]

            if(myTweet.tweetPersonUID == "add") {
                val myView = layoutInflater.inflate(R.layout.add_ticket, null)

                myView.imgViewAttach.setOnClickListener {
                    loadImage()
                }

                myView.imgViewPost.setOnClickListener {
                    //upload server
                    myView.editTxtPost.setText("")
                }

                return myView
            } else if(myTweet.tweetPersonUID == "loading"){

                return layoutInflater.inflate(R.layout.loading_ticket,null)
            }else{
                val myView = layoutInflater.inflate(R.layout.tweets_ticket,null)
                myView.txtViewTweet.text = myTweet.tweetText

//                Picasso.with(context).load(myTweet.tweetImageURL).into(myView.tweetPicture)

                myRef!!.child("users").child(myTweet.tweetPersonUID)
                    .addValueEventListener(object : ValueEventListener {

                        override fun onDataChange(dataSnapshot: DataSnapshot) {}

                        override fun onCancelled(p0: DatabaseError) {}

                    })

                return myView
            }
        }

        override fun getItem(p0: Int): Any {
            return listTicketsAdapter[p0]
        }

        override fun getItemId(p0: Int): Long {
            return p0.toLong()
        }

        override fun getCount(): Int {
            return listTicketsAdapter.size
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == _pickImageCode && resultCode == Activity.RESULT_OK && data != null) {
            val selectedImage = data.data
            val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
            val cursor = contentResolver.query(selectedImage!!, filePathColumn, null, null, null)

            cursor!!.moveToFirst()

            val columnIndex = cursor.getColumnIndex(filePathColumn[0])
            val picturePath = cursor.getString(columnIndex)

            cursor.close()

            uploadImage(BitmapFactory.decodeFile(picturePath))
        }

    }

    private fun uploadImage(bitmap: Bitmap) {
        listTweets.add(0, Ticket("0", "Hello", "URL", "loading"))
        adapter!!.notifyDataSetChanged()

        val storage = FirebaseStorage.getInstance()
        val storageReference = storage.getReferenceFromUrl(_firebaseStorageFolderPath)
        val dateFormat = SimpleDateFormat("ddMMyyHHmmss")
        val dataObj = Date()
        val imgPath = "${SaveSettings.userID}.${dateFormat.format(dataObj)}.jpg"
        val imgRef = storageReference.child("imagesPost/$imgPath")
        val byteAOStream = ByteArrayOutputStream()

        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteAOStream)

        val data = byteAOStream.toByteArray()
        val uploadTask = imgRef.putBytes(data)

        uploadTask.addOnFailureListener {
            Toast.makeText(this, "Fail to upload image", Toast.LENGTH_LONG).show()
        }.addOnSuccessListener {taskSnapshot ->
            downloadURL = taskSnapshot.storage.downloadUrl.toString()

            listTweets.removeAt(0)
            adapter!!.notifyDataSetChanged()

        }
    }

    private fun loadPost() {
        myRef!!.child("posts").addValueEventListener(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    val tData = dataSnapshot.value as HashMap<String, Any>

                    for(key in tData.keys) {
                        val post = tData[key] as HashMap<String, Any>

                        listTweets.add(Ticket(key, post["text"] as String, post["postImage"] as String,
                            post["userUID"] as String))
                    }

                    adapter!!.notifyDataSetChanged()

                } catch (exc: Exception){}
            }

        })
    }

}
