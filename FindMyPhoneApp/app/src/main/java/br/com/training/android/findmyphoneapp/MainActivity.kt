package br.com.training.android.findmyphoneapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val userData = UserData(this)

        userData.loadPhoneNumber()
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

}
