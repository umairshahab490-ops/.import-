package com.umairshahab.etea.studyplan.domain

import com.umairshahab.etea.studyplan.data.local.RevisionEntity
import com.umairshahab.etea.studyplan.data.local.TopicEntity
import org.json.JSONArray
import org.json.JSONObject

object BackupManager {

    fun createBackupJson(
        topics: List<TopicEntity>,
        revisions: List<RevisionEntity>,
        exportedAt: Long = System.currentTimeMillis()
    ): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("exportedAt", exportedAt)

        val topicsArray = JSONArray()
        for (topic in topics) {
            val tObj = JSONObject()
            tObj.put("id", topic.id)
            tObj.put("subject", topic.subject)
            tObj.put("title", topic.title)
            if (topic.chapter != null) {
                tObj.put("chapter", topic.chapter)
            } else {
                tObj.put("chapter", JSONObject.NULL)
            }
            tObj.put("createdAt", topic.createdAt)
            tObj.put("revisionHour", topic.revisionHour)
            tObj.put("revisionMinute", topic.revisionMinute)

            val intervalsArr = JSONArray()
            topic.intervals.forEach { intervalsArr.put(it) }
            tObj.put("intervals", intervalsArr)

            topicsArray.put(tObj)
        }
        root.put("topics", topicsArray)

        val revisionsArray = JSONArray()
        for (rev in revisions) {
            val rObj = JSONObject()
            rObj.put("id", rev.id)
            rObj.put("topicId", rev.topicId)
            rObj.put("intervalIndex", rev.intervalIndex)
            rObj.put("intervalDays", rev.intervalDays)
            rObj.put("dueAt", rev.dueAt)
            rObj.put("alertAt", rev.alertAt)
            rObj.put("status", rev.status)
            if (rev.completedAt != null) {
                rObj.put("completedAt", rev.completedAt)
            } else {
                rObj.put("completedAt", JSONObject.NULL)
            }
            revisionsArray.put(rObj)
        }
        root.put("revisions", revisionsArray)

        return root.toString()
    }

    fun parseBackupJson(jsonString: String): Pair<List<TopicEntity>, List<RevisionEntity>>? {
        return try {
            val root = JSONObject(jsonString)
            if (root.optInt("version", -1) != 1) return null
            if (!root.has("topics") || !root.has("revisions")) return null

            val topicsArray = root.getJSONArray("topics")
            val topicsList = mutableListOf<TopicEntity>()
            for (i in 0 until topicsArray.length()) {
                val tObj = topicsArray.getJSONObject(i)
                val intervalsArr = tObj.getJSONArray("intervals")
                val intervals = mutableListOf<Int>()
                for (j in 0 until intervalsArr.length()) {
                    intervals.add(intervalsArr.getInt(j))
                }
                topicsList.add(
                    TopicEntity(
                        id = tObj.getLong("id"),
                        subject = tObj.getString("subject"),
                        title = tObj.getString("title"),
                        chapter = if (tObj.isNull("chapter")) null else tObj.getString("chapter"),
                        createdAt = tObj.getLong("createdAt"),
                        revisionHour = tObj.getInt("revisionHour"),
                        revisionMinute = tObj.getInt("revisionMinute"),
                        intervals = intervals
                    )
                )
            }

            val revisionsArray = root.getJSONArray("revisions")
            val revisionsList = mutableListOf<RevisionEntity>()
            for (i in 0 until revisionsArray.length()) {
                val rObj = revisionsArray.getJSONObject(i)
                revisionsList.add(
                    RevisionEntity(
                        id = rObj.getLong("id"),
                        topicId = rObj.getLong("topicId"),
                        intervalIndex = rObj.getInt("intervalIndex"),
                        intervalDays = rObj.getInt("intervalDays"),
                        dueAt = rObj.getLong("dueAt"),
                        alertAt = rObj.getLong("alertAt"),
                        status = rObj.getString("status"),
                        completedAt = if (rObj.isNull("completedAt")) null else rObj.getLong("completedAt")
                    )
                )
            }

            Pair(topicsList, revisionsList)
        } catch (_: Exception) {
            null
        }
    }
}
