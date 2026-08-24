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

interface BilibiliApi {
    // ==================== 用户信息模块 ====================
    @GET("x/web-interface/zone")
    suspend fun getIpZone(): IpLocationResponse

    @GET("x/web-interface/nav")
    suspend fun getNavInfo(): NavResponse

    @GET("x/web-interface/nav/stat")
    suspend fun getNavStat(): NavStatResponse

    @GET("x/member/web/account")
    suspend fun getMemberAccount(): MemberAccountResponse

    @retrofit2.http.FormUrlEncoded
    @POST("x/member/web/sign/update")
    suspend fun updateMemberSign(
        @retrofit2.http.Field("user_sign") userSign: String,
        @retrofit2.http.Field("csrf") csrf: String
    ): SimpleApiResponse

    //  [New] 获取用户卡片信息 (轻量级用户信息)
    @GET("x/web-interface/card")
    suspend fun getUserCard(
        @Query("mid") mid: Long,
        @Query("photo") photo: Boolean = true
    ): UserCardResponse

    @GET("x/web-interface/card")
    suspend fun getUserCardRaw(
        @Query("mid") mid: Long,
        @Query("photo") photo: Boolean = true
    ): okhttp3.ResponseBody

    @GET("x/note/list/archive")
    suspend fun getPrivateVideoNoteIds(
        @Query("oid") oid: Long,
        @Query("oid_type") oidType: Int = 0,
        @Query("csrf") csrf: String? = null
    ): VideoNoteArchiveListResponse

    @GET("x/note/info")
    suspend fun getPrivateVideoNoteInfo(
        @Query("oid") oid: Long,
        @Query("oid_type") oidType: Int = 0,
        @Query("note_id") noteId: String
    ): VideoNoteInfoResponse

    @GET("x/note/is_forbid")
    suspend fun getVideoNoteForbidState(
        @Query("aid") aid: Long
    ): VideoNoteForbidResponse

    @retrofit2.http.FormUrlEncoded
    @POST("x/note/add")
    suspend fun saveVideoNote(
        @retrofit2.http.FieldMap fields: Map<String, String>
    ): VideoNoteSaveResponse

    @retrofit2.http.FormUrlEncoded
    @POST("x/note/del")
    suspend fun deleteVideoNote(
        @retrofit2.http.Field("oid") oid: Long,
        @retrofit2.http.Field("note_id") noteId: String,
        @retrofit2.http.Field("csrf") csrf: String
    ): SimpleApiResponse

    @GET("x/note/publish/list/archive")
    suspend fun getPublicVideoNoteList(
        @Query("oid") oid: Long,
        @Query("oid_type") oidType: Int = 0,
        @Query("ps") pageSize: Int = 10,
        @Query("pn") pageNumber: Int = 1
    ): PublicVideoNoteListResponse

    @GET("x/note/publish/info")
    suspend fun getPublicVideoNoteInfo(
        @Query("cvid") cvid: Long
    ): PublicVideoNoteInfoResponse

    @GET("x/web-interface/history/cursor")
    suspend fun getHistoryList(
        @Query("ps") ps: Int = 30,
        @Query("max") max: Long? = null,            //  游标: 上一页最后一条的 oid
        @Query("view_at") viewAt: Long? = null,     //  游标: 上一页最后一条的 view_at
        @Query("business") business: String? = null, //  null=省略该参数
        @Query("type") type: String? = null         //  all/archive/live/article；null=省略
    ): HistoryResponse

    @GET("x/web-interface/history/search")
    suspend fun searchHistory(
        @Query("pn") page: Int = 1,
        @Query("keyword") keyword: String,
        @Query("business") business: String = "all",
    ): HistoryResponse

    @retrofit2.http.FormUrlEncoded
    @POST("x/v2/history/report")
    suspend fun reportHistory(
        @retrofit2.http.FieldMap fields: Map<String, String>
    ): SimpleApiResponse

    @retrofit2.http.FormUrlEncoded
    @POST("x/v2/history/delete")
    suspend fun deleteHistoryItem(
        @retrofit2.http.Field("kid") kid: String,
        @retrofit2.http.Field("csrf") csrf: String
    ): SimpleApiResponse

    @retrofit2.http.FormUrlEncoded
    @POST("x/v2/history/clear")
    suspend fun clearHistory(
        @retrofit2.http.Field("csrf") csrf: String
    ): SimpleApiResponse

    @retrofit2.http.FormUrlEncoded
    @POST("x/v2/history/shadow/set")
    suspend fun setHistoryShadow(
        @retrofit2.http.Field("switch") shadowSwitch: Boolean,
        @retrofit2.http.Field("csrf") csrf: String
    ): SimpleApiResponse

    @GET("x/v2/history/shadow")
    suspend fun getHistoryShadow(): HistoryShadowResponse

    @GET("x/v3/fav/folder/created/list-all")
    suspend fun getFavFolders(
        @Query("up_mid") mid: Long,
        @Query("type") type: Int? = null,
        @Query("rid") rid: Long? = null,
        @Query("web_location") webLocation: String = "333.1387"
    ): FavFolderResponse

    @GET("x/v3/fav/folder/collected/list")
    suspend fun getCollectedFavFolders(
        @Query("up_mid") mid: Long,
        @Query("pn") pn: Int = 1,
        @Query("ps") ps: Int = 20,
        @Query("platform") platform: String = "web"
    ): FavFolderResponse

    // [新增] 创建收藏夹
    @retrofit2.http.FormUrlEncoded
    @retrofit2.http.POST("x/v3/fav/folder/add")
    suspend fun createFavFolder(
        @retrofit2.http.Field("title") title: String,
        @retrofit2.http.Field("intro") intro: String = "",
        @retrofit2.http.Field("privacy") privacy: Int = 0, // 0:公开, 1:私密
        @retrofit2.http.Field("csrf") csrf: String
    ): SimpleApiResponse

    @retrofit2.http.FormUrlEncoded
    @retrofit2.http.POST("x/v3/fav/folder/edit")
    suspend fun editFavFolder(
        @retrofit2.http.Field("media_id") mediaId: Long,
        @retrofit2.http.Field("title") title: String,
        @retrofit2.http.Field("intro") intro: String = "",
        @retrofit2.http.Field("privacy") privacy: Int = 0,
        @retrofit2.http.Field("cover") cover: String = "",
        @retrofit2.http.Field("csrf") csrf: String,
    ): SimpleApiResponse

    @retrofit2.http.FormUrlEncoded
    @retrofit2.http.POST("x/v3/fav/folder/del")
    suspend fun deleteFavFolders(
        @retrofit2.http.Field("media_ids") mediaIds: String,
        @retrofit2.http.Field("platform") platform: String = "web",
        @retrofit2.http.Field("csrf") csrf: String,
    ): SimpleApiResponse

    @GET("x/v3/fav/resource/list")
    suspend fun getFavoriteList(
        @Query("media_id") mediaId: Long,
        @Query("pn") pn: Int = 1,
        @Query("ps") ps: Int = 20,
        @Query("keyword") keyword: String = "",
        @Query("order") order: String = "mtime",
        // 文档：type 0=当前收藏夹，1=全部；tid 0=全部分区；ps 定义域 1-20
        @Query("type") type: Int = 0,
        @Query("tid") tid: Int = 0,
        @Query("platform") platform: String = "web"
    ): FavoriteResourceResponse

