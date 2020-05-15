package br.com.training.android.mytwitterapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences

class SaveSettings(private val ctx: Context) {
    private var sharedPref: SharedPreferences
    private val _sharedPrefTag = "myRef"
    private val _userIDTag = "userID"

    init {
        sharedPref = ctx.getSharedPreferences(_sharedPrefTag, Context.MODE_PRIVATE)
    }

    companion object {
        var userID = ""
    }

    fun saveSettings(userID: String) {
        val editor = sharedPref.edit()

        editor.putString(_userIDTag, userID)
        editor.apply()
        loadSettings()
    }

    fun loadSettings() {
        userID = sharedPref.getString(_userIDTag, "")!!

        if(userID == "") {
            val intent = Intent(ctx, LoginActivity::class.java)

            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            ctx.startActivity(intent)
        }
    }

}