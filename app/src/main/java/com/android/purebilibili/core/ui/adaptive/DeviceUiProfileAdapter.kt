package com.android.purebilibili.core.ui.adaptive

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.platform.LocalContext
import com.android.purebilibili.core.store.motion.MotionTierOverride
import com.android.purebilibili.core.store.motion.MotionTierOverrideStore
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
 * 用户手动覆盖与自动分档的融合规则：显式选择永远赢。
 * - Smooth 锁 Reduced；
 * - Standard 锁 Normal（低配设备上选它=接受以动效换观感）；
 * - Auto 回落到设备检测结果。
 */
fun applyMotionTierOverride(
    baseTier: MotionTier,
    override: MotionTierOverride,
): MotionTier = when (override) {
    MotionTierOverride.Auto -> baseTier
    MotionTierOverride.Smooth -> MotionTier.Reduced
    MotionTierOverride.Standard -> MotionTier.Normal
}

/**
 * Composable 场景下的标准入口：设备性能档 ⊕ 用户动效覆盖，双信号融合。
 *
 * 新代码一律用本函数替代 `remember(widthSizeClass) { resolveDeviceUiProfile(...) }`
 * 的手写组合，否则会漏掉性能分档/手动覆盖信号，低配设备拿不到初始降级。
 */
@Composable
fun rememberDeviceUiProfile(widthSizeClass: WindowWidthSizeClass): DeviceUiProfile {
    val performanceClass = LocalDevicePerformanceClass.current
    val context = LocalContext.current
    val motionTierOverride by MotionTierOverrideStore.observeOverride(context)
        .collectAsStateWithLifecycle(initialValue = MotionTierOverride.Auto)
    return remember(widthSizeClass, performanceClass, motionTierOverride) {
        val profile = resolveDeviceUiProfile(
            widthSizeClass = widthSizeClass,
            performanceClass = performanceClass,
        )
        profile.copy(
            motionTier = applyMotionTierOverride(profile.motionTier, motionTierOverride),
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
