package com.umairshahab.etea.studyplan.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.umairshahab.etea.studyplan.StudyPlanApp
import com.umairshahab.etea.studyplan.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlertReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val revisionId = intent.getLongExtra(EXTRA_REVISION_ID, -1L)

        if (action == ACTION_MARK_DONE) {
            if (revisionId != -1L) {
                NotificationHelper.cancelNotification(context, revisionId.toInt())
                AlertScheduler.cancel(context, revisionId)

                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val app = context.applicationContext as? StudyPlanApp
                        val db = app?.database ?: AppDatabase.getInstance(context)
                        db.revisionDao().updateStatus(revisionId, "DONE", System.currentTimeMillis())
                        NotificationHelper.updateGroupSummary(context)
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
            return
        }

        val topicTitle = intent.getStringExtra(EXTRA_TOPIC_TITLE) ?: "Study Topic"
        val subject = intent.getStringExtra(EXTRA_SUBJECT) ?: "Curriculum"

        if (revisionId != -1L) {
            NotificationHelper.showReminderNotification(context, revisionId, topicTitle, subject)
        }
    }

    companion object {
        const val ACTION_MARK_DONE = "MARK_DONE"
        const val EXTRA_REVISION_ID = "extra_revision_id"
        const val EXTRA_TOPIC_TITLE = "extra_topic_title"
        const val EXTRA_SUBJECT = "extra_subject"
    }
}