    @GET("x/space/like/video")
    suspend fun getLikedVideos(
        @Query("vmid") mid: Long
    ): LikedVideosResponse

    @GET("x/space/fav/season/list")
    suspend fun getFavoriteSeasonList(
        @Query("season_id") seasonId: Long,
        @Query("pn") pn: Int = 1,
        @Query("ps") ps: Int = 20
    ): FavoriteResourceResponse

    // [新增] 批量删除收藏资源 (取消收藏)
    @retrofit2.http.FormUrlEncoded
    @retrofit2.http.POST("x/v3/fav/resource/batch-del")
    suspend fun batchDelFavResource(
        @retrofit2.http.Field("media_id") mediaId: Long,
        @retrofit2.http.Field("resources") resources: String, // 格式: oid:type (e.g. "123456:2")
        @retrofit2.http.Field("csrf") csrf: String
    ): SimpleApiResponse

    @retrofit2.http.FormUrlEncoded
    @retrofit2.http.POST("x/v3/fav/resource/copy")
    suspend fun copyFavResources(
        @retrofit2.http.Field("src_media_id") sourceMediaId: Long,
        @retrofit2.http.Field("tar_media_id") targetMediaId: Long,
        @retrofit2.http.Field("mid") mid: Long,
        @retrofit2.http.Field("resources") resources: String,
        @retrofit2.http.Field("platform") platform: String = "web",
        @retrofit2.http.Field("csrf") csrf: String,
    ): SimpleApiResponse

    @retrofit2.http.FormUrlEncoded
    @retrofit2.http.POST("x/v3/fav/resource/move")
    suspend fun moveFavResources(
        @retrofit2.http.Field("src_media_id") sourceMediaId: Long,
        @retrofit2.http.Field("tar_media_id") targetMediaId: Long,
        @retrofit2.http.Field("mid") mid: Long,
        @retrofit2.http.Field("resources") resources: String,
        @retrofit2.http.Field("platform") platform: String = "web",
        @retrofit2.http.Field("csrf") csrf: String,
    ): SimpleApiResponse

    @retrofit2.http.FormUrlEncoded
    @retrofit2.http.POST("x/v3/fav/resource/clean")
    suspend fun cleanInvalidFavResource(
        @retrofit2.http.Field("media_id") mediaId: Long,
        @retrofit2.http.Field("csrf") csrf: String
    ): SimpleApiResponse

    @GET("x/polymer/web-dynamic/v1/opus/feed/fav")
    suspend fun getFavoriteArticles(
        @Query("page_size") pageSize: Int = 20,
        @Query("page") page: Int = 1,
    ): com.android.purebilibili.data.model.response.FavoriteArticleResponse

    @GET("x/note/list")
    suspend fun getFavoriteNotes(
        @Query("pn") page: Int = 1,
        @Query("ps") pageSize: Int = 10,
        @Query("csrf") csrf: String,
    ): com.android.purebilibili.data.model.response.FavoriteNoteResponse

    @GET("x/note/publish/list/user")
    suspend fun getPublishedFavoriteNotes(
        @Query("pn") page: Int = 1,
        @Query("ps") pageSize: Int = 10,
        @Query("csrf") csrf: String,
    ): com.android.purebilibili.data.model.response.FavoriteNoteResponse

    @GET("x/topic/web/fav/list")
    suspend fun getFavoriteTopics(
        @Query("page_size") pageSize: Int = 24,
        @Query("page_num") page: Int = 1,
        @Query("web_location") webLocation: String = "333.1387",
    ): com.android.purebilibili.data.model.response.FavoriteTopicResponse

    @GET("pugv/app/web/favorite/page")
    suspend fun getFavoriteCourses(
        @Query("mid") mid: Long,
        @Query("ps") pageSize: Int = 20,
        @Query("pn") page: Int = 1,
        @Query("web_location") webLocation: String = "333.1387",
    ): com.android.purebilibili.data.model.response.FavoriteCourseResponse

    @retrofit2.http.FormUrlEncoded
    @retrofit2.http.POST("x/article/favorites/del")
    suspend fun deleteFavoriteArticle(
        @retrofit2.http.Field("id") id: Long,
        @retrofit2.http.Field("csrf") csrf: String,
    ): SimpleApiResponse

    @retrofit2.http.FormUrlEncoded
    @retrofit2.http.POST("x/note/del")
    suspend fun deleteFavoriteNote(
        @retrofit2.http.Field("note_ids") noteIds: String,
        @retrofit2.http.Field("csrf") csrf: String,
    ): SimpleApiResponse

    @retrofit2.http.FormUrlEncoded
    @retrofit2.http.POST("x/note/publish/del")
    suspend fun deletePublishedFavoriteNote(
        @retrofit2.http.Field("note_ids") noteIds: String,
        @retrofit2.http.Field("csrf") csrf: String,
    ): SimpleApiResponse

    @retrofit2.http.FormUrlEncoded
    @retrofit2.http.POST("x/topic/fav/sub/cancel")
    suspend fun deleteFavoriteTopic(
        @retrofit2.http.Field("topic_id") topicId: Long,
        @retrofit2.http.Field("csrf") csrf: String,
    ): SimpleApiResponse

    @retrofit2.http.FormUrlEncoded
    @retrofit2.http.POST("pugv/app/web/favorite/del")
    suspend fun deleteFavoriteCourse(
        @retrofit2.http.Field("season_id") seasonId: Long,
        @retrofit2.http.Field("csrf") csrf: String,
    ): SimpleApiResponse

    // ==================== 推荐/热门模块 ====================
    @GET("x/web-interface/wbi/index/top/feed/rcmd")
    suspend fun getRecommendParams(@QueryMap params: Map<String, String>): RecommendResponse
    
    //  移动端推荐流 API (需要 access_token + appkey 签名)
    @GET("https://app.bilibili.com/x/v2/feed/index")
    suspend fun getMobileFeed(@QueryMap params: Map<String, String>): MobileFeedResponse

    //  合并模式 App 半边专用: 参数已按 BiliPai/BiliPai 规范 percent-encode 后签名,
    //  用 encoded=true 原样发送, 避免 Retrofit 二次编码导致签名不一致(-403/-400)
    @GET("https://app.bilibili.com/x/v2/feed/index")
    suspend fun getMobileFeedEncoded(
        @QueryMap(encoded = true) params: @JvmSuppressWildcards Map<String, String>
    ): MobileFeedResponse

    @GET("https://app.bilibili.com/x/feed/dislike")
    suspend fun submitMobileFeedDislike(
        @QueryMap params: Map<String, String>
    ): SimpleApiResponse
    
    @GET("x/web-interface/popular")
    suspend fun getPopularVideos(
        @Query("pn") pn: Int = 1,
        @Query("ps") ps: Int = 20
    ): PopularResponse  //  使用专用响应类型

