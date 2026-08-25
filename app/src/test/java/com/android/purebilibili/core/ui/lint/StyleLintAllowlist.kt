package com.android.purebilibili.core.ui.lint

/**
 * Frozen allowlist of feature files that currently contain hardcoded
 * RoundedCornerShape(N) / tween(N) / spring(N) / MaterialTheme.colorScheme.surface
 * usage. Each entry must be removed once the corresponding feature is migrated
 * to AppShapes / AppMotionTokens / AppSurfaceTokens, so the lint tests block
 * the next regression.
 *
 * Adding a new path here is a documented exception, not a default. A PR adding
 * a new entry should explain the pixel-level reason in the description.
 */
internal object StyleLintAllowlist {

    /** Feature prefixes already covered by typography, color, and spacing lint. */
    val MIGRATED_TOKEN_PREFIXES: Set<String> = setOf(
        "src/main/java/com/android/purebilibili/feature/live/",
        "src/main/java/com/android/purebilibili/feature/home/",
        "src/main/java/com/android/purebilibili/feature/dynamic/",
        "src/main/java/com/android/purebilibili/feature/following/",
        "src/main/java/com/android/purebilibili/feature/list/",
        "src/main/java/com/android/purebilibili/feature/watchlater/",
    )

    /** 迁移到 AppShapes / MaterialTheme.shapes 后从本表移除. */
    val SHAPE_HITS: Set<String> = setOf(
        // empty — all feature digit-literal shapes migrated
    )

    /**
     * 已纳管 feature 前缀下的存量颜色字面量（棘轮上限见 StyleLintAllowlistRatchetTest）。
     *
     * 这些是接入 lint 前的历史存量，且多数有像素级理由不能换主题色：
     * 直播 SuperChat 弹层按 B 站设计为深色卡片，黑/白字是固定品牌色，
     * 换成主题色会在浅色模式下失去对比度。
     */
    val COLOR_HITS: Set<String> = setOf(
        "src/main/java/com/android/purebilibili/feature/live/components/LiveSendDanmakuSheet.kt",
        "src/main/java/com/android/purebilibili/feature/live/components/LiveSuperChatFlashOverlay.kt",
        // BiliPaiX 收纳上游接入棘轮后新增的存量硬编码颜色（非 4dp 刻度/品牌色）：
        // ImagePreviewDialog 预览遮罩、BottomBar/FloatingBottomBar/FloatingDockChrome
        // 的玻璃背景高光与品牌红点、LiquidGlassAdaptiveReadability 的对比兜底色。
        "src/main/java/com/android/purebilibili/feature/dynamic/components/ImagePreviewDialog.kt",
        "src/main/java/com/android/purebilibili/feature/home/components/BottomBar.kt",
        "src/main/java/com/android/purebilibili/feature/home/components/FloatingBottomBar.kt",
        "src/main/java/com/android/purebilibili/feature/home/components/FloatingDockChrome.kt",
        "src/main/java/com/android/purebilibili/feature/home/components/LiquidGlassAdaptiveReadability.kt",
    )

    /**
     * 已纳管 feature 前缀下的存量布局尺寸字面量（棘轮上限见 StyleLintAllowlistRatchetTest）。
     *
     * 数值不在 AppSpacingTokens 的 4dp 刻度上（1/20/22/36/52/64/96/420/520dp），
     * 强制取整会改变既有像素布局；等对应 feature 迁移到命名 Spec 后移除。
     */
    val SPACING_HITS: Set<String> = setOf(
        "src/main/java/com/android/purebilibili/feature/dynamic/components/DynamicCard.kt",
        "src/main/java/com/android/purebilibili/feature/dynamic/components/DynamicFeedSkeletonCard.kt",
        "src/main/java/com/android/purebilibili/feature/home/components/HomeHeader.kt",
        "src/main/java/com/android/purebilibili/feature/live/LiveHomeSelectableChip.kt",
        "src/main/java/com/android/purebilibili/feature/live/LiveListScreen.kt",
        "src/main/java/com/android/purebilibili/feature/live/components/LiveStreamSourceSheet.kt",
        "src/main/java/com/android/purebilibili/feature/live/components/LiveSuperChatFlashOverlay.kt",
           // BiliPaiX 收纳上游接入棘轮后新增的存量非刻度间距（不在 4dp 刻度上，
        // 强制取整会改变已有像素布局；等对应 feature 迁移到命名 Spec 后移除）。
        "src/main/java/com/android/purebilibili/feature/dynamic/DynamicScreen.kt",
        "src/main/java/com/android/purebilibili/feature/dynamic/components/DynamicCreateVoteDialog.kt",
        "src/main/java/com/android/purebilibili/feature/dynamic/components/DynamicPublishComposer.kt",
        "src/main/java/com/android/purebilibili/feature/home/components/CrashTrackingConsentDialog.kt",
        "src/main/java/com/android/purebilibili/feature/home/components/FloatingBottomBar.kt",
        "src/main/java/com/android/purebilibili/feature/home/components/FloatingDockChrome.kt",
        "src/main/java/com/android/purebilibili/feature/home/components/MineSideDrawer.kt",
        "src/main/java/com/android/purebilibili/feature/list/CommonListScreen.kt",
        "src/main/java/com/android/purebilibili/feature/watchlater/WatchLaterScreen.kt",
    )

