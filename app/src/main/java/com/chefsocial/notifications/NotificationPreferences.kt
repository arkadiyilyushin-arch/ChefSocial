package com.chefsocial.notifications

import android.content.Context
import com.chefsocial.util.areNotificationsEnabled
import com.chefsocial.util.isNotifyCommentsEnabled
import com.chefsocial.util.isNotifyFollowersEnabled
import com.chefsocial.util.isNotifyLikesEnabled
import com.chefsocial.util.isNotifyMessagesEnabled
import com.chefsocial.util.isNotifyRecipesEnabled

enum class NotificationKind {
    FOLLOWER,
    COMMENT,
    LIKE,
    NEWS,
    MESSAGE,
    RECIPE,
}

fun shouldShowNotification(context: Context, kind: NotificationKind): Boolean {
    if (!areNotificationsEnabled(context)) return false
    return when (kind) {
        NotificationKind.FOLLOWER -> isNotifyFollowersEnabled(context)
        NotificationKind.COMMENT -> isNotifyCommentsEnabled(context)
        NotificationKind.LIKE -> isNotifyLikesEnabled(context)
        NotificationKind.NEWS -> isNotifyNewsEnabled(context)
        NotificationKind.MESSAGE -> isNotifyMessagesEnabled(context)
        NotificationKind.RECIPE -> isNotifyRecipesEnabled(context)
    }
}
