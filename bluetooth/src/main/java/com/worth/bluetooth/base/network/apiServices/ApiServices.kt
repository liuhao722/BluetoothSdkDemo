package com.worth.bluetooth.base.network.apiServices

import com.worth.bluetooth.base.network.bean.ResultData
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Author:  LiuHao
 * Email:   114650501@qq.com
 * TIME:    5/30/21 --> 8:20 PM
 * Description: This is ApiServices
 */
interface ApiServices {
    /**
     * 获取网络查询内容的返回值
     */
    @POST
    fun getRefResult(@Body requestBody: RequestBody?): Call<ResultData?>?
//    @POST("/api/v1/service/chat")
//    fun getRefResult(@Body requestBody: RequestBody?): Call<ResultData?>?
}