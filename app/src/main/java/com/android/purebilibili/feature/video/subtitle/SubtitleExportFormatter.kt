package com.android.purebilibili.feature.video.subtitle

import java.util.Locale

/**
 * 字幕导出格式化——把 B 站 JSON cue 列表转成标准 WebVTT / SRT 文本。
 *
 * 与 PiliPlus 的 json2Vtt/json2Srt 对齐；单轨导出，双语轨道的合并交由调用方
 * 先行构造（保持本对象为无状态纯函数，便于测试）。
 */
object SubtitleExportFormatter {

    /** 导出格式。 */
    enum class SubtitleExportFormat { WEBVTT, SRT }

    fun formatSubtitles(
        cues: List<SubtitleCue>,
        format: SubtitleExportFormat,
        videoTitle: String? = null,
    ): String = buildString {
        if (format == SubtitleExportFormat.WEBVTT) {
            append("WEBVTT")
            appendLine()
            // WebVTT 允许可选的 NOTE 元数据块，记录来源便于归档
            val title = videoTitle?.trim().orEmpty()
            if (title.isNotEmpty()) {
                append("NOTE title: ")
                append(title.replace('\n', ' '))
                appendLine()
            }
            appendLine()
        }
        val visibleCues = cues.filter { it.endMs > it.startMs }
        visibleCues.forEachIndexed { index, cue ->
            append(index + 1)
            appendLine()
            append(formatSubtitleTimestamp(cue.startMs, srtStyle = format == SubtitleExportFormat.SRT))
            append(" --> ")
            append(formatSubtitleTimestamp(cue.endMs, srtStyle = format == SubtitleExportFormat.SRT))
            appendLine()
            append(cue.content)
            appendLine()
            appendLine()
        }
    }.trimEnd() + "\n"

    /**
     * 时间戳：WebVTT 用 `.` 分隔毫秒，SRT 强制 `,`。
     * 超 99 小时截断到小时两位（实际视频不可能达到）。
     */
    internal fun formatSubtitleTimestamp(ms: Long, srtStyle: Boolean): String {
        val safeMs = ms.coerceAtLeast(0L)
        val hours = safeMs / 3_600_000L
        val minutes = safeMs % 3_600_000L / 60_000L
        val seconds = safeMs % 60_000L / 1_000L
        val millis = safeMs % 1_000L
        val separator = if (srtStyle) "," else "."
        return String.format(
            Locale.US,
            "%02d:%02d:%02d%s%03d",
            hours.coerceAtMost(99L),
            minutes,
            seconds,
            separator,
            millis,
        )
    }
}
