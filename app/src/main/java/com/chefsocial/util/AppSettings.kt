package com.chefsocial.util

import android.content.Context
import com.chefsocial.model.AppThemeMode
import com.chefsocial.model.FeedSortMode
import com.chefsocial.model.MessagePrivacy
import com.chefsocial.model.ProfileVisibility
import com.chefsocial.model.RecipeCategory

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
private const val KEY_THEME = "theme_mode"
private const val KEY_AUTO_SYNC = "auto_sync"
private const val KEY_FEED_SORT = "feed_sort"
private const val KEY_FEED_CATEGORY = "feed_category"
private const val KEY_PROFILE_VISIBILITY = "profile_visibility"
private const val KEY_MESSAGE_PRIVACY = "message_privacy"
private const val KEY_SHOW_BOOKMARKS = "show_bookmarks"
private const val KEY_NOTIFY_ENABLED = "notify_enabled"
private const val KEY_NOTIFY_FOLLOWERS = "notify_followers"
private const val KEY_NOTIFY_COMMENTS = "notify_comments"
private const val KEY_NOTIFY_LIKES = "notify_likes"
private const val KEY_NOTIFY_NEWS = "notify_news"
private const val KEY_NOTIFY_MESSAGES = "notify_messages"
private const val KEY_NOTIFY_RECIPES = "notify_recipes"

private fun prefs(context: Context) =
    context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

fun getAppLanguage(context: Context): AppLanguage {
    val code = prefs(context).getString(KEY_LANGUAGE, AppLanguage.RU.code) ?: AppLanguage.RU.code
    return AppLanguage.fromCode(code)
}

fun setAppLanguage(context: Context, language: AppLanguage) {
    prefs(context).edit().putString(KEY_LANGUAGE, language.code).apply()
}

fun isOnboardingCompleted(context: Context): Boolean =
    prefs(context).getBoolean(KEY_ONBOARDING, false)

fun setOnboardingCompleted(context: Context, completed: Boolean) {
    prefs(context).edit().putBoolean(KEY_ONBOARDING, completed).apply()
}

fun getLastSyncStats(context: Context): Pair<Long, Pair<Int, Int>> {
    val p = prefs(context)
    return p.getLong(KEY_LAST_SYNC, 0L) to
        (p.getInt(KEY_LAST_RECIPE_COUNT, 0) to p.getInt(KEY_LAST_COMMENT_COUNT, 0))
}

fun setLastSyncStats(context: Context, recipeCount: Int, commentCount: Int) {
    prefs(context).edit()
        .putLong(KEY_LAST_SYNC, System.currentTimeMillis())
        .putInt(KEY_LAST_RECIPE_COUNT, recipeCount)
        .putInt(KEY_LAST_COMMENT_COUNT, commentCount)
        .apply()
}

fun getAppThemeMode(context: Context): AppThemeMode =
    AppThemeMode.fromId(prefs(context).getString(KEY_THEME, AppThemeMode.SYSTEM.id) ?: AppThemeMode.SYSTEM.id)

fun setAppThemeMode(context: Context, mode: AppThemeMode) {
    prefs(context).edit().putString(KEY_THEME, mode.id).apply()
}

fun isAutoSyncEnabled(context: Context): Boolean =
    prefs(context).getBoolean(KEY_AUTO_SYNC, true)

fun setAutoSyncEnabled(context: Context, enabled: Boolean) {
    prefs(context).edit().putBoolean(KEY_AUTO_SYNC, enabled).apply()
}

fun getFeedSortMode(context: Context): FeedSortMode =
    FeedSortMode.fromId(prefs(context).getString(KEY_FEED_SORT, FeedSortMode.NEWEST.id) ?: FeedSortMode.NEWEST.id)

fun setFeedSortMode(context: Context, mode: FeedSortMode) {
    prefs(context).edit().putString(KEY_FEED_SORT, mode.id).apply()
}

fun getDefaultFeedCategory(context: Context): RecipeCategory =
    RecipeCategory.fromId(prefs(context).getString(KEY_FEED_CATEGORY, RecipeCategory.ALL.id) ?: RecipeCategory.ALL.id)

