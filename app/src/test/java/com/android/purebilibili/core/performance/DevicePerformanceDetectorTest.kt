package com.android.purebilibili.core.performance

import com.android.purebilibili.core.ui.adaptive.DevicePerformanceClass
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * [DevicePerformanceDetector.classify] 的边界测试。
 *
 * 阈值语义：3.5GB / 4 核以下是 Low；6GB 是 Standard/High 分界。
 * 检测误判方向必须保守——宁可把中配机划进 Standard，也不能把低配机划出 Low。
 */
class DevicePerformanceDetectorTest {

    @Test
    fun veryLowMemoryIsLow() {
        assertEquals(
            DevicePerformanceClass.Low,
            DevicePerformanceDetector.classify(totalMemMb = 2_000L, cores = 8),
        )
    }

    @Test
    fun memoryJustBelowThresholdIsLow() {
        assertEquals(
            DevicePerformanceClass.Low,
            DevicePerformanceDetector.classify(totalMemMb = 3_499L, cores = 8),
        )
    }

    @Test
    fun memoryAtLowThresholdIsStandard() {
        assertEquals(
            DevicePerformanceClass.Standard,
            DevicePerformanceDetector.classify(totalMemMb = 3_500L, cores = 8),
        )
    }

    @Test
    fun midRangeMemoryIsStandard() {
        assertEquals(
            DevicePerformanceClass.Standard,
            DevicePerformanceDetector.classify(totalMemMb = 4_000L, cores = 8),
        )
        assertEquals(
            DevicePerformanceClass.Standard,
            DevicePerformanceDetector.classify(totalMemMb = 5_500L, cores = 8),
        )
    }

    @Test
    fun highEndMemoryIsHigh() {
        assertEquals(
            DevicePerformanceClass.High,
            DevicePerformanceDetector.classify(totalMemMb = 8_000L, cores = 8),
        )
    }

    @Test
    fun fewCoresForceLowRegardlessOfMemory() {
        assertEquals(
            DevicePerformanceClass.Low,
            DevicePerformanceDetector.classify(totalMemMb = 8_000L, cores = 3),
        )
        assertEquals(
            DevicePerformanceClass.Standard,
            DevicePerformanceDetector.classify(totalMemMb = 8_000L, cores = 4),
        )
    }

    @Test
    fun memoryQueryFailureFallsBackToConservativeDefault() {
        // totalMemMb=Long.MAX_VALUE 模拟 getMemoryInfo 失败：不能因信号缺失而降级
        assertEquals(
            DevicePerformanceClass.High,
            DevicePerformanceDetector.classify(totalMemMb = Long.MAX_VALUE, cores = 8),
        )
    }
}
