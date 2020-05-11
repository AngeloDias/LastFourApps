package br.com.training.android.findmyphoneapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences

class UserData(var context: Context) {
    private val sharedPreferences: SharedPreferences

    companion object {
        const val editorPhoneNumberKey = "phoneNumber"
        const val editorTrackersKey = "listOfTrackers"
        var myTrackers: MutableMap<String, String> = HashMap()

        fun formatPhoneNumber(phoneNumber: String): String {
            var onlyNumber = phoneNumber.replace("[^0-9]".toRegex(), "")

            if(phoneNumber[0] == '+') {
                onlyNumber = "+$phoneNumber"
            }

            return onlyNumber
        }
    }

    init {
        sharedPreferences = context.getSharedPreferences("userData", Context.MODE_PRIVATE)
    }

    fun saveContactInfo(){
        var listOfTrackers = ""
        val editor = sharedPreferences.edit()

        for ((key, value) in myTrackers) {
            if(listOfTrackers.isEmpty()) {
                listOfTrackers = "$key%$value"
            } else {
                listOfTrackers += "%$key%$value"
            }
        }

        if(listOfTrackers.isEmpty()) {
            listOfTrackers = ""
        }

        editor.putString(editorTrackersKey, listOfTrackers)
        editor.apply()
    }

    fun loadContactInfo() {
        myTrackers.clear()

        val listOfTrackers = sharedPreferences.getString(editorTrackersKey, "")

        if(listOfTrackers != null && listOfTrackers != "") {
            val usersInfo = listOfTrackers.split("%").toTypedArray()
            var i = 0

            while(i < usersInfo.size) {
                myTrackers[usersInfo[i]] = usersInfo[i+1]
                i += 2
            }
        }

    }

    fun savePhoneNumber(phoneNUmber: String) {
        val editor = sharedPreferences.edit()

        editor.putString(editorPhoneNumberKey, phoneNUmber)
        editor.apply()
    }

    fun isFirstLoad() {
        val phoneNumber = sharedPreferences.getString(editorPhoneNumberKey, "")

        if(phoneNumber == "") {
            val intent = Intent(context, LoginActivity::class.java)

            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    fun loadPhoneNumber(): String {
        return sharedPreferences.getString(editorPhoneNumberKey, "") ?: ""
    }
}