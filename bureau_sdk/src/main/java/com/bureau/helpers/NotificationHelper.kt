package com.bureau.helpers

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.bureau.R
import com.bureau.utils.BUREAU
import com.bureau.utils.NOTIFICATION_CHANNEL_ID
import com.bureau.utils.NOTIFICATION_CHANNEL_NAME
import java.util.*


class NotificationHelper {

    fun showNotification(context: Context, title: String?, description: String?) {
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        val intent = Intent(context, defaultActivity::class.java).apply {
//            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//        }
        val intent: Intent? =
            context.packageManager.getLaunchIntentForPackage("com.devstory.bureau")
        val pendingIntent: PendingIntent? = PendingIntent.getActivity(context, 0, intent, 0)
        val mNotificationCompatBuilder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_NAME,
                BUREAU,
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.GREEN
            notificationChannel.enableVibration(true)
            notificationManager.createNotificationChannel(notificationChannel)
            NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_NAME)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setContentTitle(title)
                .setContentText(description)
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentIntent(pendingIntent)
        } else {
            commonNotification(
                context, title,
                description, pendingIntent!!, defaultSoundUri!!
            )
        }
        val uniqueNotificationId = (Date().time / 1000L % Int.MAX_VALUE).toString().toInt()
        notificationManager.notify(uniqueNotificationId, mNotificationCompatBuilder?.build())
    }

    private fun commonNotification(
        context: Context,
        title: String?,
        messageBody: String?, pendingIntent: PendingIntent,
        defaultSoundUri: Uri
    ): NotificationCompat.Builder {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setContentTitle(title)
                .setDefaults(NotificationCompat.DEFAULT_SOUND)
                .setContentText(messageBody)
                .setChannelId(NOTIFICATION_CHANNEL_NAME)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentIntent(pendingIntent)
                .setSound(defaultSoundUri).setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
        } else {
            NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(messageBody)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentIntent(pendingIntent)
        }
    }
}