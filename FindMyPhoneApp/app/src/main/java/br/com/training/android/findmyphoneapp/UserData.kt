package br.com.training.android.findmyphoneapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences

class UserData(var context: Context) {
    private val sharedPreferences: SharedPreferences

    companion object {
        const val editorPhoneNumberKey = "phoneNumber"
    }

    init {
        sharedPreferences = context.getSharedPreferences("userData", Context.MODE_PRIVATE)
    }

    fun savePhoneNumber(phoneNUmber: String) {
        val editor = sharedPreferences.edit()

        editor.putString(editorPhoneNumberKey, phoneNUmber)
        editor.apply()
    }

    fun loadPhoneNumber(): String {
        var phoneNumber = sharedPreferences.getString(editorPhoneNumberKey, "")

        if(phoneNumber == "") {
            val intent = Intent(context, LoginActivity::class.java)

            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } else if(phoneNumber == null) {
            phoneNumber = ""
        }

        return phoneNumber
    }
}