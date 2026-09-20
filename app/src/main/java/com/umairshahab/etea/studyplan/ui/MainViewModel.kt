package com.umairshahab.etea.studyplan.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.umairshahab.etea.studyplan.data.local.AppDatabase
import com.umairshahab.etea.studyplan.data.local.RevisionDao
import com.umairshahab.etea.studyplan.data.local.RevisionEntity
import com.umairshahab.etea.studyplan.data.local.TopicDao
import com.umairshahab.etea.studyplan.data.local.TopicEntity
import com.umairshahab.etea.studyplan.domain.BackupManager
import com.umairshahab.etea.studyplan.domain.RevisionScheduler
import com.umairshahab.etea.studyplan.notifications.AlertScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(
    application: Application,
    private val database: AppDatabase,
    private val topicDao: TopicDao = database.topicDao(),
    private val revisionDao: RevisionDao = database.revisionDao()
) : AndroidViewModel(application) {

    val topics: StateFlow<List<TopicEntity>> = topicDao.observeAll()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val revisions: StateFlow<List<RevisionEntity>> = revisionDao.observeAll()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // In-memory snapshot for 5-second UNDO
    private var lastDeletedTopicSnapshot: TopicEntity? = null
    private var lastDeletedRevisionsSnapshot: List<RevisionEntity> = emptyList()

    // In-memory snapshot for Batch Mark-Done UNDO: full snapshots of affected revisions
    private var lastBatchMarkDoneSnapshot: List<RevisionEntity> = emptyList()

    init {
        // Run missed scan and top-up upcoming alarms on app launch
        scanForMissed()
        topUpAlarms()
    }

    private fun topUpAlarms() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                AlertScheduler.rescheduleUpcoming(getApplication())
            } catch (_: Exception) {
                // Fail silently
            }
        }
    }

    fun scanForMissed() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val overdue = revisionDao.getScheduledPastDue(now)
            for (rev in overdue) {
                if (RevisionScheduler.shouldTransitionToMissed(rev.status, rev.dueAt, now)) {
                    revisionDao.updateStatus(rev.id, "MISSED", null)
                }
            }
        }
    }

    fun addTopic(
        subject: String,
        title: String,
        chapter: String?,
        revisionHour: Int,
        revisionMinute: Int,
        intervals: List<Int>
    ) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val topic = TopicEntity(
                subject = subject,
                title = title.trim(),
                chapter = chapter?.trim()?.ifBlank { null },
                createdAt = now,
                revisionHour = revisionHour,
                revisionMinute = revisionMinute,
                intervals = intervals
            )
            val topicId = topicDao.insert(topic)
            val baseTimestamp = RevisionScheduler.baseTimestamp(now, revisionHour, revisionMinute)
            val futureRevisions = RevisionScheduler.buildRevisions(topicId, baseTimestamp, intervals, now)
            if (futureRevisions.isNotEmpty()) {
                revisionDao.insertAll(futureRevisions)
                // Schedule alert alarms
                val app = getApplication<Application>()
                futureRevisions.forEach { rev ->
                    AlertScheduler.schedule(
                        context = app,
                        revisionId = rev.id,
                        alertAt = rev.alertAt,
                        topicTitle = topic.title,
                        subject = topic.subject
                    )
                }
            }
        }
    }

    fun updateTopic(
        topicId: Long,
        subject: String,
        title: String,
        chapter: String?,
        revisionHour: Int,
        revisionMinute: Int,
        intervals: List<Int>
    ) {
        viewModelScope.launch {
            val existing = topics.value.find { it.id == topicId } ?: return@launch
            val updated = existing.copy(
                subject = subject,
                title = title.trim(),
                chapter = chapter?.trim()?.ifBlank { null },
                revisionHour = revisionHour,
                revisionMinute = revisionMinute,
                intervals = intervals
            )
            topicDao.update(updated)

            val scheduleChanged = existing.revisionHour != revisionHour ||
                existing.revisionMinute != revisionMinute ||
                existing.intervals != intervals

            if (scheduleChanged) {
                // Cancel any scheduled alerts for upcoming revisions
                val existingRevs = revisionDao.getForTopic(topicId)
                val app = getApplication<Application>()
                existingRevs.filter { it.status == "SCHEDULED" }.forEach { rev ->
                    AlertScheduler.cancel(app, rev.id)
                }

                // Keep completed revision history, delete only SCHEDULED revisions
                revisionDao.deleteScheduledForTopic(topicId)

                val now = System.currentTimeMillis()
                // Regenerate future revisions from the ORIGINAL createdAt base with the new settings
                val baseTimestamp = RevisionScheduler.baseTimestamp(existing.createdAt, revisionHour, revisionMinute)
                val futureRevisions = RevisionScheduler.buildRevisions(topicId, baseTimestamp, intervals, now)
                if (futureRevisions.isNotEmpty()) {
                    revisionDao.insertAll(futureRevisions)
                    futureRevisions.forEach { rev ->
                        AlertScheduler.schedule(
                            context = app,
                            revisionId = rev.id,
                            alertAt = rev.alertAt,
                            topicTitle = updated.title,
                            subject = updated.subject
                        )
                    }
                }
            }
        }
    }

    fun deleteTopic(
        topicId: Long,
        onDeleted: ((TopicEntity) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val topic = topicDao.getById(topicId) ?: return@launch
            val relatedRevisions = revisionDao.getForTopic(topicId)

            // Preserve snapshot for UNDO
            lastDeletedTopicSnapshot = topic
            lastDeletedRevisionsSnapshot = relatedRevisions

            // Cancel alarms
            val app = getApplication<Application>()
            relatedRevisions.forEach { rev ->
                AlertScheduler.cancel(app, rev.id)
            }

            // Delete immediately
            revisionDao.deleteAllForTopic(topicId)
            topicDao.deleteById(topicId)

            onDeleted?.invoke(topic)
        }
    }

    fun undoDelete() {
        val topic = lastDeletedTopicSnapshot ?: return
        val revisions = lastDeletedRevisionsSnapshot
        viewModelScope.launch {
            topicDao.insert(topic)
            if (revisions.isNotEmpty()) {
                revisionDao.insertAll(revisions)
                val app = getApplication<Application>()
                val now = System.currentTimeMillis()
                revisions.filter { it.status == "SCHEDULED" && it.alertAt > now }.forEach { rev ->
                    AlertScheduler.schedule(
                        context = app,
                        revisionId = rev.id,
                        alertAt = rev.alertAt,
                        topicTitle = topic.title,
                        subject = topic.subject
                    )
                }
            }
            lastDeletedTopicSnapshot = null
            lastDeletedRevisionsSnapshot = emptyList()
        }
    }

    fun markDone(revisionId: Long) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            revisionDao.updateStatus(revisionId, "DONE", now)
            AlertScheduler.cancel(getApplication(), revisionId)
        }
    }

    fun batchMarkDone(revisionIds: List<Long>) {
        if (revisionIds.isEmpty()) return
        viewModelScope.launch {
            val currentRevisions = revisions.value
            lastBatchMarkDoneSnapshot = currentRevisions.filter { it.id in revisionIds }
            val now = System.currentTimeMillis()
            database.withTransaction {
                revisionDao.updateStatusForIds(revisionIds, "DONE", now)
            }
            val app = getApplication<Application>()
            revisionIds.forEach { id ->
                AlertScheduler.cancel(app, id)
            }
        }
    }

    fun undoBatchMarkDone() {
        val snapshot = lastBatchMarkDoneSnapshot
        if (snapshot.isEmpty()) return
        viewModelScope.launch {
            database.withTransaction {
                snapshot.forEach { item ->
                    revisionDao.updateStatus(item.id, item.status, item.completedAt)
                }
            }
            // If any were SCHEDULED, reschedule upcoming alarms
            val app = getApplication<Application>()
            val now = System.currentTimeMillis()
            val allTopics = topicDao.getAll().associateBy { it.id }
            snapshot.filter { it.status == "SCHEDULED" && it.alertAt > now }.forEach { rev ->
                val topic = allTopics[rev.topicId]
                if (topic != null) {
                    AlertScheduler.schedule(
                        context = app,
                        revisionId = rev.id,
                        alertAt = rev.alertAt,
                        topicTitle = topic.title,
                        subject = topic.subject
                    )
                }
            }
            lastBatchMarkDoneSnapshot = emptyList()
        }
    }

    suspend fun restoreBackup(newTopics: List<TopicEntity>, newRevisions: List<RevisionEntity>): Pair<Int, Int> = withContext(Dispatchers.IO) {
        val targetTopics = mutableListOf<TopicEntity>()
        val targetRevisions = mutableListOf<RevisionEntity>()
        val counts = BackupManager.restore(
            newTopics = newTopics,
            newRevisions = newRevisions,
            targetTopics = targetTopics,
            targetRevisions = targetRevisions
        )
        database.withTransaction {
            revisionDao.deleteAll()
            topicDao.deleteAll()
            topicDao.insertAll(targetTopics)
            revisionDao.insertAll(targetRevisions)
        }
        val app = getApplication<Application>()
        val now = System.currentTimeMillis()
        targetRevisions.filter { it.status == "SCHEDULED" && it.alertAt > now }.forEach { rev ->
            val matchingTopic = targetTopics.find { it.id == rev.topicId }
            AlertScheduler.schedule(
                context = app,
                revisionId = rev.id,
                alertAt = rev.alertAt,
                topicTitle = matchingTopic?.title ?: "Revision",
                subject = matchingTopic?.subject ?: "Study"
            )
        }
        counts
    }

    class Factory(
        private val application: Application,
        private val database: AppDatabase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                return MainViewModel(application, database) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
