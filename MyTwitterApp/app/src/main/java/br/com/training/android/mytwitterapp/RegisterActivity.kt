package br.com.training.android.mytwitterapp

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import br.com.training.android.mytwitterapp.Operations.Companion.convertStreamToString
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_register.*
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*

class RegisterActivity : AppCompatActivity() {
    private var mAuth: FirebaseAuth? = null
    private val _readImage: Int = 253
    private val _pickImageCode = 156
    private val _firebaseStorageFolderPath = "gs://mytwitterapp-fcb88.appspot.com"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        mAuth = FirebaseAuth.getInstance()

        signInAnonymously()

        btnRegister.setOnClickListener {
            it.isEnabled = false
            saveImageInFirebase()
        }
        imgViewPerson.setOnClickListener {}
    }

    private fun signInAnonymously() {
        mAuth!!.signInAnonymously().addOnCompleteListener(this) { task ->
                Log.d("LoginInfo",task.isSuccessful.toString())
        }
    }

    private fun checkPermission() {
        if(Build.VERSION.SDK_INT >= 23) {
            if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), _readImage)

                return
            }
        }

        loadImage()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode) {
            _readImage ->
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadImage()
                } else {
                    Toast.makeText(this, "Can't access your images", Toast.LENGTH_LONG).show()
                }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun loadImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

        startActivityForResult(intent, _pickImageCode)
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

            imgViewPerson.setImageBitmap(BitmapFactory.decodeFile(picturePath))
        }
    }

    private fun saveImageInFirebase() {
        val currentUser = mAuth!!.currentUser
        val storage = FirebaseStorage.getInstance()
        val storageReference = storage.getReferenceFromUrl(_firebaseStorageFolderPath)
        val dateFormat = SimpleDateFormat("ddMMyyHHmmss")
        val dataObj = Date()
        val emailStr = currentUser!!.email!!.toString().split("@")[0]
        val imgPath = "$emailStr.${dateFormat.format(dataObj)}.jpg"
        val imgRef = storageReference.child("images/$imgPath")

        imgViewPerson.isDrawingCacheEnabled = true
        imgViewPerson.buildDrawingCache()

        val drawable = imgViewPerson.drawable as BitmapDrawable
        val bitmap = drawable.bitmap
        val byteAOStream = ByteArrayOutputStream()

        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteAOStream)

        val data = byteAOStream.toByteArray()
        val uploadTask = imgRef.putBytes(data)

        uploadTask.addOnFailureListener {
            Toast.makeText(this, "Fail to upload image", Toast.LENGTH_LONG).show()
        }.addOnSuccessListener {taskSnapshot ->
            var downloadUrl = URLEncoder.encode(taskSnapshot.storage.downloadUrl.toString(), "utf-8")
            val name = URLEncoder.encode(editTextName.text.toString(), "utf-8")
            val url = "http://"

            MyAsyncTask().execute(url)
        }

    }

    inner class MyAsyncTask() : AsyncTask<String, String, String>() {

        override fun onProgressUpdate(vararg values: String?) {

            try {
                val json = JSONObject(values[0])

                Toast.makeText(applicationContext, json.getString("msg"), Toast.LENGTH_LONG).show()

                if(json.getString("msg") == "user is added") {
                    finish()
                } else {
                    btnRegister.isEnabled = true
                }

            } catch (ex: Exception) {
                ex.printStackTrace()
            }

        }

        override fun doInBackground(vararg p0: String?): String {
            try {
                val url = URL(p0[0])
                val urlConnect = url.openConnection() as HttpURLConnection

                urlConnect.connectTimeout = 7000

                var streamInString = convertStreamToString(urlConnect.inputStream)

                publishProgress(streamInString)

            } catch (e:Exception) {}

            return "Success"
        }

    }
}
