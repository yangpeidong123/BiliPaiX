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

interface StoryApi {
    // 获取故事流 (竖屏短视频列表)
    @GET("x/v2/feed/index/story")
    suspend fun getStoryFeed(
        @Query("fnval") fnval: Int = 4048,         // 视频格式参数
        @Query("fnver") fnver: Int = 0,
        @Query("force_host") forceHost: Int = 0,
        @Query("fourk") fourk: Int = 1,
        @Query("qn") qn: Int = 32,                  // 画质
        @Query("ps") ps: Int = 20,                  // 每页数量
        @Query("aid") aid: Long = 0,                // 可选，从此视频开始
        @Query("bvid") bvid: String = ""            // 可选，从此视频开始
    ): StoryResponse
}
