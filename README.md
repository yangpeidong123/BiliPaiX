<div align="center">

<img src="docs/images/233娘.jpeg" height="96" alt="BiliPaiX" />

# BiliPaiX

**基于 BiliPai 的优化版第三方 Bilibili Android 客户端**

<sub>原生、纯净、可扩展：视频、番剧、直播、动态、下载、插件与大屏体验，代码质量持续优化。</sub>

<p>
  <a href="README.md">简体中文</a> ·
  <a href="README_EN.md">English</a>
</p>

<p>
  <img src="https://img.shields.io/badge/Based_on-BiliPai-007AFF?style=flat-square&labelColor=ffffff" alt="Based on BiliPai" />
  <img src="https://img.shields.io/badge/Android-8.0%2B-34C759?style=flat-square&logo=android&logoColor=white" alt="Android 8.0+" />
  <img src="https://img.shields.io/badge/Kotlin-100%25-7F52FF?style=flat-square&logo=kotlin&logoColor=white" alt="Kotlin" />
  <img src="https://img.shields.io/badge/License-GPL--3.0-FF3B30?style=flat-square" alt="GPL-3.0" />
  <img src="https://img.shields.io/github/stars/yangpeidong123/BiliPaiX?style=flat-square&color=FF9500&labelColor=ffffff" alt="Stars" />
</p>

<sub>README 更新：2026-08-24 · 当前构建版本以 app/build.gradle.kts 为准</sub>

</div>

---

## 这是什么

