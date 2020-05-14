package br.com.training.android.mytwitterapp

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.com.training.android.mytwitterapp.Operations.Companion.convertStreamToString
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_register.*
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        btnLogin.setOnClickListener {}
        btnRegisterAccount.setOnClickListener {
            startActivity(Intent(applicationContext, RegisterActivity::class.java))
        }

    }

    inner class LoginAsyncTask() : AsyncTask<String, String, String>() {

        override fun onProgressUpdate(vararg values: String?) {

            try {
                val json = JSONObject(values[0])

                Toast.makeText(applicationContext, json.getString("msg"), Toast.LENGTH_LONG).show()

                if(json.getString("msg") == "user is added") {
                    val userInfo = JSONArray(json.getString("info"))
                    val userCredentials = userInfo.getJSONObject(0)
                    val userId = userCredentials.getString("user_id")

                    Toast.makeText(applicationContext, userCredentials.getString("first_name"), Toast.LENGTH_LONG).show()
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
