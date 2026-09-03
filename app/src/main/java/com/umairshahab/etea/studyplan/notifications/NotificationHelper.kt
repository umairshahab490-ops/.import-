package com.umairshahab.etea.studyplan.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.umairshahab.etea.studyplan.MainActivity
import com.umairshahab.etea.studyplan.R

object NotificationHelper {
    const val CHANNEL_REMINDERS = "revision_reminders"
    const val CHANNEL_MISSED = "missed_revisions"

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return

            val reminderChannel = NotificationChannel(
                CHANNEL_REMINDERS,
                "Revision Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts 2 minutes before revision sessions are due"
                enableVibration(true)
            }

            val missedChannel = NotificationChannel(
                CHANNEL_MISSED,
                "Missed Revisions",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for overdue study revisions"
            }

            notificationManager.createNotificationChannel(reminderChannel)
            notificationManager.createNotificationChannel(missedChannel)
        }
    }

    fun showReminderNotification(
        context: Context,
        revisionId: Long,
        topicTitle: String,
        subject: String
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            revisionId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_REMINDERS)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle("Revision Due Soon: $topicTitle")
            .setContentText("Subject: $subject • Scheduled revision session starts in 2 minutes.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(revisionId.toInt(), notification)
        } catch (_: SecurityException) {
            // Permission denied or restricted; fail silently
        }
    }

    fun showMissedNotification(
        context: Context,
        revisionId: Long,
        topicTitle: String,
        subject: String
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            (revisionId + 100000L).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_MISSED)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle("Missed Revision: $topicTitle")
            .setContentText("Subject: $subject • This revision was missed. Tap to review and complete.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify((revisionId + 100000L).toInt(), notification)
        } catch (_: SecurityException) {
            // Permission denied or restricted; fail silently
        }
    }
}
