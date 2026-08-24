// 文件路径: core/network/ApiClient.kt
package com.android.purebilibili.core.network

import android.content.Context
import com.android.purebilibili.BuildConfig
import com.android.purebilibili.core.network.policy.HomeFeedAnonymizerRuntime
import com.android.purebilibili.core.network.policy.resolveHardcodedDnsFallback
import com.android.purebilibili.core.network.policy.resolveHomeFeedCookieAnonymizerDecision
import com.android.purebilibili.core.network.policy.shouldEnableTrustAllCertificates
import com.android.purebilibili.core.store.TokenManager
import com.android.purebilibili.core.store.AccountSessionStore
import com.android.purebilibili.core.store.StoredAccountSession
import com.android.purebilibili.data.model.response.*
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.QueryMap
import java.net.Proxy
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.UUID
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

internal const val BANGUMI_PLAY_URL_PATH = "pgc/player/web/v2/playurl"
internal const val BANGUMI_PLAY_URL_LEGACY_PATH = "pgc/player/web/playurl"
internal const val FORCE_COOKIE_HEADER = "X-BiliPai-Force-Cookie"

internal fun applyForcedCookieHeader(request: okhttp3.Request): okhttp3.Request {
    val forcedCookie = request.header(FORCE_COOKIE_HEADER) ?: return request
    return request.newBuilder()
        .header("Cookie", forcedCookie)
        .removeHeader(FORCE_COOKIE_HEADER)
        .build()
}

private class AppSessionCookieJar : okhttp3.CookieJar {
    private val cookieLock = Any()
    private val cookieStore = mutableMapOf<String, MutableList<okhttp3.Cookie>>()

    override fun saveFromResponse(url: okhttp3.HttpUrl, cookies: List<okhttp3.Cookie>) {
        val host = url.host
        synchronized(cookieLock) {
            val existingCookies = cookieStore.getOrPut(host) { mutableListOf() }
            cookies.forEach { newCookie ->
                existingCookies.removeAll { it.name == newCookie.name }
                existingCookies.add(newCookie)
                com.android.purebilibili.core.util.Logger.d("CookieJar", " Saved cookie: ${newCookie.name} for $host")
            }
        }
    }

    override fun loadForRequest(url: okhttp3.HttpUrl): List<okhttp3.Cookie> {
        if (resolveHomeFeedCookieAnonymizerDecision(
                pluginEnabled = HomeFeedAnonymizerRuntime.enabled,
                host = url.host,
                encodedPath = url.encodedPath
            )
        ) {
            com.android.purebilibili.core.util.Logger.d(
                "CookieJar",
                " 初见推荐匿名化首页推荐请求: ${url.encodedPath}, clearCookieHeader=true"
            )
            return emptyList()
        }

        val cookies = mutableListOf<okhttp3.Cookie>()

        synchronized(cookieLock) {
            cookieStore[url.host]?.let { cookies.addAll(it) }
        }

        var buvid3 = TokenManager.buvid3Cache
        if (buvid3.isNullOrEmpty()) {
            buvid3 = UUID.randomUUID().toString() + "infoc"
            TokenManager.buvid3Cache = buvid3
        }
        if (cookies.none { it.name == "buvid3" }) {
            cookies.add(
                okhttp3.Cookie.Builder()
                    .domain(url.host)
                    .name("buvid3")
                    .value(buvid3)
                    .build()
            )
        }

        val biliBiliDomain = if (url.host.endsWith("bilibili.com")) "bilibili.com" else url.host
        val sessData = TokenManager.sessDataCache
        if (!sessData.isNullOrEmpty()) {
            cookies.removeAll { it.name == "SESSDATA" }
            cookies.add(
                okhttp3.Cookie.Builder()
                    .domain(biliBiliDomain)
                    .name("SESSDATA")
                    .value(sessData)
                    .build()
            )
        }

        val biliJct = TokenManager.csrfCache
        if (!biliJct.isNullOrEmpty()) {
            cookies.removeAll { it.name == "bili_jct" }
            cookies.add(
                okhttp3.Cookie.Builder()
                    .domain(biliBiliDomain)
                    .name("bili_jct")
                    .value(biliJct)
                    .build()
            )
        }

        TokenManager.midCache?.takeIf { it > 0L }?.let { mid ->
            if (cookies.none { it.name == "DedeUserID" }) {
                cookies.add(
                    okhttp3.Cookie.Builder()
                        .domain(biliBiliDomain)
                        .name("DedeUserID")
                        .value(mid.toString())
                        .build()
                )
            }
        }

        if (url.encodedPath.contains("playurl") || url.encodedPath.contains("pgc/view")) {
            com.android.purebilibili.core.util.Logger.d(
                "CookieJar",
                " ${url.encodedPath} request: domain=$biliBiliDomain, hasSess=${!sessData.isNullOrEmpty()}, hasCsrf=${!biliJct.isNullOrEmpty()}"
            )
        }

        return cookies
    }

