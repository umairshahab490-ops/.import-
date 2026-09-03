package com.umairshahab.etea.studyplan.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

object AlertScheduler {
    private const val WINDOW_HOURS_MS = 48L * 60 * 60 * 1000L

    fun schedule(
        context: Context,
        revisionId: Long,
        alertAt: Long,
        topicTitle: String,
        subject: String
    ) {
        val now = System.currentTimeMillis()
        if (alertAt <= now || alertAt > now + WINDOW_HOURS_MS) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, AlertReceiver::class.java).apply {
            putExtra(AlertReceiver.EXTRA_REVISION_ID, revisionId)
            putExtra(AlertReceiver.EXTRA_TOPIC_TITLE, topicTitle)
            putExtra(AlertReceiver.EXTRA_SUBJECT, subject)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            revisionId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        alertAt,
                        pendingIntent
                    )
                } else {
                    // Exact alarms disallowed; best-effort fallback
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        alertAt,
                        pendingIntent
                    )
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    alertAt,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    alertAt,
                    pendingIntent
                )
            }
        } catch (_: SecurityException) {
            // Never crash on any Android version if permission is restricted
        }
    }

    fun cancel(context: Context, revisionId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, AlertReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            revisionId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
        }
    }
}
