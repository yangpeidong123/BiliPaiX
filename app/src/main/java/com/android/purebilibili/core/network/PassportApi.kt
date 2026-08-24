package com.android.purebilibili.core.network

import com.android.purebilibili.data.model.response.*
import kotlinx.serialization.Serializable
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap
import retrofit2.http.Url

interface PassportApi {
    @GET("https://api.bilibili.com/x/web-interface/nav")
    suspend fun validateCookieSession(
        @Header(FORCE_COOKIE_HEADER) cookieHeader: String
    ): NavResponse

    // 二维码登录
    @GET("x/passport-login/web/qrcode/generate")
    suspend fun generateQrCode(): QrCodeResponse

    @GET("x/passport-login/web/qrcode/poll")
    suspend fun pollQrCode(@Query("qrcode_key") key: String): Response<PollResponse>
    
    // ==========  极验验证 + 手机号/密码登录 ==========

    /**
     * 国际冠字码列表。
     * `id` → 短信接口 `cid`；`country_id` → 拨号区号（如 "86"）。
     * 文档：/docs/login/login_action/SMS.md
     */
    @GET("web/generic/country/list")
    suspend fun getCountryList(): PassportCountryListResponse
    
    // 获取极验验证参数 (gt, challenge, token)
    @GET("x/passport-login/captcha")
    suspend fun getCaptcha(
        @Query("source") source: String = "main_web"
    ): CaptchaResponse
    
    // 发送短信验证码
    @retrofit2.http.FormUrlEncoded
    @retrofit2.http.POST("x/passport-login/web/sms/send")
    suspend fun sendSmsCode(
        // cid = 国家列表 id（中国大陆 = 1），不是拨号区号 86
        @retrofit2.http.Field("cid") cid: Int = 1,
        @retrofit2.http.Field("tel") tel: String,              // 手机号
        @retrofit2.http.Field("source") source: String = "main_web",
        @retrofit2.http.Field("token") token: String,          // captcha token
        @retrofit2.http.Field("challenge") challenge: String,  // 极验 challenge
        @retrofit2.http.Field("validate") validate: String,    // 极验验证结果
        @retrofit2.http.Field("seccode") seccode: String       // 极验安全码
    ): SmsCodeResponse

    @retrofit2.http.FormUrlEncoded
    @retrofit2.http.POST("x/passport-login/sms/send")
    suspend fun sendSmsCodeByApp(
        @retrofit2.http.Header("X-BiliPai-Login-Buvid") loginBuvid: String,
        // Kotlin Map is Map<String, out String> without this; R8 can break FieldMap bodies in release.
        @retrofit2.http.FieldMap params: @JvmSuppressWildcards Map<String, String>
    ): SmsCodeResponse
    
    // 短信验证码登录
    @retrofit2.http.FormUrlEncoded
    @retrofit2.http.POST("x/passport-login/web/login/sms")
    suspend fun loginBySms(
        @retrofit2.http.Field("cid") cid: Int = 1,
        @retrofit2.http.Field("tel") tel: String,
        @retrofit2.http.Field("code") code: Int,                // 短信验证码
        @retrofit2.http.Field("source") source: String = "main_mini",
        @retrofit2.http.Field("captcha_key") captchaKey: String, // sendSmsCode 返回的 key
        @retrofit2.http.Field("keep") keep: Int = 0,
        @retrofit2.http.Field("go_url") goUrl: String = "https://www.bilibili.com"
    ): Response<LoginResponse>  // 使用 Response 以获取 Set-Cookie

    @retrofit2.http.FormUrlEncoded
    @retrofit2.http.POST("x/passport-login/login/sms")
    suspend fun loginBySmsApp(
        @retrofit2.http.Header("X-BiliPai-Login-Buvid") loginBuvid: String,
        @retrofit2.http.FieldMap params: @JvmSuppressWildcards Map<String, String>
    ): Response<LoginResponse>
    
    // 获取 RSA 公钥 (密码登录用)
    @GET("x/passport-login/web/key")
    suspend fun getWebKey(): WebKeyResponse
    
