package com.android.purebilibili.core.ui.adaptive

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * 设备性能分档——「主动分级」信号源。
 *
 * 与 [RuntimeVisualGuardDecision] 的运行时降级正交互补：
 * - 性能档在启动时检测一次，决定**初始**动效档位，让低配设备从第一帧起就避开
 *   重动效/重模糊路径，而不是先卡一顿、等守卫监测到掉帧才被动降级；
 * - 运行时守卫继续在其上兜底（掉帧仍会进一步降到 [MotionTier.Reduced]），
 *   两者取更保守者（见 [minMotionTier] / 各消费点的融合逻辑）。
 *
 * 分档只做「降级方向」：High 与 Standard 当前共享同一初始档位，
 * 预留给后续「高配增强」特性；这样检测误差不会误伤主流机型。
 */
enum class DevicePerformanceClass {
    /** 低配：官方 isLowRamDevice 命中、总内存不足或核心数过少。 */
    Low,

    /** 主流机型（默认值）。 */
    Standard,

    /** 高配，当前与 Standard 同档，仅作为信号保留。 */
    High,
}

/**
 * 启动时无法立刻完成 Context 检测前的保守默认：与旧行为完全一致
 * （只按屏幕宽度定档），保证 CompositionLocal 未 provide 时零回归。
 */
val LocalDevicePerformanceClass = staticCompositionLocalOf { DevicePerformanceClass.Standard }

/**
 * 设备动效初始档位的唯一裁决点。
 *
 * 规则（保守优先，只向下降）：
 * - 低配设备一律 [MotionTier.Reduced]——短时长、弱位移、低 blur 预算；
 * - 其余设备维持原有按宽度的结论：大屏 Enhanced、手机 Normal。
 */
fun resolveMotionTierForDevice(
    widthClass: AdaptiveWidthClass,
    performanceClass: DevicePerformanceClass,
): MotionTier = when (performanceClass) {
    DevicePerformanceClass.Low -> MotionTier.Reduced
    DevicePerformanceClass.Standard, DevicePerformanceClass.High ->
        if (widthClass >= AdaptiveWidthClass.Expanded) {
            MotionTier.Enhanced
        } else {
            MotionTier.Normal
        }
}
