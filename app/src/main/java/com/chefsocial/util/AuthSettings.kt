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

fun updatePassword(context: Context, currentPassword: String, newPassword: String): Boolean {
    val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    val storedPassword = prefs.getString(KEY_AUTH_PASSWORD, "").orEmpty()
    if (storedPassword != currentPassword || newPassword.length < 6) return false
    prefs.edit().putString(KEY_AUTH_PASSWORD, newPassword).apply()
    return true
}

fun clearAuthCredentials(context: Context) {
    context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        .edit()
        .remove(KEY_AUTH_EMAIL)
        .remove(KEY_AUTH_PASSWORD)
        .putBoolean(KEY_LOGGED_IN, false)
        .apply()
}

fun canResetPassword(context: Context, email: String): Boolean {
    val stored = getStoredAuthEmail(context)
    return stored.isNotBlank() && email.trim().equals(stored, ignoreCase = true)
}

fun resetPassword(context: Context, email: String, newPassword: String): Boolean {
    if (!canResetPassword(context, email) || newPassword.length < 6) return false
    context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        .edit()
        .putString(KEY_AUTH_PASSWORD, newPassword)
        .apply()
    return true
}

fun hasRegisteredAccount(context: Context): Boolean =
    getStoredAuthEmail(context).isNotBlank()

const val ADMIN_EMAIL = "admin@chefly.app"

fun isAdminUser(context: Context): Boolean =
    getStoredAuthEmail(context).equals(ADMIN_EMAIL, ignoreCase = true)
