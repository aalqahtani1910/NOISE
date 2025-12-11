package com.example.noise

import android.content.Context
import android.content.SharedPreferences

object SessionManager {

    private const val PREF_NAME = "NOISE_Session"
    private const val KEY_USER_ID = "key_user_id"
    private const val KEY_USER_TYPE = "key_user_type"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveSession(context: Context, userId: String, userType: String) {
        val editor = getPreferences(context).edit()
        editor.putString(KEY_USER_ID, userId)
        editor.putString(KEY_USER_TYPE, userType)
        editor.apply()
    }

    fun getSavedSession(context: Context): Pair<String?, String?> {
        val prefs = getPreferences(context)
        val userId = prefs.getString(KEY_USER_ID, null)
        val userType = prefs.getString(KEY_USER_TYPE, null)
        return Pair(userId, userType)
    }

    fun clearSession(context: Context) {
        val editor = getPreferences(context).edit()
        editor.remove(KEY_USER_ID)
        editor.remove(KEY_USER_TYPE)
        editor.apply()
    }
}
