package com.example.noise

import android.content.Context
import android.content.SharedPreferences

object SessionManager {

    private const val PREF_NAME = "NOISE_Session"
    private const val KEY_PARENT_ID = "key_parent_id"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveSession(context: Context, parentId: String) {
        val editor = getPreferences(context).edit()
        editor.putString(KEY_PARENT_ID, parentId)
        editor.apply()
    }

    fun getSavedParentId(context: Context): String? {
        return getPreferences(context).getString(KEY_PARENT_ID, null)
    }

    fun clearSession(context: Context) {
        val editor = getPreferences(context).edit()
        editor.remove(KEY_PARENT_ID)
        editor.apply()
    }
}