    @GET("x/web-interface/ranking/v2")
    suspend fun getRankingVideos(
        @Query("rid") rid: Int = 0,
        @Query("type") type: String = "all"
    ): RankingResponse

    @GET("x/web-interface/popular/precious")
    suspend fun getPopularPreciousVideos(): PopularPreciousResponse

    @GET("x/web-interface/popular/series/list")
    suspend fun getWeeklySeriesList(): PopularSeriesListResponse

    @GET("x/web-interface/popular/series/one")
    suspend fun getWeeklySeriesVideos(
        @Query("number") number: Int
    ): PopularSeriesOneResponse
    
    //  [修复] 分区视频 - 使用 dynamic/region API 返回完整 stat（包含播放量）
    // 原 newlist API 不返回 stat 数据
    @GET("x/web-interface/dynamic/region")
    suspend fun getRegionVideos(
        @Query("rid") rid: Int,    // 分区 ID (如 4=游戏, 36=知识, 188=科技)
        @Query("pn") pn: Int = 1,
        @Query("ps") ps: Int = 30
    ): DynamicRegionResponse
    
    // ==================== 直播模块 ====================
    // 直播列表 - 使用 v3 API (经测试确认可用)
    @GET("https://api.live.bilibili.com/room/v3/area/getRoomList")
    suspend fun getLiveList(
        @Query("parent_area_id") parentAreaId: Int = 0,  // 0=全站
        @Query("area_id") areaId: Int = 0,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 30,
        @Query("sort_type") sortType: String = "online"  // 按人气排序
    ): LiveResponse

    @GET("https://api.live.bilibili.com/xlive/web-interface/v1/webMain/getMoreRecList")
    suspend fun getLiveRecommendList(
        @Query("platform") platform: String = "web",
        @Query("web_location") webLocation: String = "333.1007"
    ): LiveRecommendResponse
    
    //  [新增] 获取关注的直播 - 需要登录
    @GET("https://api.live.bilibili.com/xlive/web-ucenter/user/following")
    suspend fun getFollowedLive(
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 30,
        @Query("ignoreRecord") ignoreRecord: Int = 1,
        @Query("hit_ab") hitAb: Boolean = true
    ): FollowedLiveResponse
    
    //  [新增] 获取直播分区列表
    @GET("https://api.live.bilibili.com/room/v1/Area/getList")
    suspend fun getLiveAreaList(): LiveAreaListResponse
    
    //  [新增] 分区推荐直播列表 (xlive API)
    @GET("https://api.live.bilibili.com/xlive/web-interface/v1/second/getList")
    suspend fun getLiveSecondAreaList(
        @Query("platform") platform: String = "web",
        @Query("parent_area_id") parentAreaId: Int,
        @Query("area_id") areaId: Int = 0,
        @Query("page") page: Int = 1,
        @Query("sort_type") sortType: String = "online"
    ): LiveSecondAreaResponse

    // BiliPai-aligned app live home feed
    @GET("https://api.live.bilibili.com/xlive/app-interface/v2/index/feed")
    suspend fun getLiveFeedIndex(
        @QueryMap params: Map<String, String>
    ): com.android.purebilibili.data.model.response.LiveFeedIndexResponse

    // BiliPai-aligned app second-area list (supports new_tags sort chips)
    @GET("https://api.live.bilibili.com/xlive/app-interface/v2/second/getList")
    suspend fun getLiveAppSecondList(
        @QueryMap params: Map<String, String>
    ): com.android.purebilibili.data.model.response.LiveAppSecondListResponse
    
    //  [新增] 获取直播间初始化信息 (真实房间号)
    @GET("https://api.live.bilibili.com/room/v1/Room/room_init")
    suspend fun getLiveRoomInit(
        @Query("id") roomId: Long
    ): LiveRoomInitResponse
    
    //  [新增] 获取直播间详细信息 (含主播信息)
    @GET("https://api.live.bilibili.com/xlive/web-room/v1/index/getInfoByRoom")
    suspend fun getLiveRoomDetail(
        @Query("room_id") roomId: Long
    ): LiveRoomDetailResponse

    @GET("https://api.live.bilibili.com/xlive/web-room/v1/index/getH5InfoByRoom")
    suspend fun getLiveRoomH5Info(
        @Query("room_id") roomId: Long
    ): ResponseBody

    @GET("https://api.live.bilibili.com/xlive/web-room/v1/dM/gethistory")
    suspend fun getLiveDanmakuHistory(
        @Query("roomid") roomId: Long
    ): ResponseBody

    @GET("https://api.live.bilibili.com/xlive/web-room/v1/dM/GetDMConfigByGroup")
    suspend fun getLiveDanmakuConfig(
        @Query("room_id") roomId: Long,
        @Query("web_location") webLocation: String = "444.8"
    ): ResponseBody

    @GET("https://live-trace.bilibili.com/xlive/rdata-interface/v1/heartbeat/webHeartBeat")
    suspend fun reportLiveHeartbeat(
        @QueryMap params: Map<String, String>
    ): ResponseBody
    
    //  [新增] 获取直播弹幕 WebSocket 信息
    @GET("https://api.live.bilibili.com/xlive/web-room/v1/index/getDanmuInfo")
    suspend fun getDanmuInfo(
        @Query("id") roomId: Long,
        @Query("type") type: Int = 0
    ): LiveDanmuInfoResponse
    
    //  [新增] 获取直播弹幕 WebSocket 信息 (Wbi 签名版 - 解决 -352 风控)
    @GET("https://api.live.bilibili.com/xlive/web-room/v1/index/getDanmuInfo")
    suspend fun getDanmuInfoWbi(
        @QueryMap params: Map<String, String>
    ): LiveDanmuInfoResponse
    
    //  [新增] 获取直播间详情（包含在线人数）
    @GET("https://api.live.bilibili.com/room/v1/Room/get_info")
    suspend fun getRoomInfo(
        @Query("room_id") roomId: Long
    ): RoomInfoResponse
    
    //  [新增] 获取直播流 URL - 使用更可靠的 xlive API
    @GET("https://api.live.bilibili.com/xlive/web-room/v2/index/getRoomPlayInfo")
    suspend fun getLivePlayUrl(
        @Query("room_id") roomId: Long,
        @Query("protocol") protocol: String = "0,1",  // 0=http_stream, 1=http_hls
        @Query("format") format: String = "0,1,2",    // 0=flv, 1=ts, 2=fmp4
        @Query("codec") codec: String = "0,1,2",      // 0=avc, 1=hevc, 2=av1
        @Query("qn") quality: Int = 150,              // 150=高清
        @Query("platform") platform: String = "web",
        @Query("ptype") ptype: Int = 8,
        @Query("dolby") dolby: Int = 5,
        @Query("panorama") panorama: Int = 1,
        @Query("web_location") webLocation: String = "444.8",
        @Query("only_audio") onlyAudio: Int? = null,
        @QueryMap signedParams: Map<String, String> = emptyMap()
    ): LivePlayUrlResponse
    
    //  [新增] 旧版直播流 API - 可靠返回 quality_description 画质列表
    @GET("https://api.live.bilibili.com/room/v1/Room/playUrl")
    suspend fun getLivePlayUrlLegacy(
        @Query("cid") cid: Long,              // 房间号 (room_id)
        @Query("qn") qn: Int = 10000,         // 画质: 10000最高, 150高清, 80流畅
        @Query("platform") platform: String = "web"
    ): LivePlayUrlResponse

