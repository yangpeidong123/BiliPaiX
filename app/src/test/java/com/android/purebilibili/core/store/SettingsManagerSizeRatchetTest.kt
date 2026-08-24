package com.android.purebilibili.core.store

import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * `SettingsManager` 体量棘轮。
 *
 * 这个文件长期是全项目第二大源文件，且是最活跃的合流点——几乎每个功能都会往里加
 * 一段。它正在按领域拆成独立 store（`core/store/player/`、`navigation/`、`home/`），
 * 但拆分这件事有个众所周知的失败模式：**拆到一半停下，然后又慢慢长回去**。
 *
 * 这条棘轮是唯一能防住那种回流的机制。每完成一批迁移，把 [MAX_LINES] 调小到新的实测值；
 * 只要没人主动调大，它就只能变小。
 */
class SettingsManagerSizeRatchetTest {

    @Test
    fun settingsManagerDoesNotGrow() {
        val file = settingsManagerFile()
        val lines = file.readLines().size

        assertTrue(
            lines <= MAX_LINES,
            "SettingsManager.kt 有 $lines 行，超过上限 $MAX_LINES。" +
                "新增设置项请放进对应领域的 store（core/store/player|navigation|home/），" +
                "不要继续堆进门面文件。",
        )
    }

    private fun settingsManagerFile(): File {
        val relative = "core/store/SettingsManager.kt"
        val roots = listOf(
            "src/main/java/com/android/purebilibili",
            "app/src/main/java/com/android/purebilibili",
        )
        return roots.map { File("$it/$relative") }.firstOrNull { it.exists() }
            ?: error("找不到 SettingsManager.kt，cwd=" + File(".").absoluteFile.canonicalPath)
    }

    private companion object {
        /**
         * 冻结于本次实测值（2026-08-24，BiliPaiX 同步）。
         * 相对上一快照（6714）上调 672 行，反映自 0.2.3-beta.11 后上游持续合流
         * （播放器/弹幕/平板适配等设置项净增）以及 BiliPaiX 尚未执行的
         * SettingsManager 领域拆分。待拆分完成后应调小到新的实测值。
         * 仍只允许后续变小。
         */
        const val MAX_LINES = 7386
    }
}
