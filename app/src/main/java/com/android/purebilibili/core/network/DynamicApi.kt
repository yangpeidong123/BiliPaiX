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

@kotlinx.serialization.Serializable
data class DynamicThumbRequest(
    val dyn_id_str: String,
    val up: Int,
    val spmid: String = "333.1369.0.0",
    val from_spmid: String = "333.999.0.0"
)

@kotlinx.serialization.Serializable
data class DynamicRepostRequest(
    val dyn_req: DynamicRepostDynReq,
    val web_repost_src: DynamicWebRepostSource
)

@kotlinx.serialization.Serializable
data class DynamicRepostDynReq(
    val content: DynamicRepostContent,
    val scene: Int,
    val attach_card: kotlinx.serialization.json.JsonObject?
)

@kotlinx.serialization.Serializable
data class DynamicRepostContent(
    val contents: List<DynamicRepostContentItem>
)

@kotlinx.serialization.Serializable
data class DynamicRepostContentItem(
    val raw_text: String,
    val type: Int,
    val biz_id: String
)

@kotlinx.serialization.Serializable
data class DynamicWebRepostSource(
    val dyn_id_str: String? = null,
    val revs_id: DynamicRepostResource? = null
)

@kotlinx.serialization.Serializable
data class DynamicRepostResource(
    val dyn_type: Int,
    val rid: Long
)

@kotlinx.serialization.Serializable
data class DynamicDeleteRequest(
    val dyn_id_str: String,
    val dyn_type: Int? = null,
    val rid_str: String? = null
)

@kotlinx.serialization.Serializable
data class DynamicTopRequest(
    val dyn_str: String
)

// 动态可见范围设置：object_id 为 JSON 字符串 {"dyn_id":"...","dyn_type":N}，
// action 取 "private_pub"（仅自己）/ "public_pub"（公开）。
@kotlinx.serialization.Serializable
data class DynamicVisibilityRequest(
    val object_id: String,
    val action: String
)

internal fun buildDynamicRepostRequest(
    dynamicId: String,
    content: String
): DynamicRepostRequest {
    val contents = if (content.isBlank()) {
        emptyList()
    } else {
        listOf(
            DynamicRepostContentItem(
                raw_text = content,
                type = 1,
                biz_id = ""
            )
        )
    }
    return DynamicRepostRequest(
        dyn_req = DynamicRepostDynReq(
            content = DynamicRepostContent(contents = contents),
            scene = 4,
            attach_card = null
        ),
        web_repost_src = DynamicWebRepostSource(dyn_id_str = dynamicId)
    )
}

internal fun buildFavoriteFolderDynamicRequest(
    mediaId: Long,
    content: String,
): DynamicRepostRequest {
    val contents = content.trim().takeIf { it.isNotEmpty() }?.let { text ->
        listOf(DynamicRepostContentItem(raw_text = text, type = 1, biz_id = ""))
    }.orEmpty()
    return DynamicRepostRequest(
        dyn_req = DynamicRepostDynReq(
            content = DynamicRepostContent(contents = contents),
            scene = 5,
            attach_card = null,
        ),
        web_repost_src = DynamicWebRepostSource(
            revs_id = DynamicRepostResource(dyn_type = 4300, rid = mediaId),
        ),
    )
}

private const val DYNAMIC_FEED_FEATURES =
    "itemOpusStyle,listOnlyfans"

internal const val DYNAMIC_DETAIL_FEATURES =
    "itemOpusStyle,listOnlyfans,opusBigCover,onlyfansVote,endFooterHidden,decorationCard,onlyfansAssetsV2,ugcDelete,onlyfansQaCard,commentsNewVersion,forwardListHidden,htmlNewStyle"

/** PiliPlus opus/detail uses only htmlNewStyle so old columns return full paragraphs. */
internal const val OPUS_DETAIL_FEATURES = "htmlNewStyle"

internal const val SPACE_DYNAMIC_FEATURES =
    "itemOpusStyle,listOnlyfans,opusBigCover,commentsNewVersion,onlyfansVote,onlyfansAssetsV2,decorationCard,forwardListHidden,ugcDelete"

interface DynamicApi {
    //  添加 features 参数以获取 rich_text_nodes 表情数据
    @GET("x/polymer/web-dynamic/v1/feed/all")
    suspend fun getDynamicFeed(
        @Query("type") type: String = "all",
        @Query("offset") offset: String = "",
        @Query("update_baseline") updateBaseline: String = "",
        @Query("features") features: String = DYNAMIC_FEED_FEATURES,
        @Query("timezone_offset") timezoneOffset: Int = -480,
        @Query("platform") platform: String = "web",
        @Query("web_location") webLocation: String = "333.1365"
    ): DynamicFeedResponse
    
    //  [新增] 获取指定用户的动态列表
    @GET("x/polymer/web-dynamic/v1/feed/all")
    suspend fun getUserDynamicFeed(
        @QueryMap params: Map<String, String>
    ): DynamicFeedResponse

