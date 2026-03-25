package com.example.exoticpet

import android.content.Context
import androidx.core.content.edit

object UserSession {

    private const val PREF_NAME = "auth_pref"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun getUserId(context: Context): Int =
        prefs(context).getInt("user_id", 0)

    fun getUsername(context: Context): String =
        prefs(context).getString("username", "") ?: ""

    fun isLoggedIn(context: Context): Boolean =
        prefs(context).getBoolean("is_logged_in", false)

    fun clear(context: Context) {
        prefs(context).edit { clear() }
    }
}