    //  [新增] 发送直播弹幕
    @retrofit2.http.FormUrlEncoded
    @retrofit2.http.POST("https://api.live.bilibili.com/msg/send")
    suspend fun sendLiveDanmaku(
        @retrofit2.http.QueryMap signedParams: Map<String, String> = emptyMap(),
        @retrofit2.http.Field("roomid") roomId: Long,
        @retrofit2.http.Field("msg") msg: String,
        @retrofit2.http.Field("color") color: Int = 16777215,
        @retrofit2.http.Field("fontsize") fontsize: Int = 25,
        @retrofit2.http.Field("mode") mode: Int = 1,
        @retrofit2.http.Field("bubble") bubble: Int = 0,
        @retrofit2.http.Field("room_type") roomType: Int = 0,
        @retrofit2.http.Field("jumpfrom") jumpFrom: Int = 0,
        @retrofit2.http.Field("reply_mid") replyMid: Long = 0,
        @retrofit2.http.Field("reply_attr") replyAttr: Int = 0,
        @retrofit2.http.Field("reply_uname") replyUname: String = "",
        @retrofit2.http.Field("replay_dmid") replayDmid: String = "",
        @retrofit2.http.Field("statistics") statistics: String = "{\"appId\":100,\"platform\":5}",
        @retrofit2.http.Field("reply_type") replyType: Int = 0,
        @retrofit2.http.Field("dm_type") dmType: Int? = null,
        @retrofit2.http.Field("emoticonOptions") emoticonOptions: String? = null,
        @retrofit2.http.Field("rnd") rnd: Long = System.currentTimeMillis() / 1000,
        @retrofit2.http.Field("csrf") csrf: String,
        @retrofit2.http.Field("csrf_token") csrfToken: String
    ): SimpleApiResponse


    //  [新增] 直播间点赞 (点亮/点赞上报)
    @retrofit2.http.FormUrlEncoded
    @retrofit2.http.POST("https://api.live.bilibili.com/xlive/web-ucenter/v1/like/like_report_v3")
    suspend fun clickLikeLiveRoom(
        @retrofit2.http.Field("click_time") clickTime: Int = 1, // 点击次数
        @retrofit2.http.Field("room_id") roomId: Long,
        @retrofit2.http.Field("uid") uid: Long,        // 当前用户 UID
        @retrofit2.http.Field("anchor_id") anchorId: Long, // 主播 UID
        @retrofit2.http.Field("csrf") csrf: String,
        @retrofit2.http.Field("csrf_token") csrfToken: String
    ): SimpleApiResponse

    //  [新增] 获取直播弹幕表情
    @GET("https://api.live.bilibili.com/xlive/web-ucenter/v2/emoticon/GetEmoticons")
    suspend fun getLiveEmoticons(
        @Query("platform") platform: String = "pc",
        @Query("room_id") roomId: Long
    ): com.android.purebilibili.data.model.response.LiveEmoticonRootResponse

    @retrofit2.http.FormUrlEncoded
    @retrofit2.http.POST("https://api.live.bilibili.com/liveact/shield_user")
    suspend fun shieldLiveUser(
        @retrofit2.http.Field("uid") uid: Long,
        @retrofit2.http.Field("roomid") roomId: Long,
        @retrofit2.http.Field("type") type: Int,
        @retrofit2.http.Field("csrf") csrf: String,
        @retrofit2.http.Field("csrf_token") csrfToken: String
    ): SimpleApiResponse

    @GET("https://api.live.bilibili.com/xlive/web-room/v1/index/getInfoByUser")
    suspend fun getLiveInfoByUser(
        @QueryMap params: Map<String, String>
    ): ResponseBody

    @retrofit2.http.FormUrlEncoded
    @retrofit2.http.POST("https://api.live.bilibili.com/liveact/user_silent")
    suspend fun setLiveSilentRule(
        @retrofit2.http.Field("type") type: String,
        @retrofit2.http.Field("level") level: Int,
        @retrofit2.http.Field("csrf") csrf: String,
        @retrofit2.http.Field("csrf_token") csrfToken: String
    ): SimpleApiResponse

    @retrofit2.http.FormUrlEncoded
    @retrofit2.http.POST("https://api.live.bilibili.com/xlive/web-ucenter/v1/banned/AddShieldKeyword")
    suspend fun addLiveShieldKeyword(
        @retrofit2.http.Field("keyword") keyword: String,
        @retrofit2.http.Field("csrf") csrf: String,
        @retrofit2.http.Field("csrf_token") csrfToken: String
    ): SimpleApiResponse

    @retrofit2.http.FormUrlEncoded
    @retrofit2.http.POST("https://api.live.bilibili.com/xlive/web-ucenter/v1/banned/DelShieldKeyword")
    suspend fun deleteLiveShieldKeyword(
        @retrofit2.http.Field("keyword") keyword: String,
        @retrofit2.http.Field("csrf") csrf: String,
        @retrofit2.http.Field("csrf_token") csrfToken: String
    ): SimpleApiResponse

    @retrofit2.http.FormUrlEncoded
    @retrofit2.http.POST("https://api.live.bilibili.com/xlive/web-ucenter/v1/dMReport/Report")
    suspend fun reportLiveDanmaku(
        @retrofit2.http.Field("id") id: Long = 0,
        @retrofit2.http.Field("roomid") roomId: Long,
        @retrofit2.http.Field("tuid") targetUid: Long,
        @retrofit2.http.Field("msg") message: String,
        @retrofit2.http.Field("reason") reason: String,
        @retrofit2.http.Field("ts") ts: Long,
        @retrofit2.http.Field("sign") sign: String,
        @retrofit2.http.Field("reason_id") reasonId: Int,
        @retrofit2.http.Field("token") token: String = "",
        @retrofit2.http.Field("dm_type") dmType: Int = 0,
        @retrofit2.http.Field("id_str") idStr: String,
        @retrofit2.http.Field("csrf") csrf: String,
        @retrofit2.http.Field("csrf_token") csrfToken: String,
        @retrofit2.http.Field("visit_id") visitId: String = ""
    ): SimpleApiResponse

    @retrofit2.http.FormUrlEncoded
    @retrofit2.http.POST("https://api.live.bilibili.com/av/v1/SuperChat/report")
    suspend fun reportLiveSuperChat(
        @retrofit2.http.Field("id") id: Long,
        @retrofit2.http.Field("roomid") roomId: Long,
        @retrofit2.http.Field("uid") uid: Long,
        @retrofit2.http.Field("msg") message: String,
        @retrofit2.http.Field("reason") reason: String,
        @retrofit2.http.Field("ts") ts: Long,
        @retrofit2.http.Field("sign") sign: String = "",
        @retrofit2.http.Field("reason_id") reasonId: String,
        @retrofit2.http.Field("token") token: String = "",
        @retrofit2.http.Field("id_str") idStr: String,
        @retrofit2.http.Field("csrf") csrf: String,
        @retrofit2.http.Field("csrf_token") csrfToken: String,
        @retrofit2.http.Field("visit_id") visitId: String = ""
    ): SimpleApiResponse