    //  [新增] 动态未读数（红点）轻量接口：只返回更新基线以上的新动态条数，
    //  供底部导航轮询使用，避免每次拉全量 feed。
    @GET("x/polymer/web-dynamic/v1/feed/all/update")
    suspend fun getDynamicUpdateCount(
        @Query("type") type: String = "all",
        @Query("update_baseline") updateBaseline: String,
        @Query("web_location") webLocation: String = "333.1365"
    ): DynamicUpdateCountResponse

    //  与 PiliPlus 对齐：主路径使用 web 详情接口，并补齐 rid/type 与页面参数。
    @GET("x/polymer/web-dynamic/v1/detail")
    suspend fun getDynamicDetail(
        @Query("id") id: String? = null,
        @Query("rid") rid: String? = null,
        @Query("type") type: Int? = null,
        @Query("features") features: String = DYNAMIC_DETAIL_FEATURES,
        @Query("timezone_offset") timezoneOffset: Int = -480,
        @Query("gaia_source") gaiaSource: String = "Athena",
        @Query("web_location") webLocation: String = "333.1330"
    ): DynamicDetailResponse

    //  桌面端详情仅作降级：部分卡片在 web 接口字段更完整，desktop 反而会空内容。
    @GET("x/polymer/web-dynamic/desktop/v1/detail")
    suspend fun getDynamicDetailFallback(
        @Query("id") id: String,
        @Query("features") features: String = DYNAMIC_DETAIL_FEATURES,
        @Query("timezone_offset") timezoneOffset: Int = -480
    ): DynamicDetailResponse

    // 长图文/专栏 opus 详情。PiliPlus：WBI + features=htmlNewStyle + timezone_offset。
    @GET("x/polymer/web-dynamic/v1/opus/detail")
    suspend fun getOpusDetail(
        @QueryMap params: Map<String, String>
    ): DynamicDetailResponse

    @GET("https://app.bilibili.com/x/topic/web/details/top")
    suspend fun getTopicDetail(
        @Query("topic_id") topicId: Long,
        @Query("source") source: String = "H5",
        @Query("web_location") webLocation: String = "333.1036"
    ): TopicDetailResponse

    @GET("x/polymer/web-dynamic/v1/feed/topic")
    suspend fun getTopicFeed(
        @Query("topic_id") topicId: Long,
        @Query("sort_by") sortBy: Int = 0,
        @Query("offset") offset: String = "",
        @Query("page_size") pageSize: Int = 20,
        @Query("source") source: String = "Web",
        @Query("features") features: String = DYNAMIC_DETAIL_FEATURES
    ): TopicFeedResponse
    
    //  [新增] 获取动态评论列表 (type=17 表示动态)
    @GET("x/v2/reply")
    suspend fun getDynamicReplies(
        @Query("oid") oid: Long,       // 动态 id_str (转为 Long)
        @Query("type") type: Int = 17, // 17 = 动态评论区
        @Query("pn") pn: Int = 1,
        @Query("ps") ps: Int = 20,
        @Query("sort") sort: Int = 0   // 0=按时间, 1=按点赞
    ): ReplyResponse
    
    //  [新增] 发表动态评论
    @retrofit2.http.FormUrlEncoded
    @retrofit2.http.POST("x/v2/reply/add")
    suspend fun addDynamicReply(
        @retrofit2.http.Field("oid") oid: Long,
        @retrofit2.http.Field("type") type: Int = 17,
        @retrofit2.http.Field("message") message: String,
        @retrofit2.http.Field("csrf") csrf: String
    ): SimpleApiResponse
    
    //  [修复] 点赞动态 - 使用新版 API
    @retrofit2.http.POST("x/dynamic/feed/dyn/thumb")
    suspend fun likeDynamic(
        @Query("csrf") csrf: String,
        @Query("csrf_token") csrfToken: String = csrf,
        @retrofit2.http.Body body: DynamicThumbRequest
    ): SimpleApiResponse
    
    //  转发动态。接口按 Web 端 JSON dyn_req 协议提交，表单字段会导致请求失败后弹窗卡住。
    @retrofit2.http.POST("x/dynamic/feed/create/dyn")
    suspend fun repostDynamic(
        @Query("csrf") csrf: String,
        @Query("platform") platform: String = "web",
        @Query("x-bili-device-req-json") deviceRequestJson: String = "{\"platform\":\"web\",\"device\":\"pc\"}",
        @Query("x-bili-web-req-json") webRequestJson: String = "{\"spm_id\":\"333.1330\"}",
        @retrofit2.http.Body body: DynamicRepostRequest
    ): SimpleApiResponse

    @retrofit2.http.POST("x/dynamic/feed/create/dyn")
    suspend fun createFeedDynamic(
        @Query("csrf") csrf: String,
        @Query("platform") platform: String = "web",
        @Query("x-bili-device-req-json") deviceRequestJson: String = "{\"platform\":\"web\",\"device\":\"pc\"}",
        @Query("x-bili-web-req-json") webRequestJson: String = "{\"spm_id\":\"333.999\"}",
        @retrofit2.http.Body body: DynamicCreateFeedRequest
    ): DynamicCreateFeedResponse

