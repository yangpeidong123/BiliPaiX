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

interface SplashApi {
    @GET("https://app.bilibili.com/x/v2/splash/list")
    suspend fun getSplashList(
        @QueryMap params: Map<String, String> // 包含 appkey, ts, sign 等
    ): com.android.purebilibili.data.model.response.SplashResponse
    
    // [新增] 品牌开屏壁纸列表 (无广告，高质量)
    @GET("https://app.bilibili.com/x/v2/splash/brand/list")
    suspend fun getSplashBrandList(
        @QueryMap params: Map<String, String>
    ): com.android.purebilibili.data.model.response.SplashBrandResponse
}