    @GET("https://api.live.bilibili.com/av/v1/SuperChat/getMessageList")
    suspend fun getLiveSuperChatMessages(
        @Query("room_id") roomId: Long
    ): ResponseBody

    @GET("https://api.live.bilibili.com/xlive/lottery-interface/v1/lottery/getLotteryInfoWeb")
    suspend fun getLiveLotteryInfo(
        @Query("roomid") roomId: Long
    ): ResponseBody

    @GET("https://api.live.bilibili.com/xlive/general-interface/v1/rank/queryContributionRank")
    suspend fun getLiveContributionRank(
        @QueryMap params: Map<String, String>
    ): LiveContributionRankResponse


    // ==================== 视频播放模块 ====================
    @GET("x/web-interface/view")
    suspend fun getVideoInfo(@Query("bvid") bvid: String): VideoDetailResponse
    
    // [修复] 通过 aid 获取视频信息 - 用于移动端推荐流（可能只返回 aid）
    @GET("x/web-interface/view")
    suspend fun getVideoInfoByAid(@Query("aid") aid: Long): VideoDetailResponse
    
    @GET("x/tag/archive/tags")
    suspend fun getVideoTags(@Query("bvid") bvid: String): VideoTagResponse

    // [新增] 获取 AI 视频总结 (WBI 签名)
    @GET("x/web-interface/view/conclusion/get")
    suspend fun getAiConclusion(@QueryMap params: Map<String, String>): AiSummaryResponse


    @GET("x/player/wbi/playurl")
    suspend fun getPlayUrl(@QueryMap params: Map<String, String>): PlayUrlResponse
    
    //  HTML5 降级方案 (无 Referer 鉴权，仅 MP4 格式)
    @GET("x/player/wbi/playurl")
    suspend fun getPlayUrlHtml5(@QueryMap params: Map<String, String>): PlayUrlResponse
    
    //  [新增] 上报播放心跳（记录播放历史）
    @retrofit2.http.FormUrlEncoded
    @POST("x/click-interface/web/heartbeat")
    suspend fun reportHeartbeat(
        @retrofit2.http.FieldMap fields: Map<String, String>
    ): BaseResponse

    //  [新增] 无 WBI 签名的旧版 API (可能绕过 412)
    @GET("x/player/playurl")
    suspend fun getPlayUrlLegacy(
        @Query("bvid") bvid: String,
        @Query("cid") cid: Long,
        @Query("qn") qn: Int = 80,
        @Query("fnval") fnval: Int = 16,  // MP4 格式
        @Query("fnver") fnver: Int = 0,
        @Query("fourk") fourk: Int = 1,
        @Query("platform") platform: String = "html5",
        @Query("high_quality") highQuality: Int = 1
    ): PlayUrlResponse
    
    //  [新增] 通过 aid 获取播放地址 - 用于 Story 模式
    @GET("x/player/playurl")
    suspend fun getPlayUrlByAid(
        @Query("avid") aid: Long,
        @Query("cid") cid: Long,
        @Query("qn") qn: Int = 80,
        @Query("fnval") fnval: Int = 16,  // MP4 格式
        @Query("fnver") fnver: Int = 0,
        @Query("fourk") fourk: Int = 1,
        @Query("platform") platform: String = "html5",
        @Query("high_quality") highQuality: Int = 1
    ): PlayUrlResponse
    
    //  [新增] APP playurl API - 使用 access_token 获取高画质视频流 (4K/HDR/1080P60)
    @GET("https://api.bilibili.com/x/player/playurl")
    suspend fun getPlayUrlApp(@QueryMap params: Map<String, String>): PlayUrlResponse

    //  [新增] TV 投屏 playurl（投屏优先使用）
    @GET("x/tv/playurl")
    suspend fun getTvPlayUrl(@QueryMap params: Map<String, String>): PlayUrlResponse

    @GET("x/player/videoshot")
    suspend fun getVideoshot(
        @Query("bvid") bvid: String,
        @Query("cid") cid: Long,
        @Query("index") index: Int = 1  // 是否返回时间索引，1=是
    ): VideoshotResponse

    @GET("https://bvc.bilivideo.com/pbp/data")
    suspend fun getPbpData(
        @Query("cid") cid: Long,
        @Query("bvid") bvid: String? = null,
        @Query("aid") aid: Long? = null
    ): ResponseBody
    
    //  [修复] 获取播放器信息（包含章节/看点数据）— 使用 WBI 签名版本
    @GET("x/player/wbi/v2")
    suspend fun getPlayerInfo(
        @QueryMap params: Map<String, String>
    ): PlayerInfoResponse

    //  [新增] 获取视频的完整 BGM 列表
    @GET("x/copyright-music-publicity/bgm/multiple/music")
    suspend fun getBgmMultipleMusic(
        @Query("aid") aid: Long,
        @Query("cid") cid: Long
    ): com.android.purebilibili.data.model.response.BgmMultipleMusicResponse

    @GET("x/copyright-music-publicity/bgm/detail")
    suspend fun getBgmDetail(
        @Query("music_id") musicId: String,
        @Query("aid") aid: Long,
        @Query("cid") cid: Long
    ): com.android.purebilibili.data.model.response.BgmDetailResponse

    @GET("x/copyright-music-publicity/bgm/recommend_list")
    suspend fun getBgmRecommendList(
        @Query("music_id") musicId: String,
        @Query("aid") aid: Long,
        @Query("cid") cid: Long,
        @Query("pn") pn: Int = 1,
        @Query("ps") ps: Int = 5
    ): com.android.purebilibili.data.model.response.BgmRecommendListResponse

    @GET("x/stein/edgeinfo_v2")
    suspend fun getInteractEdgeInfo(
        @Query("bvid") bvid: String,
        @Query("graph_version") graphVersion: Long,
        @Query("edge_id") edgeId: Long? = null
    ): InteractEdgeInfoResponse

    @GET("x/web-interface/archive/related")
    suspend fun getRelatedVideos(@Query("bvid") bvid: String): RelatedResponse

    //  [修复] 使用 comment.bilibili.com 弹幕端点，避免 412 错误
    @GET("https://comment.bilibili.com/{cid}.xml")
    suspend fun getDanmakuXml(@retrofit2.http.Path("cid") cid: Long): ResponseBody
    
    //  [新增] Protobuf 弹幕 API - 分段加载 (每段 6 分钟)
    @GET("https://api.bilibili.com/x/v2/dm/web/seg.so")
    suspend fun getDanmakuSeg(
        @Query("type") type: Int = 1,              // 视频类型: 1=视频
        @Query("oid") oid: Long,                   // cid
        @Query("segment_index") segmentIndex: Int  // 分段索引 (从 1 开始)
    ): ResponseBody
    
    // [新增] 弹幕元数据 API (x/v2/dm/web/view)
    // 返回 DmWebViewReply Protobuf 数据，包含高级弹幕、代码弹幕 URL、互动指令等
    @GET("https://api.bilibili.com/x/v2/dm/web/view")
    suspend fun getDanmakuView(
        @Query("type") type: Int = 1,
        @Query("oid") oid: Long,
        @Query("pid") pid: Long
    ): ResponseBody