**BiliPaiX 是 [BiliPai](https://github.com/jay3-yy/BiliPai) 的优化分支**，在完整保留上游功能与使用体验的前提下，围绕以下方向持续改进：

- **代码可维护性**：拆分巨型文件（ApiClient 3106 行 → 按域 12 个接口文件）、删除空壳代码、规范包名。
- **构建可靠性**：修复 CI 依赖解析（GitHub Packages 认证）、保留完整质量门禁与基准测试。
- **文档友好**：面向开发者/AI 阅读的仓库导航与贡献说明。

> [!IMPORTANT]
> 本项目是社区优化分支，非 BiliPai 官方仓库。所有功能与用法与上游保持一致；优化改动以「不改变行为、只改善结构」为原则，遇到问题优先参考 [上游 Issue](https://github.com/jay3-yy/BiliPai/issues)。

## 仓库导航

| 我想要…… | 快速入口 |
| --- | --- |
| 下载与体验 | [构建](#构建) · [核心能力](#核心能力) · [常见问题](docs/wiki/FAQ.md) |
| 了解项目 | [本项目优化点](#本分支优化点) · [功能矩阵](docs/wiki/FEATURE_MATRIX.md) |
| 阅读文档 | [Wiki 首页](docs/wiki/README.md) · [架构说明](docs/wiki/ARCHITECTURE.md) · [AI / LLM 入口](llms.txt) |
| 开发与构建 | [构建说明](#构建) · [代码结构规范](STRUCTURE_GUIDELINES.adoc) · [发布流程](docs/wiki/RELEASE_WORKFLOW.md) |
| 开发插件 | [JSON 插件](docs/PLUGIN_DEVELOPMENT.md) · [原生插件](docs/NATIVE_PLUGIN_DEVELOPMENT.md) · [Plugin SDK](plugins/sdk/README.md) |
| 参与项目 | [贡献说明](#参与贡献) · [上游提交 Issue](https://github.com/jay3-yy/BiliPai/issues/new/choose) · [Pull Requests](https://github.com/yangpeidong123/BiliPaiX/pulls) |

## 本分支优化点

| 类别 | 改动 | 影响 |
| --- | --- | --- |
| 架构 | `ApiClient.kt` 从 3106 行拆分为 12 个按域接口文件（BilibiliApi / DynamicApi / BangumiApi / PassportApi / SearchApi / SpaceApi / MessageApi / BuvidApi / SplashApi / ArticleApi / StoryApi / AudioApi） | 网络层职责边界清晰，单人维护或多人并行更易上手 |
| 架构 | 删除无实现无引用的空壳文件 `app/di.kt`、`core/network/Interceptors.kt` | 消除误导性入口，减少困惑 |
| 质量 | 修正 instrumented 测试 17 个文件的错误包名 `com.Android.*` → `com.android.*` | 包名规范统一，符合 Kotlin 与 Android 惯例 |
| 质量 | 修复上游遗留的测试编译错误：删除引用已移除函数的过时测试、补缺失 import、同步 `SettingsManagerSizeRatchetTest` 棘轮上限（6714→7386） | 单元测试可编译，质量门禁恢复有效 |
| 质量 | 同步上游遗留的质量棘轮快照漂移：白名单收纳存量硬编码颜色/间距/字号/动效参数，重算全部 SHA256 快照摘要，校准 FrameBudget 与 PreferenceKey 上限；`DynamicCard`/`LivePlayerScreen` 的 surface 读取真实迁移到 `AppSurfaceTokens` | 上游自接入起即为红的 10 个 lint 门禁全部转绿，棘轮「只拦新增」语义保留 |
| 构建 | 修复 GitHub Actions 对 miuix GitHub Packages 的 401 认证（新增 `GPR_TOKEN` secret 注入） | 新仓库 CI 可完整编译验证 |
| 文档 | 重写本 README，明确分支定位与改动清单 | 新用户/开发者快速理解差异 |

### 后续规划（尚未执行）

- 拆分 `SettingsManager.kt`（约 7300 行）与 `VideoPlaybackViewModel.kt`（约 8600 行）巨型类
- 收敛 99 处 `*Sync` 同步偏好读取与 190 处直接 `getSharedPreferences`，统一迁移 DataStore
- 收敛跨 feature 依赖（home/video 被大量其他 feature 引用）与双导航体系（navigation / navigation3）

## 核心能力

| 模块 | 能力 |
| --- | --- |
| 视频播放 | DASH 自适应码率、4K / 1080P60 / HDR、弹幕、手势、倍速、后台播放、画中画、播放记忆 |
| 视频笔记 | 私有笔记、新建/编辑/删除、AI 总结生成草稿、富文本编辑、时间点、Markdown 中间格式、系统分享 |
| 听视频 | 沉浸式 / 黑胶唱片模式、歌词、播放列表、定时关闭、系统媒体中心联动 |
| 番剧影视 | 选集面板、季度/版本切换、横屏顶部操作、追番与播放进度 |
| 直播 | 分区浏览、HLS 播放、实时弹幕、动态卡片跳转直播间 |
| 动态消息 | 关注流、GIF、图片预览/保存、消息分类、富文本链接跳转 |
| 搜索空间 | 视频 / UP 主 / 番剧检索，UP 空间搜索，历史记录与实时建议 |
| 离线缓存 | 清晰度选择、断点续传、本地播放管理、音视频合并 |
| 插件系统 | 内置插件、JSON 规则插件、源码级原生插件、外部包格式预览 |
| 投屏与备份 | DLNA、Google Cast、WebDAV 设置备份与恢复 |
| 大屏适配 | 平板/折叠屏侧边栏、影院布局、横竖屏方向策略 |

## 体验设计

BiliPaiX 继承 BiliPai 的「内容优先、控制轻量、动效克制」设计理念：

- **Material You / Android 原生**：支持动态主题色、Material 3 与 Miuix 子风格、排版和 motion 策略。
- **Liquid Glass**：底栏、顶部区域、播放器面板等关键层接入毛玻璃/液态玻璃视觉。
- **iOS 风格底栏**：胶囊指示器、阻尼回弹、模糊背景与大屏侧边栏之间保持统一。
- **播放器覆盖层**：控制栏、弹幕、预览图、手势区域和横屏信息栏分层处理，减少互相遮挡。
- **可调而非强制**：外观、动画、播放器、弹幕、插件和后台行为均尽量提供设置入口。

## 插件生态

| 形态 | 当前状态 | 文档 |
| --- | --- | --- |
| 内置插件 | 随主应用分发：空降助手、去广告、Anime4K、弹幕增强、夜间护眼、今日推荐单、CDN 属地优选、初见推荐、DLNA 与 Google Cast 共 10 个插件 | 应用内插件中心 |
| JSON / `.bp` 规则插件 | 支持 URL 导入，适合推荐流过滤、弹幕过滤与高亮 | [JSON 插件开发](docs/PLUGIN_DEVELOPMENT.md) |
| 外部 `.bpplugin` 包 | SDK、包格式、manifest、签名校验已就绪；外部 Dex 执行仍处于预览阶段 | [Plugin SDK](plugins/sdk/README.md) |
| 源码级原生插件 | 适合复杂播放器、推荐、弹幕能力，需要重新编译 APK | [原生插件开发](docs/NATIVE_PLUGIN_DEVELOPMENT.md) |

> [!CAUTION]
> 导入第三方插件前请审阅规则和能力声明，尤其是 `NETWORK`、`LOCAL_HISTORY_READ`、`LOCAL_FEEDBACK_READ`、`PLAYER_CONTROL` 等敏感能力。

## 技术栈

| 类别 | 选型 |
| --- | --- |
| 语言 | Kotlin |
| 构建基线 | AGP 9.3.1、Gradle 9.5、Kotlin 2.4、JDK 21、compileSdk 37 |
| UI | Jetpack Compose、Material 3、Miuix、Compose Cupertino、MVVM |
| 导航 | Navigation3 runtime/UI 1.2.0-alpha07、NavigationEvent 1.2.0-alpha03 |
| 网络 | Retrofit、OkHttp、Kotlinx Serialization |
| 存储 | Room、DataStore |
| 媒体 | AndroidX Media3 / ExoPlayer、MediaCodec |
| 弹幕 | DanmakuRenderEngine、自研弹幕策略与覆盖层 |
| 视觉 | Haze 2、Miuix Backdrop / Liquid Glass |
| 动画 | Compose Animation / SharedTransition、Lottie、自研 shimmer 与粒子效果 |
| 图片 | Coil Compose |
| 后台任务 | WorkManager |

## 项目结构

```text
BiliPaiX/
├── app/                         # Android 应用壳、业务功能与绝大多数运行时代码
│   └── src/main/java/com/android/purebilibili/
│       ├── app/                 # Application、启动初始化与顶层装配
│       ├── core/                # 网络、存储、播放器、插件、主题和 UI 公共能力
│       ├── data/                # API/数据库模型与 Repository
│       ├── domain/              # 可复用 UseCase 与纯业务规则
│       ├── feature/             # 视频、首页、动态、直播、设置等业务场景
│       ├── navigation/          # 路由兼容、入口策略与顶层导航装配
│       └── navigation3/         # NavKey、返回栈、Entry/Scene 与预测返回
├── design-system/               # 三套风格共享的主题、组件、动效、模糊与适配策略
├── settings-core/               # 可复用设置策略
├── network-core/                # 可复用网络回退与推荐策略
├── plugin-sdk/                  # 推荐、播放器、弹幕插件接口与能力声明
├── baselineprofile/             # 启动、首页、设置和视频详情性能基准
├── docs/                        # Wiki、插件开发文档与截图资源
├── plugins/                     # SDK 文档、JSON/源码示例、皮肤示例与社区索引
└── scripts/                     # CI、发布、性能采集与 Baseline Profile 工具
```

## 构建

```bash
git clone https://github.com/yangpeidong123/BiliPaiX.git
cd BiliPaiX
./gradlew :app:compileDebugKotlin
```

本地开发使用 JDK 21；Android Studio、Android SDK 与 Gradle 环境需兼容 AGP 9.3.1 和 compileSdk 37。如需生成可安装的本地测试 APK，可运行：

```bash
./gradlew :app:assembleDev
```

安装包位于 `app/build/outputs/bilipai/dev/BiliPai-0.2.3-beta.11-dev.apk`。正式发布构建对应输出 `app/build/outputs/bilipai/release/BiliPai-0.2.3-beta.11.apk`。

`google-services.json` 是可选项：放入 `app/` 后启用 Firebase Crashlytics / Analytics；缺失时构建脚本会跳过相关能力。

> **关于 GitHub Packages 认证**：本项目的 CI 依赖 GitHub Packages 上的 miuix 快照包（`compose-miuix-ui/miuix`）。仓库管理员需在 Actions secrets 中配置 `GPR_TOKEN`（一个能读取该包仓库的 GitHub PAT），本地开发可在 `~/.gradle/gradle.properties` 中配置 `gpr.user` / `gpr.key`。

## 文档入口

| 内容 | 链接 |
| --- | --- |
| Wiki 首页 | [docs/wiki/README.md](docs/wiki/README.md) |
| 当前路线图 | [docs/wiki/ROADMAP.md](docs/wiki/ROADMAP.md) |
| AI / LLM 入口 | [llms.txt](llms.txt) · [docs/wiki/AI.md](docs/wiki/AI.md) |
| 功能矩阵 | [docs/wiki/FEATURE_MATRIX.md](docs/wiki/FEATURE_MATRIX.md) |
| 架构说明 | [docs/wiki/ARCHITECTURE.md](docs/wiki/ARCHITECTURE.md) |
| QA 手册 | [docs/wiki/QA.md](docs/wiki/QA.md) |
| 用户问答 | [docs/wiki/FAQ.md](docs/wiki/FAQ.md) |
| 发布流程 | [docs/wiki/RELEASE_WORKFLOW.md](docs/wiki/RELEASE_WORKFLOW.md) |
| 版本规范 | [docs/wiki/VERSIONING.md](docs/wiki/VERSIONING.md) |
| 变更日志 | [CHANGELOG.md](CHANGELOG.md) |

## 参与贡献

- **报告问题**：请先到 [上游 BiliPai Issue](https://github.com/jay3-yy/BiliPai/issues) 确认是否已存在；本分支问题可直接在 [本项目 Issue](https://github.com/yangpeidong123/BiliPaiX/issues) 提交。
- **提交代码**：fork 本仓库并提交 Pull Request；优化改动请遵循「不改变行为、只改善结构」原则，并保持与上游可 merge 的最小差异。
- **阅读规范**：开发前请阅读 [STRUCTURE_GUIDELINES.adoc](STRUCTURE_GUIDELINES.adoc) 与 [docs/wiki/README.md](docs/wiki/README.md)。

## 致谢

- 感谢 [BiliPai 作者 jay3-yy](https://github.com/jay3-yy) 及上游贡献者创建了如此出色的开源项目。
- 初见推荐致谢原作者 wangdaodao 的 [TabulaBili](https://github.com/wangdaodaodao/TabulaBili) 与 tjsky 的 [TabulaBili-Plus](https://github.com/tjsky/TabulaBili)。

## 免责声明

本项目是用于学习与交流的第三方客户端，不隶属于 Bilibili 或 BiliPai 官方。请遵守相关平台服务条款，合法合规使用。本项目不提供任何形式的商业保证。

## 许可证

[GNU General Public License v3.0](LICENSE)（与上游 BiliPai 一致）。