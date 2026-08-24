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

interface BangumiApi {
    // 番剧时间表
    @GET("pgc/web/timeline")
    suspend fun getTimeline(
        @Query("types") types: Int,      // 1=番剧 4=国创
        @Query("before") before: Int = 3,
        @Query("after") after: Int = 7
    ): com.android.purebilibili.data.model.response.BangumiTimelineResponse
    
    // 番剧索引/筛选 -  需要 st 参数（与 season_type 相同值）
    @GET("pgc/season/index/result")
    suspend fun getBangumiIndex(
        @Query("season_type") seasonType: Int,   // 1=番剧 2=电影 3=纪录片 4=国创 5=电视剧 7=综艺
        @Query("st") st: Int,                    //  [修复] 必需参数，与 season_type 相同
        @Query("page") page: Int = 1,
        @Query("pagesize") pageSize: Int = 20,
        @Query("order") order: Int = 3,          // 3=综合排序，2=播放量
        @Query("season_version") seasonVersion: Int = -1,  // -1=全部
        @Query("spoken_language_type") spokenLanguageType: Int = -1,  // -1=全部
        @Query("area") area: Int = -1,           // -1=全部地区
        @Query("is_finish") isFinish: Int = -1,  // -1=全部
        @Query("copyright") copyright: String = "-1", // -1=全部
        @Query("season_status") seasonStatus: String = "-1",  // -1=全部，1=免费，4,6=大会员
        @Query("season_month") seasonMonth: Int = -1,    // -1=全部
        @Query("year") year: String = "-1",      // -1=全部
        @Query("release_date") releaseDate: String = "-1", // -1=全部
        @Query("style_id") styleId: Int = -1,    // -1=全部
        @Query("producer_id") producerId: Int = -1, // -1=全部
        @Query("sort") sort: Int = 0,
        @Query("type") type: Int = 1
    ): com.android.purebilibili.data.model.response.BangumiIndexResponse

    @GET("pgc/season/index/condition")
    suspend fun getBangumiIndexCondition(
        @Query("season_type") seasonType: Int? = null,
        @Query("type") type: Int = 0,
        @Query("index_type") indexType: Int? = null,
    ): com.android.purebilibili.data.model.response.BangumiIndexConditionResponse

    @GET("pgc/season/index/result")
    suspend fun getBangumiIndexResult(
        @QueryMap params: Map<String, String>,
    ): com.android.purebilibili.data.model.response.BangumiIndexResponse
    
    // 番剧详情 -  返回 ResponseBody 自行解析，防止 OOM
    @GET("pgc/view/web/season")
    suspend fun getSeasonDetail(
        @Query("season_id") seasonId: Long? = null,
        @Query("ep_id") epId: Long? = null
    ): ResponseBody
    
    // 番剧播放地址 - BiliPai parity path
    @GET(BANGUMI_PLAY_URL_PATH)
    suspend fun getBangumiPlayUrl(
        @QueryMap params: Map<String, String>
    ): ResponseBody

    @GET(BANGUMI_PLAY_URL_LEGACY_PATH)
    suspend fun getBangumiPlayUrlLegacy(
        @QueryMap params: Map<String, String>
    ): ResponseBody
    
    // 追番/追剧
    @retrofit2.http.FormUrlEncoded
    @retrofit2.http.POST("pgc/web/follow/add")
    suspend fun followBangumi(
        @retrofit2.http.Field("season_id") seasonId: Long,
        @retrofit2.http.Field("csrf") csrf: String
    ): com.android.purebilibili.data.model.response.SimpleApiResponse
    
    // 取消追番/追剧
    @retrofit2.http.FormUrlEncoded
    @retrofit2.http.POST("pgc/web/follow/del")
    suspend fun unfollowBangumi(
        @retrofit2.http.Field("season_id") seasonId: Long,
        @retrofit2.http.Field("csrf") csrf: String
    ): com.android.purebilibili.data.model.response.SimpleApiResponse

    // 更新追番/追剧状态：1=想看, 2=在看, 3=看过
    @retrofit2.http.FormUrlEncoded
    @retrofit2.http.POST("pgc/web/follow/status/update")
    suspend fun updateBangumiFollowStatus(
        @retrofit2.http.Field("season_id") seasonId: Long,
        @retrofit2.http.Field("status") status: Int,
        @retrofit2.http.Field("csrf") csrf: String
    ): com.android.purebilibili.data.model.response.SimpleApiResponse

    @retrofit2.http.FormUrlEncoded
    @retrofit2.http.POST("pgc/web/follow/status/update")
    suspend fun updateBangumiFollowStatusBatch(
        @retrofit2.http.Field("season_id") seasonIds: String,
        @retrofit2.http.Field("status") status: Int,
        @retrofit2.http.Field("csrf") csrf: String,
    ): com.android.purebilibili.data.model.response.SimpleApiResponse
    
    //  [新增] 我的追番列表
    @GET("x/space/bangumi/follow/list")
    suspend fun getMyFollowBangumi(
        @Query("vmid") vmid: Long,          // 用户 mid (登录用户的 mid)
        @Query("type") type: Int = 1,        // 1=追番 2=追剧
        @Query("follow_status") followStatus: Int? = null,
        @Query("pn") pn: Int = 1,
        @Query("ps") ps: Int = 30
    ): com.android.purebilibili.data.model.response.MyFollowBangumiResponse

    @GET("pgc/review/short/list")
    suspend fun getBangumiShortReviews(
        @Query("media_id") mediaId: Long,
        @Query("ps") pageSize: Int = 20,
        @Query("sort") sort: Int = 0,
        @Query("cursor") cursor: String = "",
        @Query("web_location") webLocation: String = "666.19"
    ): BangumiReviewListResponse

    @GET("pgc/review/long/list")
    suspend fun getBangumiLongReviews(
        @Query("media_id") mediaId: Long,
        @Query("ps") pageSize: Int = 20,
        @Query("sort") sort: Int = 0,
        @Query("cursor") cursor: String = "",
        @Query("web_location") webLocation: String = "666.19"
    ): BangumiReviewListResponse

    @retrofit2.http.FormUrlEncoded
    @retrofit2.http.POST("pgc/review/action/like")
    suspend fun likeBangumiReview(
        @retrofit2.http.Field("media_id") mediaId: Long,
        @retrofit2.http.Field("review_type") reviewType: Int = 2,
        @retrofit2.http.Field("review_id") reviewId: Long,
        @retrofit2.http.Field("csrf") csrf: String
    ): SimpleApiResponse

    @retrofit2.http.FormUrlEncoded
    @retrofit2.http.POST("pgc/review/short/post")
    suspend fun postBangumiShortReview(
        @retrofit2.http.Field("media_id") mediaId: Long,
        @retrofit2.http.Field("score") score: Int,
        @retrofit2.http.Field("content") content: String,
        @retrofit2.http.Field("csrf") csrf: String
    ): SimpleApiResponse
}
