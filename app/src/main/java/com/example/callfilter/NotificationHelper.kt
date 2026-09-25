package com.example.callfilter

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object NotificationHelper {

    private const val CHANNEL_ID = "unknown_call_channel"
    private const val CHANNEL_NAME = "Unknown Caller Alerts"

    /** Call once, e.g. from Application/MainActivity onCreate. Safe to call repeatedly. */
    fun createNotificationChannel(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return

        // IMPORTANCE_DEFAULT = shows briefly + makes a short notification sound,
        // same tier WhatsApp/SMS use. NOT high-priority, so no ringtone-like alert.
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notifies you when someone not in your contacts calls"
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 150, 100, 150) // short buzz, not a ring pattern
            setSound(
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
        }
        manager.createNotificationChannel(channel)
    }

    fun showMissedCallNotification(context: Context, phoneNumber: String, callerLabel: String?) {
        val title = "Missed call from unknown number"
        val text = callerLabel ?: phoneNumber

        // Tapping the notification opens the dialer pre-filled with the number,
        // so the user can call back with one tap.
        val callBackIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
        val pendingIntent = PendingIntent.getActivity(
            context,
            phoneNumber.hashCode(),
            callBackIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.sym_call_missed)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText("$text\nTap to call back"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT) // message-style, not urgent/heads-up-forced
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)  // tells the system to treat it like a chat/SMS alert
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val hasPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            NotificationManagerCompat.from(context).notify(phoneNumber.hashCode(), builder.build())
        }
    }
}
