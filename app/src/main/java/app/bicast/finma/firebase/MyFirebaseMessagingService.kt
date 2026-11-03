package app.bicast.finma.firebase

import android.app.Notification
import android.app.NotificationManager
import android.media.RingtoneManager
import android.util.Log
import androidx.core.app.NotificationCompat
import app.bicast.finma.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {
    val NOT_ID = 12

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        // Works for notification messages and data-only messages
        val title = message.notification?.title ?: message.data["title"] ?: return
        val body = message.notification?.body ?: message.data["body"] ?: ""
        Log.d("FCM", "onMessageReceived : $title")

        // Pick the channel: FCM "android_channel_id" -> data["channel_id"] -> default
        val requested = message.notification?.channelId ?: message.data["channel_id"]
        val channelId = when (requested) {
            NotificationChannels.UPDATES_ID -> NotificationChannels.UPDATES_ID
            NotificationChannels.MISC_ID -> NotificationChannels.MISC_ID
            else -> NotificationChannels.MAIN_ID
        }

        sendNotification(title, body, channelId)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.i("FCM", "New Token : $token")
        application.getSharedPreferences("firebase", 0).edit().putString("fcm_token", token).apply()
    }

    private fun sendNotification(title: String, body: String, channelId: String) {
        // Safe to call repeatedly; also covers the case where Application didn't run first
        NotificationChannels.createAll(applicationContext)

        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.ic_notification)
            .setSound(soundUri)
            .setAutoCancel(true)

        if (channelId == NotificationChannels.MAIN_ID) {
            builder.setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
        } else {
            builder.setPriority(NotificationCompat.PRIORITY_DEFAULT)
        }

        val notify: Notification = builder.build()
        // Use a unique id per message so notifications don't overwrite each other
        val id = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
        nm.notify(id, notify)
    }
}
