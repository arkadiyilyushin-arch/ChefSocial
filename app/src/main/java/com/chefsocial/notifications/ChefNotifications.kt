package com.chefsocial.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.chefsocial.R

private const val CHANNEL_ID = "chefly_updates"

fun ensureNotificationChannel(context: Context, channelName: String) {
    val channel = NotificationChannel(
        CHANNEL_ID,
        channelName,
        NotificationManager.IMPORTANCE_DEFAULT,
    )
    context.getSystemService(NotificationManager::class.java)
        ?.createNotificationChannel(channel)
}

@SuppressLint("MissingPermission")
fun showNotification(context: Context, id: Int, title: String, body: String) {
    if (
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
        PackageManager.PERMISSION_GRANTED
    ) {
        return
    }
    ensureNotificationChannel(context, title)
    val notification = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle(title)
        .setContentText(body)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)
        .build()
    NotificationManagerCompat.from(context).notify(id, notification)
}
