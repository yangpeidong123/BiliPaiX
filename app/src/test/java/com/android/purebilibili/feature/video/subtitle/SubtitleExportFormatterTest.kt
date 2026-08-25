package com.android.purebilibili.feature.video.subtitle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SubtitleExportFormatterTest {

    private val cues = listOf(
        SubtitleCue(startMs = 1_000L, endMs = 3_500L, content = "你好"),
        SubtitleCue(startMs = 3_600_000L, endMs = 3_661_234L, content = "World\n第二行"),
    )

    @Test
    fun webvttUsesDotMillisecondSeparator() {
        assertEquals("00:00:01.000", formatTs(1_000L, srt = false))
        assertEquals("00:00:03.500", formatTs(3_500L, srt = false))
    }

    @Test
    fun srtUsesCommaMillisecondSeparator() {
        assertEquals("00:00:01,000", formatTs(1_000L, srt = true))
        assertEquals("01:01:01,234", formatTs(3_661_234L, srt = true))
    }

    @Test
    fun negativeTimestampClampedToZero() {
        assertEquals("00:00:00,000", formatTs(-5L, srt = true))
    }

    @Test
    fun webvttOutputContainsHeaderAndCues() {
        val out = SubtitleExportFormatter.formatSubtitles(
            cues,
            SubtitleExportFormatter.SubtitleExportFormat.WEBVTT,
            videoTitle = "测试视频",
        )
        assertTrue(out.startsWith("WEBVTT"))
        assertTrue(out.contains("NOTE title: 测试视频"))
        assertTrue(out.contains("00:00:01.000 --> 00:00:03.500"))
        assertTrue(out.contains("你好"))
        // cue 内容里的换行原样保留
        assertTrue(out.contains("World\n第二行"))
    }

    @Test
    fun srtOutputHasNoWebvttHeader() {
        val out = SubtitleExportFormatter.formatSubtitles(
            cues,
            SubtitleExportFormatter.SubtitleExportFormat.SRT,
            videoTitle = "测试视频",
        )
        assertFalse(out.contains("WEBVTT"))
        assertFalse(out.contains("NOTE title"))
        assertTrue(out.startsWith("1\n"))
        assertTrue(out.contains("01:00:00,000 --> 01:01:01,234"))
    }

    @Test
    fun invalidCuesFilteredOut() {
        val mixed = listOf(
            SubtitleCue(startMs = 0L, endMs = 0L, content = "zero-duration"),
            SubtitleCue(startMs = 500L, endMs = 200L, content = "reversed"),
            SubtitleCue(startMs = 1_000L, endMs = 2_000L, content = "valid"),
        )
        val out = SubtitleExportFormatter.formatSubtitles(
            mixed,
            SubtitleExportFormatter.SubtitleExportFormat.SRT,
        )
        assertFalse(out.contains("zero-duration"))
        assertFalse(out.contains("reversed"))
        assertTrue(out.contains("valid"))
        // 只有 1 条有效 cue，编号从 1 开始
        assertTrue(out.trimEnd().startsWith("1\n"))
    }

    @Test
    fun emptyCuesProduceMinimalOutput() {
        val vtt = SubtitleExportFormatter.formatSubtitles(
            emptyList(),
            SubtitleExportFormatter.SubtitleExportFormat.WEBVTT,
        )
        assertTrue(vtt.startsWith("WEBVTT"))

        val srt = SubtitleExportFormatter.formatSubtitles(
            emptyList(),
            SubtitleExportFormatter.SubtitleExportFormat.SRT,
        )
        assertEquals("\n", srt)
    }

    private fun formatTs(ms: Long, srt: Boolean): String =
        SubtitleExportFormatter.formatSubtitleTimestamp(ms, srtStyle = srt)
}
