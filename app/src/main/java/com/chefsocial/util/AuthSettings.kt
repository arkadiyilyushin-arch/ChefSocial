package com.chefsocial.util

import android.content.Context

private const val PREFS = "chef_social_prefs"
private const val KEY_AUTH_EMAIL = "auth_email"
private const val KEY_AUTH_PASSWORD = "auth_password"
private const val KEY_LOGGED_IN = "logged_in"

fun isLoggedIn(context: Context): Boolean =
    context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        .getBoolean(KEY_LOGGED_IN, false)

fun setLoggedIn(context: Context, loggedIn: Boolean) {
    context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        .edit()
        .putBoolean(KEY_LOGGED_IN, loggedIn)
        .apply()
}

fun getStoredAuthEmail(context: Context): String =
    context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        .getString(KEY_AUTH_EMAIL, "")
        .orEmpty()

fun saveAuthCredentials(context: Context, email: String, password: String) {
    context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        .edit()
        .putString(KEY_AUTH_EMAIL, email.trim())
        .putString(KEY_AUTH_PASSWORD, password)
        .putBoolean(KEY_LOGGED_IN, true)
        .apply()
}

fun validateLogin(context: Context, email: String, password: String): Boolean {
    val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    val storedEmail = prefs.getString(KEY_AUTH_EMAIL, "").orEmpty()
    val storedPassword = prefs.getString(KEY_AUTH_PASSWORD, "").orEmpty()
    return storedEmail.isNotBlank() &&
        email.trim().equals(storedEmail, ignoreCase = true) &&
        password == storedPassword
}

fun hasRegisteredAccount(context: Context): Boolean =
    getStoredAuthEmail(context).isNotBlank()

const val ADMIN_EMAIL = "admin@chefly.app"

fun isAdminUser(context: Context): Boolean =
    getStoredAuthEmail(context).equals(ADMIN_EMAIL, ignoreCase = true)
