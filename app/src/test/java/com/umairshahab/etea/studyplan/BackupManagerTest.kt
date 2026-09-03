package com.umairshahab.etea.studyplan

import com.umairshahab.etea.studyplan.data.local.RevisionEntity
import com.umairshahab.etea.studyplan.data.local.TopicEntity
import com.umairshahab.etea.studyplan.domain.BackupManager
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class BackupManagerTest {

    @Test
    fun createBackupJsonProducesValidJsonWithVersionOneAndCorrectCounts() {
        val topics = listOf(
            TopicEntity(
                id = 1L,
                subject = "Maths",
                title = "Calculus Derivatives",
                chapter = "Chapter 4",
                createdAt = 1000L,
                revisionHour = 9,
                revisionMinute = 30,
                intervals = listOf(1, 3, 7, 15, 30)
            ),
            TopicEntity(
                id = 2L,
                subject = "Physics",
                title = "Kinematics",
                chapter = null,
                createdAt = 2000L,
                revisionHour = 14,
                revisionMinute = 0,
                intervals = listOf(2, 4, 8)
            )
        )

        val revisions = listOf(
            RevisionEntity(
                id = 101L,
                topicId = 1L,
                intervalIndex = 0,
                intervalDays = 1,
                dueAt = 5000L,
                alertAt = 4100L,
                status = "SCHEDULED",
                completedAt = null
            ),
            RevisionEntity(
                id = 102L,
                topicId = 1L,
                intervalIndex = 1,
                intervalDays = 3,
                dueAt = 7000L,
                alertAt = 6100L,
                status = "DONE",
                completedAt = 7050L
            )
        )

        val jsonStr = BackupManager.createBackupJson(topics, revisions, exportedAt = 9999L)
        assertNotNull(jsonStr)

        val root = JSONObject(jsonStr)
        assertEquals(1, root.getInt("version"))
        assertEquals(9999L, root.getLong("exportedAt"))

        val topicsArr = root.getJSONArray("topics")
        assertEquals(2, topicsArr.length())

        val revisionsArr = root.getJSONArray("revisions")
        assertEquals(2, revisionsArr.length())
    }

    @Test
    fun parseBackupJsonRoundTripsTopicsAndRevisionsWithoutDataLoss() {
        val originalTopics = listOf(
            TopicEntity(
                id = 10L,
                subject = "Chemistry",
                title = "Organic Synthesis",
                chapter = "Reactions",
                createdAt = 123456789L,
                revisionHour = 8,
                revisionMinute = 15,
                intervals = listOf(1, 3, 7, 15, 30)
            ),
            TopicEntity(
                id = 20L,
                subject = "English",
                title = "Grammar & Vocabulary",
                chapter = null,
                createdAt = 987654321L,
                revisionHour = 18,
                revisionMinute = 0,
                intervals = listOf(3, 7, 14)
            )
        )

        val originalRevisions = listOf(
            RevisionEntity(
                id = 501L,
                topicId = 10L,
                intervalIndex = 0,
                intervalDays = 1,
                dueAt = 123500000L,
                alertAt = 123490000L,
                status = "SCHEDULED",
                completedAt = null
            ),
            RevisionEntity(
                id = 502L,
                topicId = 20L,
                intervalIndex = 0,
                intervalDays = 3,
                dueAt = 988000000L,
                alertAt = 987990000L,
                status = "DONE",
                completedAt = 988000050L
            )
        )

        val jsonStr = BackupManager.createBackupJson(originalTopics, originalRevisions)
        val result = BackupManager.parseBackupJson(jsonStr)

        assertNotNull(result)
        val (parsedTopics, parsedRevisions) = result!!

        assertEquals(originalTopics.size, parsedTopics.size)
        assertEquals(originalRevisions.size, parsedRevisions.size)

        assertEquals(originalTopics[0], parsedTopics[0])
        assertEquals(originalTopics[1], parsedTopics[1])

        assertEquals(originalRevisions[0], parsedRevisions[0])
        assertEquals(originalRevisions[1], parsedRevisions[1])
    }

    @Test
    fun parseBackupJsonReturnsNullOnInvalidOrCorruptedJsonOrMismatchedVersion() {
        // Corrupted / malformed JSON
        assertNull(BackupManager.parseBackupJson("{ invalid json text"))
        assertNull(BackupManager.parseBackupJson(""))

        // Mismatched version
        val wrongVersionJson = """
            {
                "version": 2,
                "exportedAt": 12345,
                "topics": [],
                "revisions": []
            }
        """.trimIndent()
        assertNull(BackupManager.parseBackupJson(wrongVersionJson))

        // Missing required keys
        val missingKeysJson = """
            {
                "version": 1,
                "exportedAt": 12345
            }
        """.trimIndent()
        assertNull(BackupManager.parseBackupJson(missingKeysJson))
    }
}