    /** 已纳管 feature 前缀下的存量排版字面量（棘轮上限见 StyleLintAllowlistRatchetTest）。 */
    val TYPOGRAPHY_HITS: Set<String> = setOf(
        "src/main/java/com/android/purebilibili/feature/live/components/LiveSuperChatFlashOverlay.kt",
        // BiliPaiX 收纳上游接入棘轮后的存量 sp 字面量（动态模块侧边栏箭头、
        // 投票/评论面板与发布器的特殊密度文字），迁移到 MaterialTheme.typography 后移除。
        "src/main/java/com/android/purebilibili/feature/dynamic/DynamicScreen.kt",
        "src/main/java/com/android/purebilibili/feature/dynamic/components/DynamicCommentSheet.kt",
        "src/main/java/com/android/purebilibili/feature/dynamic/components/DynamicCreateVoteDialog.kt",
        "src/main/java/com/android/purebilibili/feature/dynamic/components/DynamicPublishComposer.kt",
        "src/main/java/com/android/purebilibili/feature/dynamic/components/DynamicSidebar.kt",
    )

    /** 迁移到 AppMotionTokens 后从本表移除. */
    val MOTION_HITS: Set<String> = setOf(
        "src/main/java/com/android/purebilibili/feature/login/LoginComponents.kt",
        "src/main/java/com/android/purebilibili/feature/login/LoginScreen.kt",
        "src/main/java/com/android/purebilibili/feature/onboarding/OnboardingBottomSheet.kt",
        "src/main/java/com/android/purebilibili/feature/search/SearchScreen.kt",
        "src/main/java/com/android/purebilibili/feature/settings/ui/CacheClearAnimation.kt",
        "src/main/java/com/android/purebilibili/feature/space/SpaceScreen.kt",
        "src/main/java/com/android/purebilibili/feature/video/screen/VideoContentSection.kt",
        "src/main/java/com/android/purebilibili/feature/video/ui/components/CelebrationAnimations.kt",
        "src/main/java/com/android/purebilibili/feature/video/ui/components/SponsorSkipUI.kt",
        "src/main/java/com/android/purebilibili/feature/video/ui/components/VideoCommentSheetHost.kt",
        "src/main/java/com/android/purebilibili/feature/video/ui/overlay/CommandDanmakuOverlay.kt",
        "src/main/java/com/android/purebilibili/feature/video/ui/overlay/FullscreenPlayerOverlay.kt",
        "src/main/java/com/android/purebilibili/feature/video/ui/overlay/VideoPlayerOverlay.kt",
        "src/main/java/com/android/purebilibili/feature/video/ui/pager/PortraitVideoPager.kt"
    )

    /**
     * 已纳管（migrated）feature 前缀下的存量字面动效参数（棘轮上限见
     * StyleLintAllowlistRatchetTest. MIGRATED_MOTION_HITS_SHA256）。
     *
     * 这些是 home 模块的皮肤/手势专用动效：BottomBarSkin 图标循环动画、
     * Liquid Glass 可读性过渡、miuix 阻尼拖拽 spring 曲线——数值为手感调校
     * 结果，没有 AppMotionTokens 中 1:1 的语义对应，强行映射会改变交互手感。
     * 等对应皮肤迁移到命名 Spec 后移除。
     */
    val MIGRATED_MOTION_HITS: Set<String> = setOf(
        "src/main/java/com/android/purebilibili/feature/home/components/BottomBarUiSkin.kt",
        "src/main/java/com/android/purebilibili/feature/home/components/FloatingBottomBar.kt",
        "src/main/java/com/android/purebilibili/feature/home/components/LiquidGlassAdaptiveReadability.kt",
        "src/main/java/com/android/purebilibili/feature/home/components/miuix/DampedDragAnimation.kt",
    )

