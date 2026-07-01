package com.example.campusassistant.ui

import android.content.Context
import kotlin.math.abs

object UserSessionManager {
    private const val PREFS_NAME = "campus_user_session"
    private const val KEY_LOGGED_IN = "logged_in"
    private const val KEY_USERNAME = "username"
    private const val KEY_TAG = "tag"
    private const val KEY_BADGE = "badge"
    private const val KEY_BALANCE = "balance"
    private const val KEY_ORDERS = "orders"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    @JvmStatic
    fun isLoggedIn(context: Context): Boolean = prefs(context).getBoolean(KEY_LOGGED_IN, false)

    @JvmStatic
    fun login(context: Context, username: String, tag: String?) {
        val cleanName = username.trim().ifEmpty { "校园用户" }
        val cleanTag = tag?.trim().takeUnless { it.isNullOrEmpty() } ?: "在校用户"
        val seed = abs(cleanName.hashCode())
        val balance = ((seed % 5000) / 10.0) + 20
        val orders = seed % 5
        val badge = if (seed % 2 == 0) "♂" else "♀"

        prefs(context).edit()
            .putBoolean(KEY_LOGGED_IN, true)
            .putString(KEY_USERNAME, cleanName)
            .putString(KEY_TAG, cleanTag)
            .putString(KEY_BADGE, badge)
            .putString(KEY_BALANCE, String.format("%.2f", balance))
            .putInt(KEY_ORDERS, orders)
            .apply()
    }

    @JvmStatic
    fun logout(context: Context) {
        prefs(context).edit().clear().apply()
    }

    @JvmStatic
    fun getDisplayName(context: Context): String =
        prefs(context).getString(KEY_USERNAME, "点击登录") ?: "点击登录"

    @JvmStatic
    fun getIdentityTag(context: Context): String =
        prefs(context).getString(KEY_TAG, "未登录用户") ?: "未登录用户"

    @JvmStatic
    fun getBadgeSymbol(context: Context): String =
        prefs(context).getString(KEY_BADGE, "•") ?: "•"

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
