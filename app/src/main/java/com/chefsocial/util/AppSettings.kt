package com.chefsocial.util

import android.content.Context

enum class AppLanguage(val code: String) {
    RU("ru"),
    EN("en"),
    ;

    companion object {
        fun fromCode(code: String): AppLanguage =
            entries.find { it.code == code } ?: RU
    }
}

private const val PREFS = "chef_social_prefs"
private const val KEY_LANGUAGE = "language"
private const val KEY_ONBOARDING = "onboarding_done"
private const val KEY_LAST_SYNC = "last_sync_at"
private const val KEY_LAST_RECIPE_COUNT = "last_recipe_count"
private const val KEY_LAST_COMMENT_COUNT = "last_comment_count"

fun getAppLanguage(context: Context): AppLanguage {
    val code = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        .getString(KEY_LANGUAGE, AppLanguage.RU.code) ?: AppLanguage.RU.code
    return AppLanguage.fromCode(code)
}

fun setAppLanguage(context: Context, language: AppLanguage) {
    context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        .edit()
        .putString(KEY_LANGUAGE, language.code)
        .apply()
}

fun isOnboardingCompleted(context: Context): Boolean =
    context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        .getBoolean(KEY_ONBOARDING, false)

fun setOnboardingCompleted(context: Context, completed: Boolean) {
    context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        .edit()
        .putBoolean(KEY_ONBOARDING, completed)
        .apply()
}

fun getLastSyncStats(context: Context): Pair<Long, Pair<Int, Int>> {
    val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    return prefs.getLong(KEY_LAST_SYNC, 0L) to
        (prefs.getInt(KEY_LAST_RECIPE_COUNT, 0) to prefs.getInt(KEY_LAST_COMMENT_COUNT, 0))
}

fun setLastSyncStats(context: Context, recipeCount: Int, commentCount: Int) {
    context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        .edit()
        .putLong(KEY_LAST_SYNC, System.currentTimeMillis())
        .putInt(KEY_LAST_RECIPE_COUNT, recipeCount)
        .putInt(KEY_LAST_COMMENT_COUNT, commentCount)
        .apply()
}
