package com.android.purebilibili.core.store

import android.content.Context
import com.android.purebilibili.core.util.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * SharedPreferences 后台预热器——*Sync 读路径的功耗/卡顿治理核心。
 *
 * 背景：SettingsManager 的 45 个 `getXxxSync` 走 SharedPreferences 内存快照，
 * 双写 setter 保证它与 DataStore 同步。SP 的唯一阻塞点是「每个文件进程内首次
 * 访问时的磁盘加载」——若第一次读取发生在主线程启动路径，会产生几十毫秒级卡顿。
 *
 * 方案：Application.onCreate 尽早在 Default 调度器上把全部 18 个 SP 文件 touch
 * 一遍，磁盘加载在后台完成；此后主线程上所有 *Sync 调用都是纯内存读。
 * 103 个调用点零改动、行为零变化。
 *
 * 维护约定：本清单必须与 SettingsManager 中 getSharedPreferences("...") 的
 * 文件名保持一致；新增 SP 文件时两处同步更新。
 */
object SettingsPreferencesWarmer {

    private const val TAG = "SettingsPrefsWarmer"

    private val PREF_FILE_NAMES = listOf(
        "mini_player",
        "quality_settings",
        "auto_play_cache",
        "feed_api",
        "video_overlay_cache",
        "privacy_mode",
        "haptic_cache",
        "easter_egg",
        "download_prefs",
        "data_saver",
        "comment_settings",
        "auto_rotate_cache",
        "app_icon_cache",
        "theme_cache",
        "portrait_fullscreen_cache",
        "image_save_prefs",
        "crash_tracking",
        "analytics_tracking",
    )

    fun warmAsync(context: Context) {
        val appContext = context.applicationContext
        // 一次性预热任务：SupervisorJob 隔离失败，跑完即结束，不持有长生命周期引用
        val warmScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        warmScope.launch {
            PREF_FILE_NAMES.forEach { fileName ->
                runCatching {
                    // getSharedPreferences 即触发后台加载；.all 强制等它在本协程完成
                    appContext.getSharedPreferences(fileName, Context.MODE_PRIVATE).all
                }.onFailure { throwable ->
                    Logger.w(TAG, "Failed to warm prefs file: $fileName", throwable)
                }
            }
            Logger.d(TAG, "Warmed ${PREF_FILE_NAMES.size} preference files off the main thread")
        }
    }
}