    // [新增] 同步弹幕个人配置（账号云同步）
    @retrofit2.http.FormUrlEncoded
    @retrofit2.http.POST("x/v2/dm/web/config")
    suspend fun updateDanmakuWebConfig(
        @retrofit2.http.Field("dm_switch") dmSwitch: String,
        @retrofit2.http.Field("blockscroll") blockScroll: String,
        @retrofit2.http.Field("blocktop") blockTop: String,
        @retrofit2.http.Field("blockbottom") blockBottom: String,
        @retrofit2.http.Field("blockcolor") blockColor: String,
        @retrofit2.http.Field("blockspecial") blockSpecial: String,
        @retrofit2.http.Field("opacity") opacity: Float,
        @retrofit2.http.Field("dmarea") dmArea: Int,
        @retrofit2.http.Field("speedplus") speedPlus: Float,
        @retrofit2.http.Field("fontsize") fontSize: Float,
        @retrofit2.http.Field("csrf") csrf: String
    ): SimpleApiResponse
    
    // [新增] 发送弹幕
    @retrofit2.http.FormUrlEncoded
    @retrofit2.http.POST("x/v2/dm/post")
    suspend fun sendDanmaku(
        @retrofit2.http.Field("oid") oid: Long,               // 视频 cid
        @retrofit2.http.Field("aid") aid: Long,               // 视频 aid (必需)
        @retrofit2.http.Field("type") type: Int = 1,          // 弹幕类型: 1=视频
        @retrofit2.http.Field("msg") msg: String,             // 弹幕内容
        @retrofit2.http.Field("progress") progress: Long,      // 弹幕出现时间 (毫秒)
        @retrofit2.http.Field("color") color: Int = 16777215,  // 颜色 (十进制RGB，默认白色)
        @retrofit2.http.Field("fontsize") fontsize: Int = 25,  // 字号: 18小/25中/36大
        @retrofit2.http.Field("mode") mode: Int = 1,           // 模式: 1滚动/4底部/5顶部
        @retrofit2.http.Field("pool") pool: Int = 0,           // 弹幕池: 0普通/1字幕/2特殊
        @retrofit2.http.Field("colorful") colorful: Int? = null, // 60001=大会员渐变彩色
        @retrofit2.http.Field("checkbox_type") checkboxType: Int? = null, // 4=UP身份标识
        @retrofit2.http.Field("plat") plat: Int = 1,           // 平台: 1=web
        @retrofit2.http.Field("csrf") csrf: String
    ): SendDanmakuResponse

    @retrofit2.http.FormUrlEncoded
    @retrofit2.http.POST("x/v2/dm/command/post")
    suspend fun sendCommandDanmaku(
        @retrofit2.http.Field("type") type: Int,
        @retrofit2.http.Field("aid") aid: Long,
        @retrofit2.http.Field("cid") cid: Long,
        @retrofit2.http.Field("progress") progress: Long,
        @retrofit2.http.Field("plat") plat: Int = 1,
        @retrofit2.http.Field("data") data: String,
        @retrofit2.http.Field("csrf") csrf: String
    ): CommandDanmakuResponse

    @GET
    suspend fun getDanmakuSpecialDm(@retrofit2.http.Url url: String): ResponseBody

    // [新增] 打分弹幕提交 (x/v2/dm/command/grade/post)
    // 互动投票/打分弹幕的提交端点；grade_score 为偶数，最大 10
    @retrofit2.http.FormUrlEncoded
    @retrofit2.http.POST("x/v2/dm/command/grade/post")
    suspend fun gradeDanmaku(
        @retrofit2.http.Field("aid") aid: Long,               // 稿件 aid
        @retrofit2.http.Field("cid") cid: Long,               // 分P cid
        @retrofit2.http.Field("progress") progress: Long,      // 弹幕出现时间 (毫秒)
        @retrofit2.http.Field("grade_id") gradeId: Long,       // 打分/投票 ID
        @retrofit2.http.Field("grade_score") gradeScore: Int,  // 分数 (偶数，最大 10)
        @retrofit2.http.Field("csrf") csrf: String
    ): com.android.purebilibili.data.model.response.SimpleApiResponse

    // [新增] 撤回弹幕 (2分钟内可撤回，每天3次)
    @retrofit2.http.FormUrlEncoded
    @retrofit2.http.POST("x/dm/recall")
    suspend fun recallDanmaku(
        @retrofit2.http.Field("cid") cid: Long,               // 视频 cid
        @retrofit2.http.Field("dmid") dmid: Long,             // 弹幕 ID
        @retrofit2.http.Field("csrf") csrf: String
    ): DanmakuActionResponse

    // [新增] 点赞弹幕
    @retrofit2.http.FormUrlEncoded
    @retrofit2.http.POST("x/v2/dm/thumbup/add")
    suspend fun likeDanmaku(
        @retrofit2.http.Field("oid") oid: Long,               // 视频 cid
        @retrofit2.http.Field("dmid") dmid: Long,             // 弹幕 ID
        @retrofit2.http.Field("op") op: Int = 1,              // 操作: 1点赞/2取消
        @retrofit2.http.Field("platform") platform: String = "web_player",
        @retrofit2.http.Field("csrf") csrf: String
    ): DanmakuActionResponse

    // [新增] 查询弹幕点赞状态与票数
    @GET("x/v2/dm/thumbup/stats")
    suspend fun getDanmakuThumbupStats(
        @Query("oid") oid: Long,                              // 视频 cid
        @Query("ids") ids: String                             // 逗号分隔 dmid 列表
    ): com.android.purebilibili.data.model.response.DanmakuThumbupStatsResponse

    // [新增] 举报弹幕
    @retrofit2.http.FormUrlEncoded
    @retrofit2.http.POST("x/dm/report/add")
    suspend fun reportDanmaku(
        @retrofit2.http.Field("cid") cid: Long,               // 视频 cid
        @retrofit2.http.Field("dmid") dmid: Long,             // 弹幕 ID
        @retrofit2.http.Field("reason") reason: Int,          // 举报原因
        @retrofit2.http.Field("content") content: String = "", // 举报内容描述
        @retrofit2.http.Field("csrf") csrf: String
    ): DanmakuActionResponse

    // ==================== 评论模块 ====================
    // 评论主列表 (需 WBI 签名)
    @GET("x/v2/reply/wbi/main")
    suspend fun getReplyList(@QueryMap params: Map<String, String>): ReplyResponse

    // 评论主列表兼容链路
    @GET("x/v2/reply/main")
    suspend fun getReplyListMain(@QueryMap params: Map<String, String>): ReplyResponse
    
    //  [新增] 旧版评论 API - 用于时间排序 (sort=0)
    // 此 API 不需要 WBI 签名，分页更稳定
    @GET("x/v2/reply")
    suspend fun getReplyListLegacy(
        @Query("oid") oid: Long,
        @Query("type") type: Int = 1,
        @Query("pn") pn: Int = 1,
        @Query("ps") ps: Int = 20,
        @Query("sort") sort: Int = 0  // 0=按时间, 1=按点赞数, 2=按回复数
    ): ReplyResponse

