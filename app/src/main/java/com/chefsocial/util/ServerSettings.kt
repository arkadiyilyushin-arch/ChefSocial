package com.chefsocial.util

import android.content.Context

private const val PREFS = "chef_social_prefs"
private const val KEY_SERVER_URL = "server_url"

fun getServerUrl(context: Context): String {
    val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    return prefs.getString(KEY_SERVER_URL, DEFAULT_SERVER_URL) ?: DEFAULT_SERVER_URL
}

fun setServerUrl(context: Context, url: String) {
    context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        .edit()
        .putString(KEY_SERVER_URL, url)
        .apply()
}

const val DEFAULT_SERVER_URL = "http://10.0.2.2:8080/"