    fun clear() {
        synchronized(cookieLock) {
            cookieStore.clear()
        }
    }
}

/** A cookie jar isolated from the main account, used only for playback authorization. */
private class PlaybackAccountCookieJar(account: StoredAccountSession) : okhttp3.CookieJar {
    private val cookieLock = Any()
    private val cookies = mutableMapOf(
        "SESSDATA" to account.sessData,
        "bili_jct" to account.csrf,
        "DedeUserID" to account.mid.toString(),
        "buvid3" to account.buvid3
    ).filterValues { it.isNotBlank() }.toMutableMap()

    override fun saveFromResponse(url: okhttp3.HttpUrl, responseCookies: List<okhttp3.Cookie>) {
        synchronized(cookieLock) {
            responseCookies.forEach { cookie -> cookies[cookie.name] = cookie.value }
        }
    }

    override fun loadForRequest(url: okhttp3.HttpUrl): List<okhttp3.Cookie> {
        val domain = if (url.host.endsWith("bilibili.com")) "bilibili.com" else url.host
        return synchronized(cookieLock) {
            cookies.map { (name, value) ->
                okhttp3.Cookie.Builder().domain(domain).name(name).value(value).build()
            }
        }
    }
}

/**
 * Bilibili 主 API 接口
 * 
 * 功能模块分区:
 * - 用户信息 (L30-45): getNavInfo, getNavStat, getHistoryList, getFavFolders, getFavoriteList
 * - 推荐/热门 (L50-70): getRecommendParams, getPopularVideos, getRegionVideos
 * - 直播 (L75-140): getLiveList, getFollowedLive, getLivePlayUrl 等
 * - 视频播放 (L145-185): getVideoInfo, getPlayUrl, getDanmakuXml 等
 * - 评论 (L195-225): getReplyList, getEmotes, getReplyReply
 * - 用户交互 (L230-295): 点赞/投币/收藏/关注 等
 * - 稍后再看 (L300-320): getWatchLaterList, addToWatchLater, deleteFromWatchLater
 */
object NetworkModule {
    internal var appContext: Context? = null
    private val appSessionCookieJar = AppSessionCookieJar()
    private var playbackAccountKey: String? = null
    private var playbackAccountApi: BilibiliApi? = null
    private var playbackAccountBangumiApi: BangumiApi? = null

    fun init(context: Context) {
        appContext = context.applicationContext
        com.android.purebilibili.core.store.NetworkProxyStore.init(context.applicationContext)
    }

    fun clearRuntimeCookies() {
        appSessionCookieJar.clear()
    }

    @Synchronized
    fun clearPlaybackAccountClient() {
        playbackAccountKey = null
        playbackAccountApi = null
        playbackAccountBangumiApi = null
    }

    fun playbackAccount(): StoredAccountSession? =
        appContext?.let(AccountSessionStore::getPlaybackAccount)