    @GET("x/v2/reply/reply")
    suspend fun getReplyReply(
        @Query("oid") oid: Long,
        @Query("type") type: Int = 1,
        @Query("root") root: Long, // 根评论 ID (rpid)
        @Query("pn") pn: Int,     // 页码
        @Query("ps") ps: Int = 20 // 每页数量
    ): ReplyResponse

    @GET("x/v2/reply/count")
    suspend fun getReplyCount(
        @Query("oid") oid: Long,
        @Query("type") type: Int
    ): ReplyCountResponse

    @GET("x/polymer/web-dynamic/v1/mention/search")
    suspend fun searchMentionUsers(
        @Query("keyword") keyword: String? = null
    ): MentionSearchResponse

    // [新增] 发送评论
    @retrofit2.http.FormUrlEncoded
    @retrofit2.http.POST("x/v2/reply/add")
    suspend fun addReply(
        @retrofit2.http.Field("oid") oid: Long,
        @retrofit2.http.Field("type") type: Int = 1,
        @retrofit2.http.Field("message") message: String,
        @retrofit2.http.Field("plat") plat: Int = 1,
        @retrofit2.http.Field("root") root: Long? = null,
        @retrofit2.http.Field("parent") parent: Long? = null,
        @retrofit2.http.Field("pictures") pictures: String? = null,
        @retrofit2.http.Field("sync_to_dynamic") syncToDynamic: Int? = null,
        @retrofit2.http.Field("csrf") csrf: String
    ): AddReplyResponse

    // [新增] 评论图片上传（复用动态图片上传接口）
    @retrofit2.http.Multipart
    @retrofit2.http.POST("x/dynamic/feed/draw/upload_bfs")
    suspend fun uploadCommentImage(
        @retrofit2.http.Part fileUp: okhttp3.MultipartBody.Part,
        @retrofit2.http.Part("category") category: okhttp3.RequestBody,
        @retrofit2.http.Part("biz") biz: okhttp3.RequestBody,
        @retrofit2.http.Part("csrf") csrf: okhttp3.RequestBody
    ): UploadCommentImageResponse

    @retrofit2.http.Multipart
    @retrofit2.http.POST("x/dynamic/feed/draw/upload_bfs")
    suspend fun uploadPrivateMessageImage(
        @retrofit2.http.Part fileUp: okhttp3.MultipartBody.Part,
        @retrofit2.http.Part("biz") biz: okhttp3.RequestBody,
        @retrofit2.http.Part("csrf") csrf: okhttp3.RequestBody
    ): UploadCommentImageResponse

    /**
     * 获取表情包
     */
    @GET("x/emote/user/panel/web")
    suspend fun getEmotes(
        @QueryMap params: Map<String, String>
    ): com.android.purebilibili.data.model.response.EmoteResponse
    
    // [新增] 点赞评论
    @retrofit2.http.FormUrlEncoded
    @retrofit2.http.POST("x/v2/reply/action")
    suspend fun likeReply(
        @retrofit2.http.Field("oid") oid: Long,
        @retrofit2.http.Field("type") type: Int = 1,
        @retrofit2.http.Field("rpid") rpid: Long,
        @retrofit2.http.Field("action") action: Int,
        @retrofit2.http.Field("csrf") csrf: String
    ): SimpleApiResponse
    
    // [新增] 点踩评论
    @retrofit2.http.FormUrlEncoded
    @retrofit2.http.POST("x/v2/reply/hate")
    suspend fun hateReply(
        @retrofit2.http.Field("oid") oid: Long,
        @retrofit2.http.Field("type") type: Int = 1,
        @retrofit2.http.Field("rpid") rpid: Long,
        @retrofit2.http.Field("action") action: Int,
        @retrofit2.http.Field("csrf") csrf: String
    ): SimpleApiResponse
    
    // [新增] 删除评论
    @retrofit2.http.FormUrlEncoded
    @retrofit2.http.POST("x/v2/reply/del")
    suspend fun deleteReply(
        @retrofit2.http.Field("oid") oid: Long,
        @retrofit2.http.Field("type") type: Int = 1,
        @retrofit2.http.Field("rpid") rpid: Long,
        @retrofit2.http.Field("csrf") csrf: String
    ): SimpleApiResponse

    @retrofit2.http.FormUrlEncoded
    @retrofit2.http.POST("x/v2/reply/top")
    suspend fun setReplyTop(
        @retrofit2.http.Field("oid") oid: Long,
        @retrofit2.http.Field("type") type: Int = 1,
        @retrofit2.http.Field("rpid") rpid: Long,
        @retrofit2.http.Field("action") action: Int,
        @retrofit2.http.Field("csrf") csrf: String
    ): SimpleApiResponse
    
    // [新增] 举报评论
    @retrofit2.http.FormUrlEncoded
    @retrofit2.http.POST("x/v2/reply/report")
    suspend fun reportReply(
        @retrofit2.http.Field("oid") oid: Long,
        @retrofit2.http.Field("type") type: Int = 1,
        @retrofit2.http.Field("rpid") rpid: Long,
        @retrofit2.http.Field("reason") reason: Int,
        @retrofit2.http.Field("content") content: String = "",
        @retrofit2.http.Field("csrf") csrf: String
    ): SimpleApiResponse
    
    // ==================== 用户交互模块 ====================
    // 查询与 UP 主的关注关系
    @GET("x/relation")
    suspend fun getRelation(
        @Query("fid") fid: Long  // UP 主 mid
    ): RelationResponse

    @GET("x/relation/tags")
    suspend fun getRelationTags(): RelationTagsResponse

    @GET("x/relation/blacks")
    suspend fun getRelationBlacks(
        @Query("ps") pageSize: Int = 50,
        @Query("pn") page: Int = 1
    ): com.android.purebilibili.data.model.response.RelationBlacksResponse

    @GET("x/relation/tag/user")
    suspend fun getRelationTagUser(
        @Query("fid") fid: Long
    ): RelationTagUserResponse

    @GET("x/relation/tag")
    suspend fun getRelationTagMembers(
        @Query("tagid") tagId: Long,
        @Query("order_type") orderType: String = "",
        @Query("ps") pageSize: Int = 100,
        @Query("pn") page: Int = 1
    ): com.android.purebilibili.data.model.response.RelationTagMembersResponse

    @GET("x/relation/tag")
    suspend fun getRelationTagFollowingUsers(
        @Query("tagid") tagId: Long,
        @Query("order_type") orderType: String = "",
        @Query("ps") pageSize: Int = 100,
        @Query("pn") page: Int = 1
    ): com.android.purebilibili.data.model.response.RelationTagFollowingsResponse
    
    //  [新增] 查询视频是否已收藏
    @GET("x/v2/fav/video/favoured")
    suspend fun checkFavoured(
        @Query("aid") aid: Long
    ): FavouredResponse

    @GET("x/web-interface/archive/relation")
    suspend fun getVideoRelation(
        @Query("aid") aid: Long? = null,
        @Query("bvid") bvid: String? = null
    ): VideoRelationResponse
    
