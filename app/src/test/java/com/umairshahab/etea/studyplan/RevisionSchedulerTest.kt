package com.umairshahab.etea.studyplan

import com.umairshahab.etea.studyplan.domain.RevisionScheduler
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class RevisionSchedulerTest {

    private val zoneId: ZoneId = ZoneId.of("UTC")

    @Test
    fun defaultIntervalsGenerateExactlyFiveRevisionsWithCorrectDueDates() {
        val topicId = 1L
        val requestedHour = 10
        val requestedMinute = 30

        // Fixed anchor at 2026-09-01 08:00 UTC
        val anchorZdt = ZonedDateTime.of(2026, 9, 1, 8, 0, 0, 0, zoneId)
        val anchorMillis = anchorZdt.toInstant().toEpochMilli()

        val baseMillis = RevisionScheduler.baseTimestamp(anchorMillis, requestedHour, requestedMinute, zoneId)
        val revisions = RevisionScheduler.buildRevisions(
            topicId = topicId,
            baseMillis = baseMillis,
            intervals = RevisionScheduler.DEFAULT_INTERVALS,
            nowMillis = anchorMillis,
            zoneId = zoneId
        )

        assertEquals(5, revisions.size)
        assertEquals(listOf(1, 3, 7, 15, 30), RevisionScheduler.DEFAULT_INTERVALS)

        val expectedDays = listOf(1, 3, 7, 15, 30)
        val baseZdt = Instant.ofEpochMilli(baseMillis).atZone(zoneId)

        revisions.forEachIndexed { index, revision ->
            assertEquals(expectedDays[index], revision.intervalDays)
            assertEquals(index, revision.intervalIndex)
            val expectedDueMillis = baseZdt.plusDays(expectedDays[index].toLong()).toInstant().toEpochMilli()
            assertEquals(expectedDueMillis, revision.dueAt)
        }
    }

    @Test
    fun dueDatesLandAtRequestedHourAndMinute() {
        val topicId = 2L
        val requestedHour = 16
        val requestedMinute = 45

        val anchorZdt = ZonedDateTime.of(2026, 9, 1, 10, 0, 0, 0, zoneId)
        val anchorMillis = anchorZdt.toInstant().toEpochMilli()

        val baseMillis = RevisionScheduler.baseTimestamp(anchorMillis, requestedHour, requestedMinute, zoneId)
        val revisions = RevisionScheduler.buildRevisions(
            topicId = topicId,
            baseMillis = baseMillis,
            intervals = RevisionScheduler.DEFAULT_INTERVALS,
            nowMillis = anchorMillis,
            zoneId = zoneId
        )

        assertTrue(revisions.isNotEmpty())
        for (revision in revisions) {
            val dueZdt = Instant.ofEpochMilli(revision.dueAt).atZone(zoneId)
            assertEquals(requestedHour, dueZdt.hour)
            assertEquals(requestedMinute, dueZdt.minute)
        }
    }

    @Test
    fun notificationAlertOffsetIsTwoMinutesBefore() {
        val topicId = 3L
        val anchorZdt = ZonedDateTime.of(2026, 9, 1, 8, 0, 0, 0, zoneId)
        val anchorMillis = anchorZdt.toInstant().toEpochMilli()

        val baseMillis = RevisionScheduler.baseTimestamp(anchorMillis, 9, 0, zoneId)
        val revisions = RevisionScheduler.buildRevisions(
            topicId = topicId,
            baseMillis = baseMillis,
            intervals = RevisionScheduler.DEFAULT_INTERVALS,
            nowMillis = anchorMillis,
            zoneId = zoneId
        )

        val expectedOffsetMillis = 120000L // 2 min
        revisions.forEach { revision ->
            assertEquals(expectedOffsetMillis, revision.dueAt - revision.alertAt)
        }
    }

    @Test
    fun customIntervalsProduceMatchingCountAndDays() {
        val topicId = 4L
        val customIntervals = listOf(2, 5, 10, 20)
        val anchorZdt = ZonedDateTime.of(2026, 9, 1, 8, 0, 0, 0, zoneId)
        val anchorMillis = anchorZdt.toInstant().toEpochMilli()

        val baseMillis = RevisionScheduler.baseTimestamp(anchorMillis, 11, 0, zoneId)
        val revisions = RevisionScheduler.buildRevisions(
            topicId = topicId,
            baseMillis = baseMillis,
            intervals = customIntervals,
            nowMillis = anchorMillis,
            zoneId = zoneId
        )

        assertEquals(4, revisions.size)
        val expectedDays = listOf(2, 5, 10, 20)
        revisions.forEachIndexed { index, revision ->
            assertEquals(expectedDays[index], revision.intervalDays)
            assertEquals(index, revision.intervalIndex)
        }
    }

    @Test
    fun targetTimeAlreadyPassedTodayPushesBaseTimestampToTomorrow() {
        val requestedHour = 8
        val requestedMinute = 0

        // Anchor at 2026-09-01 10:00 UTC (past the 08:00 requested time)
        val anchorZdt = ZonedDateTime.of(2026, 9, 1, 10, 0, 0, 0, zoneId)
        val anchorMillis = anchorZdt.toInstant().toEpochMilli()

        val baseMillis = RevisionScheduler.baseTimestamp(anchorMillis, requestedHour, requestedMinute, zoneId)
        val baseZdt = Instant.ofEpochMilli(baseMillis).atZone(zoneId)

        // Must push to tomorrow: 2026-09-02 08:00 UTC
        assertEquals(2026, baseZdt.year)
        assertEquals(9, baseZdt.monthValue)
        assertEquals(2, baseZdt.dayOfMonth)
        assertEquals(requestedHour, baseZdt.hour)
        assertEquals(requestedMinute, baseZdt.minute)
    }

    @Test
    fun targetTimeExactlyEqualToAnchorPushesBaseTimestampToTomorrow() {
        val requestedHour = 8
        val requestedMinute = 30

        // Anchor at 2026-09-01 08:30:00.000 UTC (EXACTLY equal to target time)
        val anchorZdt = ZonedDateTime.of(2026, 9, 1, requestedHour, requestedMinute, 0, 0, zoneId)
        val anchorMillis = anchorZdt.toInstant().toEpochMilli()

        // Spec: "if not after anchor, +1 day" -> equal is NOT after anchor, rolls to next day
        val baseMillis = RevisionScheduler.baseTimestamp(anchorMillis, requestedHour, requestedMinute, zoneId)
        val baseZdt = Instant.ofEpochMilli(baseMillis).atZone(zoneId)

        assertEquals(2026, baseZdt.year)
        assertEquals(9, baseZdt.monthValue)
        assertEquals(2, baseZdt.dayOfMonth)
        assertEquals(requestedHour, baseZdt.hour)
        assertEquals(requestedMinute, baseZdt.minute)
    }

    @Test
    fun shouldTransitionToMissedPurePredicateTest() {
        val now = 100_000L
        val pastDue = 90_000L
        val futureDue = 110_000L

        // SCHEDULED + past = true
        assertTrue(RevisionScheduler.shouldTransitionToMissed("SCHEDULED", pastDue, now))

        // SCHEDULED + future = false
        assertFalse(RevisionScheduler.shouldTransitionToMissed("SCHEDULED", futureDue, now))

        // MISSED + past = false (idempotent, does not re-transition)
        assertFalse(RevisionScheduler.shouldTransitionToMissed("MISSED", pastDue, now))

        // DONE = false (never revert completed items)
        assertFalse(RevisionScheduler.shouldTransitionToMissed("DONE", pastDue, now))
        assertFalse(RevisionScheduler.shouldTransitionToMissed("DONE", futureDue, now))
    }
}
