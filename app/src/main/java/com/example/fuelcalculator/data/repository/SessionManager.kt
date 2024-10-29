package com.example.fuelcalculator.data.repository

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "AuthPrefs"
        private const val KEY_REMEMBER_ME = "remember_me"
        private const val KEY_USERNAME = "username"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }

    fun createLoginSession(username: String, rememberMe: Boolean) {
        prefs.edit().apply{
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_USERNAME, username)
            putBoolean(KEY_REMEMBER_ME, rememberMe)
            apply()
        }
    }

    fun getSavedUsername():String?{
        return prefs.getString(KEY_USERNAME, null)
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }


    fun clearSession(){
        prefs.edit().apply{
            clear()
            apply()
        }
    }

    fun isRememberMeEnabled(): Boolean = prefs.getBoolean(KEY_REMEMBER_ME, false)

}