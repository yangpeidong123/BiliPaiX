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

interface AudioApi {
    // 🎵 获取音频基本信息
    @GET("audio/music-service-c/web/song/info")
    suspend fun getSongInfo(
        @Query("sid") sid: Long
    ): com.android.purebilibili.data.model.response.SongInfoResponse

    // 🎵 获取音频流地址
    @GET("audio/music-service-c/web/url")
    suspend fun getSongStream(
        @Query("sid") sid: Long,
        @Query("privilege") privilege: Int = 2,
        @Query("quality") quality: Int = 2
    ): com.android.purebilibili.data.model.response.SongStreamResponse

    // 🎵 获取歌词
    @GET("audio/music-service-c/web/song/lyric")
    suspend fun getSongLyric(
        @Query("sid") sid: Long
    ): com.android.purebilibili.data.model.response.SongLyricResponse
}