    @retrofit2.http.POST("x/vote/create")
    suspend fun createVote(
        @Query("csrf") csrf: String,
        @retrofit2.http.Body body: DynamicCreateVoteRequest
    ): DynamicCreateVoteResponse

    @retrofit2.http.FormUrlEncoded
    @retrofit2.http.POST("x/new-reserve/up/reserve/create")
    suspend fun createReserve(
        @retrofit2.http.Field("type") type: Int = 2,
        @retrofit2.http.Field("sub_type") subType: Int,
        @retrofit2.http.Field("from") from: Int = 1,
        @retrofit2.http.Field("title") title: String,
        @retrofit2.http.Field("live_plan_start_time") livePlanStartTime: Long,
        @retrofit2.http.Field("csrf") csrf: String
    ): DynamicCreateReserveResponse

    //  发布纯文本动态（multipart form，type=4 表示纯文本）
    @retrofit2.http.Multipart
    @retrofit2.http.POST("https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/create")
    suspend fun createDynamic(
        @retrofit2.http.Part("dynamic_id") dynamicId: Int = 0,
        @retrofit2.http.Part("type") type: Int = 4,
        @retrofit2.http.Part("rid") rid: Int = 0,
        @retrofit2.http.Part("content") content: String,
        @retrofit2.http.Part("csrf") csrf: String
    ): DynamicCreateResponse

    //  关注 UP 列表（含未读标记 has_update，供 UP 列表红点使用）
    @GET("https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/w_dyn_uplist")
    suspend fun getDynamicUplist(): UplistResponse

    @retrofit2.http.POST("x/dynamic/feed/operate/remove")
    suspend fun deleteDynamic(
        @Query("csrf") csrf: String,
        @Query("platform") platform: String = "web",
        @retrofit2.http.Body body: DynamicDeleteRequest
    ): SimpleApiResponse

    //  置顶 / 取消置顶动态（仅自己的动态，JSON body {"dyn_str": "..."}）
    @retrofit2.http.POST("x/dynamic/feed/space/set_top")
    suspend fun setDynamicTop(
        @Query("csrf") csrf: String,
        @retrofit2.http.Body body: DynamicTopRequest
    ): SimpleApiResponse

    @retrofit2.http.POST("x/dynamic/feed/space/rm_top")
    suspend fun removeDynamicTop(
        @Query("csrf") csrf: String,
        @retrofit2.http.Body body: DynamicTopRequest
    ): SimpleApiResponse

    @GET("x/vote/vote_info")
    suspend fun getVoteInfo(
        @Query("vote_id") voteId: Long
    ): DynamicVoteInfoResponse

    @retrofit2.http.POST("x/vote/do_vote")
    suspend fun doVote(
        @Query("csrf") csrf: String,
        @retrofit2.http.Body body: DynamicDoVoteRequest
    ): DynamicVoteInfoResponse

    //  动态可见范围（公开 / 仅自己）
    @retrofit2.http.FormUrlEncoded
    @retrofit2.http.POST("x/dynamic/feed/dynamic_report/add")
    suspend fun reportDynamic(
        @Query("csrf") csrf: String,
        @retrofit2.http.Field("accused_uid") accusedUid: Long,
        @retrofit2.http.Field("dynamic_id") dynamicId: String,
        @retrofit2.http.Field("reason_type") reasonType: Int,
        @retrofit2.http.Field("reason_desc") reasonDesc: String? = null
    ): SimpleApiResponse

    @retrofit2.http.Multipart
    @retrofit2.http.POST("https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/modify")
    suspend fun editDynamic(
        @retrofit2.http.Part("dynamic_id") dynamicId: String,
        @retrofit2.http.Part("type") type: Int = 4,
        @retrofit2.http.Part("rid") rid: Int = 0,
        @retrofit2.http.Part("content") content: String,
        @retrofit2.http.Part("csrf") csrf: String
    ): DynamicCreateResponse

    @retrofit2.http.POST("x/dynamic/feed/dyn/private_pub_setting")
    suspend fun setDynamicVisibility(
        @Query("csrf") csrf: String,
        @Query("platform") platform: String = "web",
        @retrofit2.http.Body body: DynamicVisibilityRequest
    ): SimpleApiResponse

    //  评论互动状态（评论精选 / 评论开关），oid 取 basic.comment_id_str
    @GET("x/v2/reply/subject/interaction-status")
    suspend fun getReplyInteractionStatus(
        @Query("oid") oid: Long,
        @Query("type") type: Int,
        @Query("web_location") webLocation: Double = 333.1369
    ): ReplyInteractionResponse

    //  修改评论互动：action 1=开启精选 2=停止精选 3=关闭评论 4=恢复评论
    @retrofit2.http.FormUrlEncoded
    @retrofit2.http.POST("x/v2/reply/subject/modify")
    suspend fun modifyReplySubject(
        @retrofit2.http.Field("oid") oid: Long,
        @retrofit2.http.Field("type") type: Int,
        @retrofit2.http.Field("action") action: Int,
        @retrofit2.http.Field("csrf") csrf: String
    ): SimpleApiResponse
}
