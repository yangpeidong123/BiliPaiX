package com.android.purebilibili.core.store.motion

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.android.purebilibili.core.store.settingsDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * 用户对动效档位的手动覆盖。
 *
 * - [Auto]：跟随设备性能分档（[com.android.purebilibili.core.ui.adaptive.DevicePerformanceClass]）；
 * - [Smooth]：流畅优先，锁 [com.android.purebilibili.core.ui.adaptive.MotionTier.Reduced]；
 * - [Standard]：锁 [com.android.purebilibili.core.ui.adaptive.MotionTier.Normal]，
 *   低配设备上选择此项即表示接受以动效换观感（运行时守卫仍会在持续掉帧时兜底降级）。
 */
enum class MotionTierOverride(val value: Int) {
    Auto(0),
    Smooth(1),
    Standard(2);

    companion object {
        fun fromValue(value: Int?): MotionTierOverride =
            entries.firstOrNull { it.value == value } ?: Auto
    }
}

internal val motionTierOverridePreferencesKey =
    intPreferencesKey("motion_tier_override")

object MotionTierOverrideStore {
    fun observeOverride(context: Context): Flow<MotionTierOverride> =
        context.settingsDataStore.data
            .map { preferences ->
                MotionTierOverride.fromValue(preferences[motionTierOverridePreferencesKey])
            }
            .distinctUntilChanged()

    suspend fun setOverride(context: Context, override: MotionTierOverride) {
        context.settingsDataStore.edit { preferences ->
            preferences[motionTierOverridePreferencesKey] = override.value
        }
    }
}
