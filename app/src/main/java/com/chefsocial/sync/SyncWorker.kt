package com.chefsocial.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.chefsocial.data.AppDatabase
import com.chefsocial.data.ChefRepository
import com.chefsocial.data.remote.SyncRepository
import com.chefsocial.notifications.showNotification
import com.chefsocial.ui.localization.AppStrings
import com.chefsocial.util.getAppLanguage
import com.chefsocial.util.getServerApiToken
import com.chefsocial.util.getServerUrl
import com.chefsocial.util.setLastSyncStats

class SyncWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val db = AppDatabase.get(applicationContext)
        val repository = ChefRepository(db)
        val (beforeRecipes, beforeComments) = repository.counts()
        val syncRepo = SyncRepository(
            db = db,
            baseUrl = getServerUrl(applicationContext),
            apiToken = getServerApiToken(applicationContext),
        )
        val strings = AppStrings.forLanguage(getAppLanguage(applicationContext))

        return syncRepo.sync(beforeRecipes, beforeComments)
            .map { result ->
                setLastSyncStats(applicationContext, result.recipeCount, result.commentCount)
                if (result.newRecipes > 0) {
                    showNotification(
                        applicationContext,
                        NOTIFICATION_RECIPE,
                        strings.notificationChannel,
                        strings.notificationNewRecipe,
                    )
                }
                if (result.newComments > 0) {
                    showNotification(
                        applicationContext,
                        NOTIFICATION_COMMENT,
                        strings.notificationChannel,
                        strings.notificationNewComment,
                    )
                }
                Result.success()
            }
            .getOrElse { Result.retry() }
    }

    companion object {
        const val WORK_NAME = "chef_social_sync"
        private const val NOTIFICATION_RECIPE = 1001
        private const val NOTIFICATION_COMMENT = 1002
    }
}
