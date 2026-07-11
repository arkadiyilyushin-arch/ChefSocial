package com.chefsocial.util

import android.content.Context
import com.chefsocial.BuildConfig

private const val PREFS = "chef_social_prefs"
private const val KEY_SERVER_URL = "server_url"
private const val KEY_SERVER_API_TOKEN = "server_api_token"

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

fun getServerApiToken(context: Context): String =
    context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        .getString(KEY_SERVER_API_TOKEN, "")
        .orEmpty()

fun setServerApiToken(context: Context, token: String) {
    context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        .edit()
        .putString(KEY_SERVER_API_TOKEN, token.trim())
        .apply()
}

const val DEFAULT_SERVER_URL: String = BuildConfig.DEFAULT_SERVER_URL
