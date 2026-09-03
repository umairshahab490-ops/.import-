package com.umairshahab.etea.studyplan.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlertReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val revisionId = intent.getLongExtra(EXTRA_REVISION_ID, -1L)
        val topicTitle = intent.getStringExtra(EXTRA_TOPIC_TITLE) ?: "Study Topic"
        val subject = intent.getStringExtra(EXTRA_SUBJECT) ?: "Curriculum"

        if (revisionId != -1L) {
            NotificationHelper.showReminderNotification(context, revisionId, topicTitle, subject)
        }
    }

    companion object {
        const val EXTRA_REVISION_ID = "extra_revision_id"
        const val EXTRA_TOPIC_TITLE = "extra_topic_title"
        const val EXTRA_SUBJECT = "extra_subject"
    }
}
