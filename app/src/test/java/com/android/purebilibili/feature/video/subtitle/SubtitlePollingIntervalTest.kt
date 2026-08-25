package com.android.purebilibili.feature.video.subtitle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * 字幕轮询事件化的功耗语义测试。
 *
 * 核心不变式：任何情况下返回值 ≤ 对应 fallback 周期——事件化只许更省，
 * 不许比固定轮询更迟钝。
 */
class SubtitlePollingIntervalTest {

    private fun cue(startMs: Long, endMs: Long) = SubtitleCue(
        startMs = startMs,
        endMs = endMs,
        content = "t",
    )

    @Test
    fun pausedPlaybackAlwaysUsesLegacyPausedInterval() {
        val cues = listOf(cue(0, 10_000))
        assertEquals(
            260L,
            resolveSubtitlePollingIntervalMs(
                primaryCues = cues,
                secondaryCues = emptyList(),
                positionMs = 5_000L,
                isPlaying = false,
            ),
        )
    }

    @Test
    fun noCuesUsesLowFrequencyProbe() {
        assertEquals(
            SUBTITLE_NO_CUES_POLL_INTERVAL_MS,
            resolveSubtitlePollingIntervalMs(
                primaryCues = emptyList(),
                secondaryCues = emptyList(),
                positionMs = 0L,
                isPlaying = true,
            ),
        )
    }

    @Test
    fun midSentenceCapsAtMaxActiveInterval() {
        // 长句中间：距边界远，休眠被夹到上限 300ms（原为 120ms → 省 ~60%）
        val cues = listOf(cue(1_000L, 31_000L))
        assertEquals(
            SUBTITLE_MAX_ACTIVE_POLL_INTERVAL_MS,
            resolveSubtitlePollingIntervalMs(
                primaryCues = cues,
                secondaryCues = emptyList(),
                positionMs = 2_000L,
                isPlaying = true,
            ),
        )
    }

    @Test
    fun nearBoundaryWakesEarlyWithMargin() {
        // 距句尾仅 100ms：提前 40ms 醒来切换文案，不越过边界
        val cues = listOf(cue(0L, 2_100L))
        assertEquals(
            60L,
            resolveSubtitlePollingIntervalMs(
                primaryCues = cues,
                secondaryCues = emptyList(),
                positionMs = 2_000L,
                isPlaying = true,
            ),
        )
    }

    @Test
    fun gapBetweenSenticesSleepsUntilNextStart() {
        // 句间空窗：睡到下一句开始前
        val cues = listOf(
            cue(0L, 1_000L),
            cue(3_000L, 4_000L),
        )
        assertEquals(
            SUBTITLE_MAX_ACTIVE_POLL_INTERVAL_MS,
            resolveSubtitlePollingIntervalMs(
                primaryCues = cues,
                secondaryCues = emptyList(),
                positionMs = 1_500L,
                isPlaying = true,
            ),
        )
    }

    @Test
    fun secondaryTrackBoundaryIsHonoredToo() {
        // 副轨更早有边界时以副轨为准（双语场景）
        val primary = listOf(cue(0L, 20_000L))
        val secondary = listOf(cue(0L, 2_500L))
        assertEquals(
            SUBTITLE_MAX_ACTIVE_POLL_INTERVAL_MS,
            resolveSubtitlePollingIntervalMs(
                primaryCues = primary,
                secondaryCues = secondary,
                positionMs = 1_000L,
                isPlaying = true,
            ),
        )
    }

    @Test
    fun neverExceedsLegacyFallbackPeriod() {
        // 不变式：任何位置都不超过 300ms 上限（= 永不比旧行为更迟钝）
        val cues = listOf(
            cue(0L, 1_000L),
            cue(1_200L, 60_000L),
        )
        for (position in 0L..61_000L step 137L) {
            val interval = resolveSubtitlePollingIntervalMs(
                primaryCues = cues,
                secondaryCues = emptyList(),
                positionMs = position,
                isPlaying = true,
            )
            assertTrue(
                interval in SUBTITLE_MIN_ACTIVE_POLL_INTERVAL_MS..
                    SUBTITLE_MAX_ACTIVE_POLL_INTERVAL_MS,
                "position=$position interval=$interval 越界",
            )
        }
    }

    @Test
    fun nextBoundaryBinarySearchFindsEarliestFutureEdge() {
        val cues = listOf(
            cue(0L, 900L),
            cue(1_000L, 1_900L),
            cue(5_000L, 6_000L),
        )
        // 在句中 → 返回本句结束
        assertEquals(900L, resolveNextSubtitleBoundaryMs(cues, 100L))
        // 在空窗 → 返回下一句开始
        assertEquals(1_000L, resolveNextSubtitleBoundaryMs(cues, 950L))
        // 全部播完 → null
        assertNull(resolveNextSubtitleBoundaryMs(cues, 9_999L))
        // 空轨道 → null
        assertNull(resolveNextSubtitleBoundaryMs(emptyList(), 100L))
    }
}