    /**
     * Returns a client scoped to the optional playback account. It never changes
     * the app's primary account or grants entitlements locally: Bilibili still
     * decides access from the selected account's own server-side session.
     */
    @Synchronized
    fun playbackApi(): BilibiliApi {
        val context = appContext ?: return api
        val account = AccountSessionStore.getPlaybackAccount(context) ?: return api
        if (account.mid == TokenManager.midCache) return api

        val key = "${account.mid}:${account.sessData.hashCode()}:${account.buvid3.hashCode()}"
        ensurePlaybackAccountClients(account, key)
        return requireNotNull(playbackAccountApi)
    }

    @Synchronized
    fun playbackBangumiApi(): BangumiApi {
        val context = appContext ?: return bangumiApi
        val account = AccountSessionStore.getPlaybackAccount(context) ?: return bangumiApi
        if (account.mid == TokenManager.midCache) return bangumiApi

        val key = "${account.mid}:${account.sessData.hashCode()}:${account.buvid3.hashCode()}"
        ensurePlaybackAccountClients(account, key)
        return requireNotNull(playbackAccountBangumiApi)
    }

    private fun ensurePlaybackAccountClients(account: StoredAccountSession, key: String) {
        if (playbackAccountKey == key && playbackAccountApi != null && playbackAccountBangumiApi != null) {
            return
        }
        val client = okHttpClient.newBuilder()
            .cookieJar(PlaybackAccountCookieJar(account))
            .build()
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.bilibili.com/")
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
        playbackAccountApi = retrofit.create(BilibiliApi::class.java)
        playbackAccountBangumiApi = retrofit.create(BangumiApi::class.java)
        playbackAccountKey = key
    }

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    internal fun resolveSharedNetworkProtocols(): List<Protocol> {
        return listOf(Protocol.HTTP_2, Protocol.HTTP_1_1)
    }

    internal fun resolveApiHttpCacheBudgetBytes(): Long {
        return 32L * 1024 * 1024
    }

    internal fun resolveAndroidHdLoginAppKeyHeader(encodedPath: String): String? {
        return when (encodedPath) {
            "/x/passport-login/sms/send",
            "/x/passport-login/login/sms",
            "/x/passport-login/oauth2/login",
            "/x/passport-login/oauth2/access_token",
            "/x/safecenter/user/info",
            "/x/safecenter/captcha/pre",
            "/x/safecenter/common/sms/send",
            "/x/safecenter/login/tel/verify" -> "android_hd"
            else -> null
        }
    }

    /**
     * Dynamic proxy: app HTTP proxy when enabled, else system proxy / VPN.
     * Read live from [NetworkProxyStore] so toggle works without rebuilding clients.
     */
    internal fun buildAppProxySelector(): java.net.ProxySelector {
        return object : java.net.ProxySelector() {
            override fun select(uri: java.net.URI?): List<Proxy> {
                val settings = com.android.purebilibili.core.store.NetworkProxyStore.getSync()
                val systemProxies = runCatching {
                    getDefault()?.select(uri).orEmpty()
                }.getOrDefault(emptyList())
                return com.android.purebilibili.core.network.policy.selectAppHttpProxies(
                    settings = settings,
                    systemProxies = systemProxies,
                )
            }

            override fun connectFailed(
                uri: java.net.URI?,
                sa: java.net.SocketAddress?,
                ioe: java.io.IOException?,
            ) {
                com.android.purebilibili.core.util.Logger.w(
                    "ApiClient",
                    "Proxy connect failed uri=$uri sa=$sa: ${ioe?.message}"
                )
            }
        }
    }

    internal fun buildPlaybackOkHttpClient(sharedClient: OkHttpClient): OkHttpClient {
        return sharedClient.newBuilder()
            // Media playback should not inherit device-local proxy apps that may expose
            // an unavailable loopback port and break streaming with ECONNREFUSED.
            .proxy(Proxy.NO_PROXY)
            .build()
    }