    /** 迁移到 AppSurfaceTokens 后从本表移除. */
    val SURFACE_HITS: Set<String> = setOf(
        "src/main/java/com/android/purebilibili/feature/bangumi/BangumiDetailScreen.kt",
        "src/main/java/com/android/purebilibili/feature/bangumi/BangumiScreen.kt",
        "src/main/java/com/android/purebilibili/feature/category/CategoryScreen.kt",
        "src/main/java/com/android/purebilibili/feature/download/BatchDownloadDialog.kt",
        "src/main/java/com/android/purebilibili/feature/download/DirectorySelectionDialog.kt",
        "src/main/java/com/android/purebilibili/feature/download/DownloadListScreen.kt",
        "src/main/java/com/android/purebilibili/feature/download/DownloadQualityDialog.kt",
        "src/main/java/com/android/purebilibili/feature/message/ChatScreen.kt",
        "src/main/java/com/android/purebilibili/feature/onboarding/OnboardingBottomSheet.kt",
        "src/main/java/com/android/purebilibili/feature/onboarding/OnboardingScreen.kt",
        "src/main/java/com/android/purebilibili/feature/plugin/AdFilterPlugin.kt",
        "src/main/java/com/android/purebilibili/feature/plugin/EyeProtectionOverlay.kt",
        "src/main/java/com/android/purebilibili/feature/plugin/HomeFeedAnonymizerPlugin.kt",
        "src/main/java/com/android/purebilibili/feature/plugin/SponsorBlockPlugin.kt",
        "src/main/java/com/android/purebilibili/feature/profile/OfficialWallpaperSheet.kt",
        "src/main/java/com/android/purebilibili/feature/profile/ProfileScreen.kt",
        "src/main/java/com/android/purebilibili/feature/profile/SplashWallpaperPickerSheet.kt",
        "src/main/java/com/android/purebilibili/feature/profile/WallpaperAdjustmentSheet.kt",

        "src/main/java/com/android/purebilibili/feature/search/SearchScreen.kt",
        "src/main/java/com/android/purebilibili/feature/search/SearchTrendingScreen.kt",
        "src/main/java/com/android/purebilibili/feature/search/TopicDetailScreen.kt",
        "src/main/java/com/android/purebilibili/feature/settings/screen/PluginsScreen.kt",
        "src/main/java/com/android/purebilibili/feature/settings/ui/CacheClearAnimation.kt",
        "src/main/java/com/android/purebilibili/feature/space/SpaceScreen.kt",
        "src/main/java/com/android/purebilibili/feature/video/player/VideoPlayerComponents.kt",
        "src/main/java/com/android/purebilibili/feature/video/screen/TabletCinemaLayout.kt",
        "src/main/java/com/android/purebilibili/feature/video/screen/TabletVideoLayout.kt",
        "src/main/java/com/android/purebilibili/feature/video/screen/VideoContentSection.kt",
        "src/main/java/com/android/purebilibili/feature/video/screen/VideoDetailScreen.kt",
        "src/main/java/com/android/purebilibili/feature/video/ui/components/BottomInputBar.kt",
        "src/main/java/com/android/purebilibili/feature/video/ui/components/CollectionSheet.kt",
        "src/main/java/com/android/purebilibili/feature/video/ui/components/CommentInputBar.kt",
        "src/main/java/com/android/purebilibili/feature/video/ui/components/CommentInputDialog.kt",
        "src/main/java/com/android/purebilibili/feature/video/ui/components/DanmakuSendDialog.kt",
        "src/main/java/com/android/purebilibili/feature/video/ui/components/EmotePanelSheet.kt",
        "src/main/java/com/android/purebilibili/feature/video/ui/components/FavoriteFolderSheet.kt",
        "src/main/java/com/android/purebilibili/feature/video/ui/components/GlassComponents.kt",
        "src/main/java/com/android/purebilibili/feature/video/ui/components/InteractiveChoiceOverlay.kt",
        "src/main/java/com/android/purebilibili/feature/video/ui/components/RelatedVideoItem.kt",
        "src/main/java/com/android/purebilibili/feature/video/ui/components/ReplyComponents.kt",
        "src/main/java/com/android/purebilibili/feature/video/ui/components/SkeletonComponents.kt",
        "src/main/java/com/android/purebilibili/feature/video/ui/components/TwoFingerSpeedFeedbackOverlay.kt",
        "src/main/java/com/android/purebilibili/feature/video/ui/overlay/VideoPlayerOverlay.kt",
        "src/main/java/com/android/purebilibili/feature/video/ui/pager/PortraitDetailSheet.kt",
        "src/main/java/com/android/purebilibili/feature/video/ui/section/VideoInfoSection.kt",
        "src/main/java/com/android/purebilibili/feature/web/WebViewScreen.kt"
    )
}
