package app.bicast.finma.firebase

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import app.bicast.finma.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {
    val NOT_ID = 12
    val NOT_CHANNEL_NAME = "FCM_NOTIFICATION"
    val NOT_CHANNEL_ID = "1002"
    val soundUri :Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d("FCM", "onMessageReceived : " + message.notification!!.title)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            sendNotification(message.notification!!.title!!,message.notification!!.body!!)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New Token : $token")
        application.getSharedPreferences("firebase", 0).edit().putString("fcm_token", token).apply()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendNotification(title: String, body: String) {
        val notif = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val attr = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        val notificationChannel =
            NotificationChannel(NOT_CHANNEL_ID, NOT_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
        notificationChannel.enableVibration(true)
        notificationChannel.setSound(soundUri, attr)
        notificationChannel.vibrationPattern = longArrayOf(1000, 1000, 1000, 1000, 1000)
        notif.createNotificationChannel(notificationChannel)
        val notify: Notification =
            NotificationCompat.Builder(applicationContext,NOT_CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setContentTitle(title)
                .setSmallIcon(R.drawable.ic_notification)
                .setSound(soundUri)
                .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
                .build()

        notify.flags = notify.flags or Notification.FLAG_AUTO_CANCEL
        notif.notify(NOT_ID, notify)
    }
}