    val okHttpClient: OkHttpClient by lazy {
        val builder = OkHttpClient.Builder()
            .protocols(resolveSharedNetworkProtocols())
            .proxySelector(buildAppProxySelector())
            //  [新增] 超时配置，提高网络稳定性
            .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(20, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
            //  [性能优化] HTTP 磁盘缓存 - 10MB，减少重复请求
            .cache(okhttp3.Cache(
                directory = java.io.File(appContext?.cacheDir ?: java.io.File("/tmp"), "okhttp_cache"),
                maxSize = resolveApiHttpCacheBudgetBytes()
            ))
            //  [性能优化] 连接池优化 - 保持更多空闲连接
            .connectionPool(okhttp3.ConnectionPool(
                maxIdleConnections = 10,
                keepAliveDuration = 5,
                timeUnit = java.util.concurrent.TimeUnit.MINUTES
            ))
            //  [新增] 自动重试和重定向
            .retryOnConnectionFailure(true)
            .followRedirects(true)
            .followSslRedirects(true)
        
        if (shouldEnableTrustAllCertificates(BuildConfig.DEBUG)) {
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            })
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, SecureRandom())
            builder.sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
                .hostnameVerifier { _, _ -> true }
        }
        
        builder
            //  [Fix] 自定义 DNS 实现，绕过 OkHttp 可能被混淆内部类的问题，并添加日志
            .dns(object : okhttp3.Dns {
                override fun lookup(hostname: String): List<java.net.InetAddress> {
                try {
                    val addresses = java.net.InetAddress.getAllByName(hostname).toList()
                    com.android.purebilibili.core.util.Logger.d("ApiClient", "DNS resolved: $hostname -> $addresses")
                    return addresses
                } catch (e: Exception) {
                    com.android.purebilibili.core.util.Logger.e("ApiClient", "DNS failed for $hostname: ${e.message}")
                    val fallback = resolveHardcodedDnsFallback(
                        hostname = hostname,
                        allowHardcodedIpFallback = BuildConfig.ALLOW_HARDCODED_DNS_FALLBACK
                    )
                    if (fallback != null) {
                        com.android.purebilibili.core.util.Logger.w(
                            "ApiClient",
                            "⚠️ Using Hardcoded IP for ${fallback.description}: ${fallback.ipAddress}"
                        )
                        return listOf(java.net.InetAddress.getByName(fallback.ipAddress))
                    }
                    throw e
                }
            }})
            //  [关键] 添加 CookieJar 自动管理 Cookie（参考 PiliPala）
            .cookieJar(appSessionCookieJar)
            .addInterceptor { chain ->
                val original = chain.request()
                val url = original.url
                var referer = "https://www.bilibili.com"
                
                //  如果请求中包含 bvid，构造更具体的 Referer (解决 412 问题)
                val bvid = url.queryParameter("bvid")
                if (!bvid.isNullOrEmpty()) {
                    referer = "https://www.bilibili.com/video/$bvid"
                }
                
                //  如果是 Space API 请求，使用 space.bilibili.com 作为 Referer
                val mid = url.queryParameter("mid") ?: url.queryParameter("vmid")
                if (url.encodedPath.contains("/x/space/") && !mid.isNullOrEmpty()) {
                    referer = "https://space.bilibili.com/$mid"
                }
                
                //  [新增] 直播 API Referer 处理
                if (url.host == "api.live.bilibili.com") {
                    val roomId = url.queryParameter("room_id") ?: url.queryParameter("id")
                    referer = if (!roomId.isNullOrEmpty()) {
                        "https://live.bilibili.com/$roomId"
                    } else {
                        "https://live.bilibili.com"
                    }
                }
                
                //  [修复] 弹幕 API 需要使用视频页面作为 Referer (解决 412 问题)
                if (url.encodedPath.contains("/dm/list.so") || url.encodedPath.contains("/x/v1/dm/")) {
                    referer = "https://www.bilibili.com/video/"
                }

                //  [修复] 动态接口使用动态页 Referer/Origin，降低 412 触发概率
                val isDynamicEndpoint = url.encodedPath.contains("/x/polymer/web-dynamic/") ||
                    url.encodedPath.contains("/x/dynamic/")
                if (isDynamicEndpoint) {
                    referer = "https://t.bilibili.com/"
                }

                val isFavoriteEndpoint = url.encodedPath.contains("/x/v3/fav/") ||
                    url.encodedPath.contains("/x/space/fav/")
                if (isFavoriteEndpoint) {
                    val mediaId = url.queryParameter("media_id")
                    val favoriteMid = url.queryParameter("up_mid")
                        ?: TokenManager.midCache?.takeIf { it > 0L }?.toString()
                    referer = when {
                        // 新版收藏夹内容页已迁移到 /list/ml...；Referer 与 Origin 必须同源。
                        !mediaId.isNullOrEmpty() ->
                            "https://www.bilibili.com/list/ml$mediaId"
                        !favoriteMid.isNullOrEmpty() ->
                            "https://space.bilibili.com/$favoriteMid/favlist"
                        else -> "https://space.bilibili.com/"
                    }
                }

                var origin = "https://www.bilibili.com"
                if (url.host == "api.live.bilibili.com") {
                    origin = "https://live.bilibili.com"
                }
                if (isDynamicEndpoint) {
                    origin = "https://t.bilibili.com"
                }
                if (isFavoriteEndpoint) {
                    origin = if (url.queryParameter("media_id").isNullOrEmpty()) {
                        "https://space.bilibili.com"
                    } else {
                        "https://www.bilibili.com"
                    }
                }

                val androidHdLoginAppKeyHeader = resolveAndroidHdLoginAppKeyHeader(url.encodedPath)
                val loginBuvid = original.header("X-BiliPai-Login-Buvid")
                //  合并模式 App 半边(匿名 android_hd 取流)同样需要 HD 身份头(UA/app-key/buvid),
                //  仅当请求带 mobi_app=android_hd 时命中, 不影响原 TV 取流(mobi_app=android)
                val isHdFeedRequest = url.encodedPath == "/x/v2/feed/index" &&
                    url.queryParameter("mobi_app") == "android_hd"
                val isAndroidHdLoginEndpoint = androidHdLoginAppKeyHeader != null || isHdFeedRequest
                val explicitReferer = original.header("Referer")
                val builder = original.newBuilder()
                    .header(
                        "User-Agent",
                        if (isAndroidHdLoginEndpoint) {
                            "Mozilla/5.0 BiliDroid/2.0.1 (bbcallen@gmail.com) os/android model/android_hd mobi_app/android_hd build/2001100 channel/master innerVer/2001100 osVer/15 network/2"
                        } else {
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36"
                        }
                    )
                if (!isAndroidHdLoginEndpoint && explicitReferer.isNullOrBlank()) {
                    builder.header("Origin", origin) //  动态 Origin 头
                }
                if (androidHdLoginAppKeyHeader != null || isHdFeedRequest) {
                    builder
                        .header("app-key", androidHdLoginAppKeyHeader ?: "android_hd")
                        .header("buvid", loginBuvid ?: TokenManager.buvid3Cache.orEmpty())
                        .removeHeader("X-BiliPai-Login-Buvid")
                        .header("bili-http-engine", "cronet")
                        .header("env", "prod")
                        .header("x-bili-trace-id", "11111111111111111111111111111111:1111111111111111:0:0")
                }
                if (androidHdLoginAppKeyHeader != null) {
                    // Match PiliPlus LoginHttp.headers exactly for Passport App requests.
                    builder
                        .header("x-bili-aurora-eid", "")
                        .header("x-bili-aurora-zone", "")
                        .header("Content-Type", "application/x-www-form-urlencoded; charset=utf-8")
                }
                
                //  [关键修复] WBI 签名接口绝对不能设置 Referer 头，否则会失败
                // 参考：https://github.com/SocialSisterYi/bilibili-API-collect/blob/master/docs/misc/sign/wbi.md
                val isWbiEndpoint = url.encodedPath.contains("/wbi/")
                if (explicitReferer.isNullOrBlank() &&
                    !isWbiEndpoint && !isAndroidHdLoginEndpoint
                ) {
                    builder.header("Referer", referer)
                }

                val request = builder.build()
                com.android.purebilibili.core.util.Logger.d(
                    "ApiClient",
                    " Sending request to ${original.url}, Referer: ${request.header("Referer") ?: "OMITTED"}, hasSess=${!TokenManager.sessDataCache.isNullOrEmpty()}, hasCsrf=${!TokenManager.csrfCache.isNullOrEmpty()}"
                )

                try {
                    val response = chain.proceed(request)
                    com.android.purebilibili.core.util.Logger.d(
                        "ApiClient",
                        " Network protocol: ${response.protocol} ${request.url.host}${request.url.encodedPath}"
                    )
                    if (response.code >= 500 || response.code == 429 || response.code == 412) {
                        com.android.purebilibili.core.util.CrashReporter.reportApiError(
                            endpoint = "${request.method} ${request.url.encodedPath}",
                            httpCode = response.code,
                            errorMessage = "HTTP ${response.code}"
                        )
                    }
                    response
                } catch (e: Exception) {
                    if (com.android.purebilibili.core.util.shouldReportApiFailure(
                            callCanceled = chain.call().isCanceled(),
                            throwable = e
                        )
                    ) {
                        com.android.purebilibili.core.util.CrashReporter.reportApiError(
                            endpoint = "${request.method} ${request.url.encodedPath}",
                            httpCode = -1,
                            errorMessage = e.message ?: e.javaClass.simpleName
                        )
                    }
                    throw e
                }
            }
            // CookieJar runs after application interceptors and replaces Cookie.
            // Restore an explicit imported cookie after that, matching PiliPlus.
            .addNetworkInterceptor { chain ->
                chain.proceed(applyForcedCookieHeader(chain.request()))
            }
            .build()
    }

    val playbackOkHttpClient: OkHttpClient by lazy {
        buildPlaybackOkHttpClient(okHttpClient)
    }
    
    //  [新增] Guest OkHttpClient - 不带登录凭证，用于风控时的降级
    // 当登录用户遭遇风控 (-351) 时，可以尝试以游客身份获取视频
    val guestOkHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .protocols(resolveSharedNetworkProtocols())
            .proxySelector(buildAppProxySelector())
            .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(20, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .followRedirects(true)
            .followSslRedirects(true)
            //  CookieJar 使用全新的 buvid3，不复用可能被污染的 buvid3Cache
            .cookieJar(object : okhttp3.CookieJar {
                // 为 guest 模式生成独立的 buvid3，避免复用被风控的 buvid3
                private val guestBuvid3: String by lazy { 
                    UUID.randomUUID().toString().replace("-", "") + "infoc"
                }
                
                override fun saveFromResponse(url: okhttp3.HttpUrl, cookies: List<okhttp3.Cookie>) {
                    // 不保存任何 cookie
                }
                
                override fun loadForRequest(url: okhttp3.HttpUrl): List<okhttp3.Cookie> {
                    val cookies = mutableListOf<okhttp3.Cookie>()
                    
                    //  使用全新生成的 guestBuvid3，不使用 TokenManager.buvid3Cache
                    cookies.add(okhttp3.Cookie.Builder()
                        .domain(url.host)
                        .name("buvid3")
                        .value(guestBuvid3)
                        .build())
                    
                    com.android.purebilibili.core.util.Logger.d(
                        "GuestCookieJar",
                        " ${url.encodedPath} request: guest mode with fresh buvid3=${guestBuvid3.take(15)}..."
                    )
                    
                    return cookies
                }
            })
            .addInterceptor { chain ->
                val original = chain.request()
                val url = original.url
                var referer = "https://www.bilibili.com"
                
                val bvid = url.queryParameter("bvid")
                if (!bvid.isNullOrEmpty()) {
                    referer = "https://www.bilibili.com/video/$bvid"
                }

                val isWbiEndpoint = url.encodedPath.contains("/wbi/")
                val builder = original.newBuilder()
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36")
                    .header("Origin", "https://www.bilibili.com")

                if (!isWbiEndpoint) {
                    builder.header("Referer", referer)
                }
                
                val request = builder.build()
                try {
                    val response = chain.proceed(request)
                    com.android.purebilibili.core.util.Logger.d(
                        "ApiClient",
                        " Guest network protocol: ${response.protocol} ${request.url.host}${request.url.encodedPath}"
                    )
                    if (response.code >= 500 || response.code == 429 || response.code == 412) {
                        com.android.purebilibili.core.util.CrashReporter.reportApiError(
                            endpoint = "guest ${request.method} ${request.url.encodedPath}",
                            httpCode = response.code,
                            errorMessage = "HTTP ${response.code}"
                        )
                    }
                    response
                } catch (e: Exception) {
                    if (com.android.purebilibili.core.util.shouldReportApiFailure(
                            callCanceled = chain.call().isCanceled(),
                            throwable = e
                        )
                    ) {
                        com.android.purebilibili.core.util.CrashReporter.reportApiError(
                            endpoint = "guest ${request.method} ${request.url.encodedPath}",
                            httpCode = -1,
                            errorMessage = e.message ?: e.javaClass.simpleName
                        )
                    }
                    throw e
                }
            }
            .build()
    }
    
    //  [新增] Guest API - 使用 guestOkHttpClient，用于风控降级
    val guestApi: BilibiliApi by lazy {
        Retrofit.Builder().baseUrl("https://api.bilibili.com/").client(guestOkHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType())).build()
            .create(BilibiliApi::class.java)
    }

    val api: BilibiliApi by lazy {
        Retrofit.Builder().baseUrl("https://api.bilibili.com/").client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType())).build()
            .create(BilibiliApi::class.java)
    }
    val passportApi: PassportApi by lazy {
        Retrofit.Builder().baseUrl("https://passport.bilibili.com/").client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType())).build()
            .create(PassportApi::class.java)
    }
    val searchApi: SearchApi by lazy {
        Retrofit.Builder().baseUrl("https://api.bilibili.com/").client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType())).build()
            .create(SearchApi::class.java)
    }

    val articleApi: ArticleApi by lazy {
        Retrofit.Builder().baseUrl("https://api.bilibili.com/").client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType())).build()
            .create(ArticleApi::class.java)
    }
    
    //  动态 API
    val dynamicApi: DynamicApi by lazy {
        Retrofit.Builder().baseUrl("https://api.bilibili.com/").client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType())).build()
            .create(DynamicApi::class.java)
    }
    
    //  Buvid API (用于获取设备指纹)
    val buvidApi: BuvidApi by lazy {
        Retrofit.Builder().baseUrl("https://api.bilibili.com/").client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType())).build()
            .create(BuvidApi::class.java)
    }
    
    //  [新增] UP主空间 API
    val spaceApi: SpaceApi by lazy {
        Retrofit.Builder().baseUrl("https://api.bilibili.com/").client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType())).build()
            .create(SpaceApi::class.java)
    }
    
    //  [新增] 番剧/影视 API
    val bangumiApi: BangumiApi by lazy {
        Retrofit.Builder().baseUrl("https://api.bilibili.com/").client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType())).build()
            .create(BangumiApi::class.java)
    }
    
    //  [新增] 故事模式 (竖屏短视频) API - 使用 app.bilibili.com
    val storyApi: StoryApi by lazy {
        Retrofit.Builder().baseUrl("https://app.bilibili.com/").client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType())).build()
            .create(StoryApi::class.java)
    }
    
    //  [新增] 开屏/壁纸 API
    val splashApi: SplashApi by lazy {
        Retrofit.Builder().baseUrl("https://app.bilibili.com/").client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType())).build()
            .create(SplashApi::class.java)
    }
    
    //  [新增] 私信 API - 使用 api.vc.bilibili.com
    val messageApi: MessageApi by lazy {
        Retrofit.Builder().baseUrl("https://api.vc.bilibili.com/").client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType())).build()
            .create(MessageApi::class.java)
    }
    
    //  [新增] 音频 API - 使用 www.bilibili.com
    val audioApi: AudioApi by lazy {
        Retrofit.Builder().baseUrl("https://www.bilibili.com/").client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType())).build()
            .create(AudioApi::class.java)
    }
}
