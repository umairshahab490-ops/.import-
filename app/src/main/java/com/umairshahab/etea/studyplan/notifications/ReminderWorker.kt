package com.umairshahab.etea.studyplan.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.umairshahab.etea.studyplan.StudyPlanApp
import com.umairshahab.etea.studyplan.data.local.AppDatabase

class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val context = applicationContext
        val app = context as? StudyPlanApp
        val db = app?.database ?: AppDatabase.getInstance(context)
        val topicDao = db.topicDao()
        val revisionDao = db.revisionDao()

        val now = System.currentTimeMillis()

        // 1. Transition SCHEDULED revisions with dueAt < now to MISSED in DB, and show one notification per transition
        val overdueRevisions = revisionDao.getScheduledPastDue(now)
        for (rev in overdueRevisions) {
            revisionDao.updateStatus(rev.id, "MISSED", null)
            val topic = topicDao.getById(rev.topicId)
            NotificationHelper.showMissedNotification(
                context = context,
                revisionId = rev.id,
                topicTitle = topic?.title ?: "Topic #${rev.topicId}",
                subject = topic?.subject ?: ""
            )
        }

        // 2. Schedule alarms for revisions whose alertAt falls within the next 48 hours (tops up alarms)
        val windowEnd = now + 48L * 60 * 60 * 1000L
        val upcomingRevisions = revisionDao.getScheduledWithAlertBetween(now, windowEnd)
        for (rev in upcomingRevisions) {
            val topic = topicDao.getById(rev.topicId)
            AlertScheduler.schedule(
                context = context,
                revisionId = rev.id,
                alertAt = rev.alertAt,
                topicTitle = topic?.title ?: "Topic #${rev.topicId}",
                subject = topic?.subject ?: ""
            )
        }

        return Result.success()
    }
}