    //  [新增] 关注/取关 UP 主
    @retrofit2.http.FormUrlEncoded
    @retrofit2.http.POST("x/relation/modify")
    suspend fun modifyRelation(
        @retrofit2.http.Field("fid") fid: Long,      // UP 主 mid
        @retrofit2.http.Field("act") act: Int,        // 1=关注, 2=取关, 5=拉黑, 6=解除拉黑
        @retrofit2.http.Field("csrf") csrf: String,
        @retrofit2.http.Field("re_src") reSrc: Int? = null
    ): SimpleApiResponse

    @retrofit2.http.FormUrlEncoded
    @retrofit2.http.POST("x/relation/tags/addUsers")
    suspend fun addUsersToRelationTags(
        @retrofit2.http.Field("fids") fids: String,
        @retrofit2.http.Field("tagids") tagIds: String,
        @retrofit2.http.Field("csrf") csrf: String
    ): SimpleApiResponse
    
    //  [新增] 收藏/取消收藏视频
    @retrofit2.http.FormUrlEncoded
    @retrofit2.http.POST("x/v3/fav/resource/deal")
    suspend fun dealFavorite(
        @retrofit2.http.Field("rid") rid: Long,                    // 视频 aid
        @retrofit2.http.Field("type") type: Int = 2,               // 资源类型 2=视频
        @retrofit2.http.Field("add_media_ids") addIds: String = "", // 添加到的收藏夹 ID
        @retrofit2.http.Field("del_media_ids") delIds: String = "", // 从收藏夹移除
        @retrofit2.http.Field("csrf") csrf: String
    ): SimpleApiResponse

    @retrofit2.http.FormUrlEncoded
    @retrofit2.http.POST("x/v3/fav/season/fav")
    suspend fun favoriteSeason(
        @retrofit2.http.Field("platform") platform: String = "web",
        @retrofit2.http.Field("season_id") seasonId: Long,
        @retrofit2.http.Field("csrf") csrf: String
    ): SimpleApiResponse

    @retrofit2.http.FormUrlEncoded
    @retrofit2.http.POST("x/v3/fav/season/unfav")
    suspend fun unfavoriteSeason(
        @retrofit2.http.Field("platform") platform: String = "web",
        @retrofit2.http.Field("season_id") seasonId: Long,
        @retrofit2.http.Field("csrf") csrf: String
    ): SimpleApiResponse
    
    //  [新增] 点赞/取消点赞视频
    @retrofit2.http.FormUrlEncoded
    @retrofit2.http.POST("x/web-interface/archive/like")
    suspend fun likeVideo(
        @retrofit2.http.Field("aid") aid: Long,
        @retrofit2.http.Field("like") like: Int,   // 1=点赞, 2=取消点赞
        @retrofit2.http.Field("csrf") csrf: String
    ): SimpleApiResponse
    
    //  [新增] 查询是否已点赞
    @GET("x/web-interface/archive/has/like")
    suspend fun hasLiked(
        @Query("aid") aid: Long
    ): HasLikedResponse
    
    //  [新增] 投币
    @retrofit2.http.FormUrlEncoded
    @retrofit2.http.POST("x/web-interface/coin/add")
    suspend fun coinVideo(
        @retrofit2.http.Field("aid") aid: Long,
        @retrofit2.http.Field("multiply") multiply: Int,       // 投币数量 1 或 2
        @retrofit2.http.Field("select_like") selectLike: Int,  // 1=同时点赞, 0=不点赞
        @retrofit2.http.Field("csrf") csrf: String
    ): SimpleApiResponse
    
    //  [新增] 查询已投币数
    @GET("x/web-interface/archive/coins")
    suspend fun hasCoined(
        @Query("aid") aid: Long
    ): HasCoinedResponse
    
    //  [新增] 获取关注列表（用于首页显示"已关注"标签）
    @GET("x/relation/followings")
    suspend fun getFollowings(
        @Query("vmid") vmid: Long,        // 用户 mid
        @Query("pn") pn: Int = 1,         // 页码
        @Query("ps") ps: Int = 50,        // 每页数量（最大 50）
        @Query("order_type") orderType: String = ""  // 按关注顺序
    ): FollowingsResponse
    
    //  [官方适配] 获取视频在线观看人数
    @GET("x/player/online/total")
    suspend fun getOnlineCount(
        @Query("bvid") bvid: String,
        @Query("cid") cid: Long
    ): OnlineResponse
    
    // ==================== 稍后再看模块 ====================
    @GET("x/v2/history/toview")
    suspend fun getWatchLaterList(): WatchLaterResponse

    @GET("x/v2/history/toview/web")
    suspend fun getWatchLaterPage(
        @QueryMap params: Map<String, String>,
    ): WatchLaterResponse
    
    //  [新增] 添加到稍后再看
    @retrofit2.http.FormUrlEncoded
    @retrofit2.http.POST("x/v2/history/toview/add")
    suspend fun addToWatchLater(
        @retrofit2.http.Field("aid") aid: Long,
        @retrofit2.http.Field("csrf") csrf: String
    ): SimpleApiResponse
    
    //  [新增] 从稍后再看删除
    @retrofit2.http.FormUrlEncoded
    @retrofit2.http.POST("x/v2/history/toview/del")
    suspend fun deleteFromWatchLater(
        @retrofit2.http.Field("aid") aid: Long? = null,
        @retrofit2.http.Field("viewed") viewed: Boolean? = null,
        @retrofit2.http.Field("csrf") csrf: String
    ): SimpleApiResponse

    @retrofit2.http.FormUrlEncoded
    @retrofit2.http.POST("x/v2/history/toview/del")
    suspend fun deleteMultipleFromWatchLater(
        @retrofit2.http.Field("aid") aids: String,
        @retrofit2.http.Field("csrf") csrf: String,
    ): SimpleApiResponse

    @retrofit2.http.FormUrlEncoded
    @retrofit2.http.POST("x/v2/history/toview/clear")
    suspend fun clearWatchLater(
        @retrofit2.http.Field("clean_type") cleanType: Int? = null,
        @retrofit2.http.Field("csrf") csrf: String
    ): SimpleApiResponse

    @retrofit2.http.FormUrlEncoded
    @retrofit2.http.POST("x/v2/history/toview/copy")
    suspend fun copyWatchLaterToFavorite(
        @retrofit2.http.Field("tar_media_id") targetMediaId: Long,
        @retrofit2.http.Field("mid") mid: Long,
        @retrofit2.http.Field("resources") resources: String,
        @retrofit2.http.Field("platform") platform: String = "web",
        @retrofit2.http.Field("csrf") csrf: String,
    ): SimpleApiResponse

    @retrofit2.http.FormUrlEncoded
    @retrofit2.http.POST("x/v2/history/toview/move")
    suspend fun moveWatchLaterToFavorite(
        @retrofit2.http.Field("tar_media_id") targetMediaId: Long,
        @retrofit2.http.Field("resources") resources: String,
        @retrofit2.http.Field("platform") platform: String = "web",
        @retrofit2.http.Field("csrf") csrf: String,
    ): SimpleApiResponse
}
