package com.android.purebilibili.core.ui.adaptive

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.android.purebilibili.core.util.WindowWidthSizeClass

data class DeviceUiProfile(
    val widthSizeClass: WindowWidthSizeClass,
    val isTablet: Boolean,
    val motionTier: MotionTier,
)

fun resolveDeviceUiProfile(
    widthSizeClass: WindowWidthSizeClass,
    performanceClass: DevicePerformanceClass = DevicePerformanceClass.Standard,
): DeviceUiProfile {
    val spec = resolveDeviceUiProfileSpec(
        widthClass = widthSizeClass.toAdaptiveWidthClass(),
        performanceClass = performanceClass,
    )
    return DeviceUiProfile(
        widthSizeClass = widthSizeClass,
        isTablet = spec.isTablet,
        motionTier = spec.motionTier,
    )
}

/**
 * Composable 场景下的标准入口：自动融合 [LocalDevicePerformanceClass]。
 *
 * 新代码一律用本函数替代 `remember(widthSizeClass) { resolveDeviceUiProfile(...) }`
 * 的手写组合，否则会漏掉性能分档信号，低配设备拿不到初始降级。
 */
@Composable
fun rememberDeviceUiProfile(widthSizeClass: WindowWidthSizeClass): DeviceUiProfile {
    val performanceClass = LocalDevicePerformanceClass.current
    return remember(widthSizeClass, performanceClass) {
        resolveDeviceUiProfile(
            widthSizeClass = widthSizeClass,
            performanceClass = performanceClass,
        )
    }
}

internal fun WindowWidthSizeClass.toAdaptiveWidthClass(): AdaptiveWidthClass = when (this) {
    WindowWidthSizeClass.Compact -> AdaptiveWidthClass.Compact
    WindowWidthSizeClass.Medium -> AdaptiveWidthClass.Medium
    WindowWidthSizeClass.Expanded -> AdaptiveWidthClass.Expanded
    WindowWidthSizeClass.Large -> AdaptiveWidthClass.Large
    WindowWidthSizeClass.ExtraLarge -> AdaptiveWidthClass.ExtraLarge
}
