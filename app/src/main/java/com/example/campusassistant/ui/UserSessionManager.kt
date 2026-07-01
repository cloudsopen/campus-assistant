package com.example.campusassistant.ui

import android.content.Context
import kotlin.math.abs

object UserSessionManager {
    private const val PREFS_NAME = "campus_user_session"
    private const val KEY_LOGGED_IN = "logged_in"
    private const val KEY_USERNAME = "username"
    private const val KEY_BALANCE = "balance"
    private const val KEY_ORDERS = "orders"
    private const val KEY_USER_ID = "user_id"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    @JvmStatic
    fun isLoggedIn(context: Context): Boolean = prefs(context).getBoolean(KEY_LOGGED_IN, false)

    @JvmStatic
    fun login(context: Context, user: com.example.campusassistant.data.User) {
        val cleanName = user.username.trim().ifEmpty { "校园用户" }
        val seed = abs(cleanName.hashCode())
        val balance = ((seed % 5000) / 10.0) + 20
        val orders = seed % 5

        prefs(context).edit()
            .putBoolean(KEY_LOGGED_IN, true)
            .putLong(KEY_USER_ID, user.id)
            .putString(KEY_USERNAME, cleanName)
            .putString(KEY_BALANCE, String.format("%.2f", balance))
            .putInt(KEY_ORDERS, orders)
            .apply()
    }

    @JvmStatic
    fun getUserId(context: Context): Long = prefs(context).getLong(KEY_USER_ID, -1L)

    @JvmStatic
    fun logout(context: Context) {
        prefs(context).edit().clear().apply()
    }

    @JvmStatic
    fun getDisplayName(context: Context): String =
        prefs(context).getString(KEY_USERNAME, "点击登录") ?: "点击登录"

    @JvmStatic
    fun getAvatarLabel(context: Context): String {
        val name = getDisplayName(context)
        return if (name.isNotEmpty()) name.substring(0, 1) else "A"
    }

    @JvmStatic
    fun getBalanceText(context: Context): String {
        val balance = prefs(context).getString(KEY_BALANCE, "0.00") ?: "0.00"
        return "¥$balance"
    }

    @JvmStatic
    fun getActiveOrdersText(context: Context): String =
        prefs(context).getInt(KEY_ORDERS, 0).toString()
}
