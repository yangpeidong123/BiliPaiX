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

interface SearchApi {
    @GET("x/web-interface/wbi/search/default")
    suspend fun getDefaultSearch(@QueryMap params: Map<String, String>): com.android.purebilibili.data.model.response.SearchDefaultResponse

    @GET("x/web-interface/search/default")
    suspend fun getDefaultSearchLegacy(): com.android.purebilibili.data.model.response.SearchDefaultResponse

    @GET("x/web-interface/wbi/search/square")
    suspend fun getHotSearch(@QueryMap params: Map<String, String>): HotSearchResponse

    @GET("https://s.search.bilibili.com/main/hotword")
    suspend fun getTrendingList(
        @Query("limit") limit: Int = 30
    ): com.android.purebilibili.data.model.response.SearchTrendingResponse

    @GET("https://app.bilibili.com/x/v2/search/recommend")
    suspend fun getSearchRecommend(
        @Query("build") build: Int = 8430300,
        @Query("channel") channel: String = "master",
        @Query("version") version: String = "8.43.0",
        @Query("c_locale") cLocale: String = "zh_CN",
        @Query("mobi_app") mobiApp: String = "android",
        @Query("platform") platform: String = "android",
        @Query("s_locale") sLocale: String = "zh_CN",
        @Query("from") from: Int = 2
    ): com.android.purebilibili.data.model.response.SearchRecommendResponse

    //  综合搜索 (不支持排序)
    @Headers(
        "Origin: https://search.bilibili.com",
        "Referer: https://search.bilibili.com/"
    )
    @GET("x/web-interface/wbi/search/all/v2")
    suspend fun searchAll(@QueryMap params: Map<String, String>): SearchResponse
    
    //  [修复] 分类搜索 - 支持排序和时长筛选
    @Headers(
        "Origin: https://search.bilibili.com",
        "Referer: https://search.bilibili.com/"
    )
    @GET("x/web-interface/wbi/search/type")
    suspend fun search(@QueryMap params: Map<String, String>): SearchTypeResponse
    
    //  [新增] UP主搜索 - 专用解析
    @Headers(
        "Origin: https://search.bilibili.com",
        "Referer: https://search.bilibili.com/"
    )
    @GET("x/web-interface/wbi/search/type")
    suspend fun searchUp(@QueryMap params: Map<String, String>): com.android.purebilibili.data.model.response.SearchUpResponse
    
    //  [新增] 番剧搜索 - search_type=media_bangumi
    @Headers(
        "Origin: https://search.bilibili.com",
        "Referer: https://search.bilibili.com/"
    )
    @GET("x/web-interface/wbi/search/type")
    suspend fun searchBangumi(@QueryMap params: Map<String, String>): com.android.purebilibili.data.model.response.BangumiSearchResponse

    //  [新增] 影视搜索 - search_type=media_ft
    @Headers(
        "Origin: https://search.bilibili.com",
        "Referer: https://search.bilibili.com/"
    )
    @GET("x/web-interface/wbi/search/type")
    suspend fun searchMediaFt(@QueryMap params: Map<String, String>): com.android.purebilibili.data.model.response.BangumiSearchResponse
    
    //  [新增] 直播搜索 - search_type=live_room
    @Headers(
        "Origin: https://search.bilibili.com",
        "Referer: https://search.bilibili.com/"
    )
    @GET("x/web-interface/wbi/search/type")
    suspend fun searchLive(@QueryMap params: Map<String, String>): com.android.purebilibili.data.model.response.LiveRoomSearchResponse

    @Headers(
        "Origin: https://search.bilibili.com",
        "Referer: https://search.bilibili.com/"
    )
    @GET("x/web-interface/wbi/search/type")
    suspend fun searchLiveUser(@QueryMap params: Map<String, String>): com.android.purebilibili.data.model.response.SearchLiveUserResponse

    //  [新增] 专栏搜索 - search_type=article
    @Headers(
        "Origin: https://search.bilibili.com",
        "Referer: https://search.bilibili.com/"
    )
    @GET("x/web-interface/wbi/search/type")
    suspend fun searchArticle(@QueryMap params: Map<String, String>): com.android.purebilibili.data.model.response.SearchArticleResponse

    @Headers(
        "Origin: https://search.bilibili.com",
        "Referer: https://search.bilibili.com/"
    )
    @GET("x/web-interface/wbi/search/type")
    suspend fun searchTopic(@QueryMap params: Map<String, String>): com.android.purebilibili.data.model.response.SearchTopicResponse

    @Headers(
        "Origin: https://search.bilibili.com",
        "Referer: https://search.bilibili.com/"
    )
    @GET("x/web-interface/wbi/search/type")
    suspend fun searchPhoto(@QueryMap params: Map<String, String>): com.android.purebilibili.data.model.response.SearchPhotoResponse
    
    //  搜索建议/联想
    @GET("https://s.search.bilibili.com/main/suggest")
    suspend fun getSearchSuggest(
        @Query("term") term: String,
        @Query("main_ver") mainVer: String = "v1",
        @Query("highlight") highlight: String = term
    ): SearchSuggestResponse
}