fun setDefaultFeedCategory(context: Context, category: RecipeCategory) {
    prefs(context).edit().putString(KEY_FEED_CATEGORY, category.id).apply()
}

fun getProfileVisibility(context: Context): ProfileVisibility =
    ProfileVisibility.fromId(
        prefs(context).getString(KEY_PROFILE_VISIBILITY, ProfileVisibility.PUBLIC.id)
            ?: ProfileVisibility.PUBLIC.id,
    )

fun setProfileVisibility(context: Context, visibility: ProfileVisibility) {
    prefs(context).edit().putString(KEY_PROFILE_VISIBILITY, visibility.id).apply()
}

fun getMessagePrivacy(context: Context): MessagePrivacy =
    MessagePrivacy.fromId(
        prefs(context).getString(KEY_MESSAGE_PRIVACY, MessagePrivacy.EVERYONE.id)
            ?: MessagePrivacy.EVERYONE.id,
    )

fun setMessagePrivacy(context: Context, privacy: MessagePrivacy) {
    prefs(context).edit().putString(KEY_MESSAGE_PRIVACY, privacy.id).apply()
}

fun isShowBookmarksPublic(context: Context): Boolean =
    prefs(context).getBoolean(KEY_SHOW_BOOKMARKS, true)

fun setShowBookmarksPublic(context: Context, show: Boolean) {
    prefs(context).edit().putBoolean(KEY_SHOW_BOOKMARKS, show).apply()
}

fun areNotificationsEnabled(context: Context): Boolean =
    prefs(context).getBoolean(KEY_NOTIFY_ENABLED, true)

fun setNotificationsEnabled(context: Context, enabled: Boolean) {
    prefs(context).edit().putBoolean(KEY_NOTIFY_ENABLED, enabled).apply()
}

fun isNotifyFollowersEnabled(context: Context): Boolean =
    prefs(context).getBoolean(KEY_NOTIFY_FOLLOWERS, true)

fun setNotifyFollowersEnabled(context: Context, enabled: Boolean) {
    prefs(context).edit().putBoolean(KEY_NOTIFY_FOLLOWERS, enabled).apply()
}

fun isNotifyCommentsEnabled(context: Context): Boolean =
    prefs(context).getBoolean(KEY_NOTIFY_COMMENTS, true)

fun setNotifyCommentsEnabled(context: Context, enabled: Boolean) {
    prefs(context).edit().putBoolean(KEY_NOTIFY_COMMENTS, enabled).apply()
}

fun isNotifyLikesEnabled(context: Context): Boolean =
    prefs(context).getBoolean(KEY_NOTIFY_LIKES, true)

fun setNotifyLikesEnabled(context: Context, enabled: Boolean) {
    prefs(context).edit().putBoolean(KEY_NOTIFY_LIKES, enabled).apply()
}

fun isNotifyNewsEnabled(context: Context): Boolean =
    prefs(context).getBoolean(KEY_NOTIFY_NEWS, true)

fun setNotifyNewsEnabled(context: Context, enabled: Boolean) {
    prefs(context).edit().putBoolean(KEY_NOTIFY_NEWS, enabled).apply()
}

fun isNotifyMessagesEnabled(context: Context): Boolean =
    prefs(context).getBoolean(KEY_NOTIFY_MESSAGES, true)

fun setNotifyMessagesEnabled(context: Context, enabled: Boolean) {
    prefs(context).edit().putBoolean(KEY_NOTIFY_MESSAGES, enabled).apply()
}

fun isNotifyRecipesEnabled(context: Context): Boolean =
    prefs(context).getBoolean(KEY_NOTIFY_RECIPES, true)

fun setNotifyRecipesEnabled(context: Context, enabled: Boolean) {
    prefs(context).edit().putBoolean(KEY_NOTIFY_RECIPES, enabled).apply()
}

fun clearUserPreferences(context: Context) {
    val language = getAppLanguage(context)
    val theme = getAppThemeMode(context)
    prefs(context).edit().clear().apply()
    setAppLanguage(context, language)
    setAppThemeMode(context, theme)
}