    // 密码登录
    @retrofit2.http.FormUrlEncoded
    @retrofit2.http.POST("x/passport-login/web/login")
    suspend fun loginByPassword(
        @retrofit2.http.Field("username") username: String,     // 手机号
        @retrofit2.http.Field("password") password: String,     // RSA 加密后的密码
        @retrofit2.http.Field("keep") keep: Int = 0,
        @retrofit2.http.Field("token") token: String,
        @retrofit2.http.Field("challenge") challenge: String,
        @retrofit2.http.Field("validate") validate: String,
        @retrofit2.http.Field("seccode") seccode: String,
        @retrofit2.http.Field("source") source: String = "main-fe-header",
        @retrofit2.http.Field("go_url") goUrl: String = "https://www.bilibili.com"
    ): Response<LoginResponse>

    @retrofit2.http.FormUrlEncoded
    @retrofit2.http.POST("x/passport-login/oauth2/login")
    suspend fun loginByPasswordApp(
        @retrofit2.http.Header("X-BiliPai-Login-Buvid") loginBuvid: String,
        @retrofit2.http.FieldMap params: @JvmSuppressWildcards Map<String, String>
    ): Response<LoginResponse>

    // ========== 密码登录风控（安全中心手机验证） ==========

    @GET("x/safecenter/user/info")
    suspend fun safeCenterGetInfo(
        @Query("tmp_code") tmpCode: String
    ): com.android.purebilibili.data.model.response.SafeCenterInfoResponse

    @retrofit2.http.POST("x/safecenter/captcha/pre")
    suspend fun safeCenterPreCapture(): com.android.purebilibili.data.model.response.SafeCenterPreCaptureResponse

    @retrofit2.http.FormUrlEncoded
    @retrofit2.http.POST("x/safecenter/common/sms/send")
    suspend fun safeCenterSendSms(
        @retrofit2.http.Header("Referer") referer: String,
        @retrofit2.http.FieldMap params: @JvmSuppressWildcards Map<String, String>
    ): com.android.purebilibili.data.model.response.SafeCenterSmsSendResponse

    @retrofit2.http.FormUrlEncoded
    @retrofit2.http.POST("x/safecenter/login/tel/verify")
    suspend fun safeCenterVerifySms(
        @retrofit2.http.Header("Referer") referer: String,
        @retrofit2.http.FieldMap params: @JvmSuppressWildcards Map<String, String>
    ): com.android.purebilibili.data.model.response.SafeCenterSmsVerifyResponse

    @retrofit2.http.FormUrlEncoded
    @retrofit2.http.POST("x/passport-login/oauth2/access_token")
    suspend fun oauth2AccessToken(
        @retrofit2.http.FieldMap params: @JvmSuppressWildcards Map<String, String>
    ): Response<LoginResponse>
    
    // ==========  TV 端登录 (获取 access_token 用于高画质视频) ==========
    
    // TV 端申请二维码
    @retrofit2.http.FormUrlEncoded
    @retrofit2.http.POST("https://passport.bilibili.com/x/passport-tv-login/qrcode/auth_code")
    suspend fun generateTvQrCode(
        @retrofit2.http.FieldMap params: @JvmSuppressWildcards Map<String, String>
    ): TvQrCodeResponse
    
    // TV 端轮询登录状态
    @retrofit2.http.FormUrlEncoded
    @retrofit2.http.POST("https://passport.bilibili.com/x/passport-tv-login/qrcode/poll")
    suspend fun pollTvQrCode(
        @retrofit2.http.FieldMap params: @JvmSuppressWildcards Map<String, String>
    ): TvPollResponse

    //  [新增] TV 端刷新 Token
    @retrofit2.http.FormUrlEncoded
    @retrofit2.http.POST("https://passport.bilibili.com/x/passport-tv-login/h5/refresh")
    suspend fun refreshToken(
        @retrofit2.http.FieldMap params: @JvmSuppressWildcards Map<String, String>
    ): com.android.purebilibili.data.model.response.TvTokenRefreshResponse
}
