package com.android.purebilibili.core.ui.adaptive

enum class AdaptiveWidthClass {
    Compact,
    Medium,
    Expanded,
    Large,
    ExtraLarge,
}

data class DeviceUiProfileSpec(
    val isTablet: Boolean,
    val motionTier: MotionTier,
)

fun resolveDeviceUiProfileSpec(
    widthClass: AdaptiveWidthClass,
    performanceClass: DevicePerformanceClass = DevicePerformanceClass.Standard,
): DeviceUiProfileSpec {
    val motionTier = resolveMotionTierForDevice(
        widthClass = widthClass,
        performanceClass = performanceClass,
    )

    return DeviceUiProfileSpec(
        isTablet = widthClass != AdaptiveWidthClass.Compact,
        motionTier = motionTier,
    )
}
