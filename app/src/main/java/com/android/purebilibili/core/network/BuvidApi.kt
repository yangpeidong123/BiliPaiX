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
data class BuvidSpiData(
    val b_3: String = "",  // buvid3
    val b_4: String = ""   // buvid4
)

@kotlinx.serialization.Serializable
data class BuvidSpiResponse(
    val code: Int = 0,
    val data: BuvidSpiData? = null
)

//  [新增] Buvid API
interface BuvidApi {
    @GET("x/frontend/finger/spi")
    suspend fun getSpi(): BuvidSpiResponse
    
    //  Buvid 激活 (PiliPala 中关键的一步)
    @retrofit2.http.FormUrlEncoded
    @POST("x/internal/gaia-gateway/ExClimbWuzhi")
    suspend fun activateBuvid(
        @retrofit2.http.Field("payload") payload: String
    ): SimpleApiResponse
}
