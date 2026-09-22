package com.umairshahab.etea.studyplan.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class BackupReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val context = applicationContext
        val prefs = context.getSharedPreferences("study_plan_prefs", Context.MODE_PRIVATE)
        val lastBackupExportedAt = prefs.getLong("last_backup_exported_at", 0L)
        val now = System.currentTimeMillis()
        val fourteenDaysMillis = 14L * 24L * 60L * 60L * 1000L

        if (lastBackupExportedAt <= 0L || (now - lastBackupExportedAt) > fourteenDaysMillis) {
            NotificationHelper.showBackupReminderNotification(context)
        }

        return Result.success()
    }
}
