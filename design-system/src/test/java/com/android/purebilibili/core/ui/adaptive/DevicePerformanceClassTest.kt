package com.android.purebilibili.core.ui.adaptive

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * 设备性能分档的裁决规则测试。
 *
 * 关键约束：性能档只做「降级方向」——Low 强制 Reduced，Standard/High
 * 维持原有按宽度结论；任何让 High 比 Standard 更激进的改动都应在此失败。
 */
class DevicePerformanceClassTest {

    @Test
    fun lowPerformanceForcesReducedTierOnEveryWidthClass() {
        AdaptiveWidthClass.entries.forEach { widthClass ->
            assertEquals(
                MotionTier.Reduced,
                resolveMotionTierForDevice(widthClass, DevicePerformanceClass.Low),
                "低配设备在 $widthClass 下也必须初始即 Reduced",
            )
        }
    }

    @Test
    fun standardPerformanceKeepsLegacyWidthOnlyBehavior() {
        assertEquals(
            MotionTier.Normal,
            resolveMotionTierForDevice(AdaptiveWidthClass.Compact, DevicePerformanceClass.Standard),
        )
        assertEquals(
            MotionTier.Normal,
            resolveMotionTierForDevice(AdaptiveWidthClass.Medium, DevicePerformanceClass.Standard),
        )
        assertEquals(
            MotionTier.Enhanced,
            resolveMotionTierForDevice(AdaptiveWidthClass.Expanded, DevicePerformanceClass.Standard),
        )
    }

    @Test
    fun highPerformanceMatchesStandardSoDetectionErrorCannotUpgrade() {
        AdaptiveWidthClass.entries.forEach { widthClass ->
            assertEquals(
                resolveMotionTierForDevice(widthClass, DevicePerformanceClass.Standard),
                resolveMotionTierForDevice(widthClass, DevicePerformanceClass.High),
                "High 与 Standard 必须同档：检测误判不能放大动效",
            )
        }
    }

    @Test
    fun performanceEnumOrderIsConservativeFirst() {
        assertTrue(
            DevicePerformanceClass.Low.ordinal < DevicePerformanceClass.Standard.ordinal &&
                DevicePerformanceClass.Standard.ordinal < DevicePerformanceClass.High.ordinal,
            "枚举顺序被重排会破坏 when 分支语义与文档约定",
        )
    }
}
