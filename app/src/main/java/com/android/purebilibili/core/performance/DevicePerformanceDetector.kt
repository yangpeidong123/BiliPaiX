package com.android.purebilibili.core.performance

import android.app.ActivityManager
import android.content.Context
import com.android.purebilibili.core.ui.adaptive.DevicePerformanceClass

/**
 * 设备性能分档检测——[DevicePerformanceClass] 的唯一生产来源。
 *
 * 只用稳定、官方的信号，避免误判：
 * 1. [ActivityManager.isLowRamDevice]：Android 官方低配标志（Go 设备 / ≤2GB），直接 Low；
 * 2. 总内存（[ActivityManager.MemoryInfo.totalMem]）：<3.5GB Low、<6GB Standard；
 * 3. CPU 核心数：≤4 核基本是老设备，按 Low 处理。
 *
 * 检测在进程生命周期内只做一次（结果由调用方缓存），信号都是静态硬件属性，
 * 不存在运行期波动。
 */
object DevicePerformanceDetector {

    fun detect(context: Context): DevicePerformanceClass {
        val activityManager = context.getSystemService(ActivityManager::class.java)
            ?: return DevicePerformanceClass.Standard
        if (activityManager.isLowRamDevice) {
            return DevicePerformanceClass.Low
        }
        val totalMemMb = runCatching {
            val info = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(info)
            info.totalMem / (1024L * 1024L)
        }.getOrDefault(Long.MAX_VALUE)
        val cores = Runtime.getRuntime().availableProcessors()
        return classify(totalMemMb = totalMemMb, cores = cores)
    }

    /**
     * 纯分类逻辑，供单测覆盖边界。阈值取整百并留安全余量：
     * 3.5GB 档把「标称 4GB 实际可用更少」的机型划入 Low；
     * 6GB 是当前主流分界，低于它不主动降级（交给运行时守卫兜底）。
     */
    internal fun classify(totalMemMb: Long, cores: Int): DevicePerformanceClass = when {
        totalMemMb < 3_500L || cores <= MIN_CORES_FOR_STANDARD -> DevicePerformanceClass.Low
        totalMemMb < 6_000L -> DevicePerformanceClass.Standard
        else -> DevicePerformanceClass.High
    }

    private const val MIN_CORES_FOR_STANDARD = 4
}
