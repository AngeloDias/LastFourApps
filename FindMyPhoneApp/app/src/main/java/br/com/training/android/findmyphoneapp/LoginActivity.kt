package br.com.training.android.findmyphoneapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_login.*
import java.text.SimpleDateFormat
import java.util.*

class LoginActivity : AppCompatActivity() {
    private var mAuth: FirebaseAuth? = null

    companion object {
        const val userDBChildPath = "users"
        const val requestingDBChildPath = "request"
        const val findingDBChildPath = "finders"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mAuth = FirebaseAuth.getInstance()

        signInAnonymously()

        btnRegister.setOnClickListener {
            val userData = UserData(applicationContext)
            val dateFormat = SimpleDateFormat("yyyy/MMM/dd HH:MM:ss")
            val date = Date()
            val mDBReference = FirebaseDatabase.getInstance().reference

            userData.savePhoneNumber(editTextPhone.text.toString())

            mDBReference.child(userDBChildPath).child(editTextPhone.text.toString())
                .child(requestingDBChildPath).setValue(dateFormat.format(date).toString())
            mDBReference.child(userDBChildPath).child(editTextPhone.text.toString())
                .child(findingDBChildPath).setValue(dateFormat.format(date).toString())
            finish()
        }

    }

    private fun signInAnonymously() {
        mAuth!!.signInAnonymously().addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                Toast.makeText(baseContext, "Authentication successful.", Toast.LENGTH_SHORT).show()

            } else {
                // If sign in fails, display a message to the user.
                Toast.makeText(baseContext, "Authentication failed.",
                    Toast.LENGTH_SHORT).show()
            }

        }
    }

}
