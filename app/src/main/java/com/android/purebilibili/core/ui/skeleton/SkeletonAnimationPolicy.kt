package com.android.purebilibili.core.ui.skeleton

import android.content.Context
import android.os.PowerManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * 骨架屏动画策略——系统低功耗（省电模式）下停掉无限脉冲动画。
 *
 * 背景：弱网时骨架屏可能长时间挂在屏幕上，`rememberInfiniteTransition`
 * 每帧重绘 shimmer/pulse；系统省电模式本就要求降低视觉负载，此时把骨架屏
 * 降级为静态色块是零配置的合规做法。
 *
 * 门控值按进程生命周期缓存：用户开关系统省电后，下一次进入加载态生效，
 * 不监听广播（避免为低频场景引入 receiver 开销）。
 */
const val SKELETON_STATIC_PULSE = 0.5f

@Composable
fun rememberSkeletonAnimationEnabled(): Boolean {
    val context = LocalContext.current
    return remember {
        val powerManager =
            context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        powerManager?.isPowerSaveMode != true
    }
}
