package com.worth.bluetooth.base.network

import com.worth.bluetooth.base.core.storage.MeKV
import com.worth.bluetooth.base.core.utils.L
import com.worth.bluetooth.base.network.apiServices.ApiServices
import com.worth.bluetooth.business.global.mHttpBody
import com.worth.bluetooth.business.global.mHttpHeaders
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.await
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Author:  LiuHao
 * Email:   114650501@qq.com
 * TIME:    5/30/21 --> 8:15 PM
 * Description: This is RetrofitUtils
 */
class RetrofitUtils private constructor() {
    private val baseUrl = MeKV.getHost()
    private val json = MediaType.parse("application/json; charset=utf-8")

    /**
     * 网络请求
     */
    fun requestServer(queryWord: String?, block: (String) -> Unit) {
        GlobalScope.launch {
            val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(addHeaders())
                .build()
            val retrofitApi = retrofit.create(ApiServices::class.java)
            var map: HashMap<String, Any?> = HashMap()
            map[MeKV.getAiInstructionSetKey()] = queryWord
            mHttpBody?.mapKeys {
                map.put(it.key, it.value)
            }

            try {
                val jsonStr = JSONObject(map).toString()
                val body = RequestBody.create(json, jsonStr)
                val result = retrofitApi.getRefResult(body)?.await()
                result?.run {           //  返回结果不为空
                    L.e(TAG, toString())
                    when (code) {
                        200 -> {
                            block.invoke(this.result)
                        }
                        else -> {
                            block.invoke("")
                        }
                    }
                } ?: run {
                    block.invoke("")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                block.invoke("")
            }
        }
    }

    /**
     * 对头部进行操作
     */
    private fun addHeaders(): OkHttpClient {
        val header = mHttpHeaders
        val httpClient = OkHttpClient.Builder()
        httpClient.addInterceptor { chain ->
            val original = chain.request()
            val request = original.newBuilder()
            header?.mapKeys {
                request.header(it.key, it?.value as String)
            }
            request.method(original.method(), original.body())
            chain.proceed(request.build())
        }
        return httpClient.build()
    }

    private object SingletonHolder {
        var instance = RetrofitUtils()
    }

    companion object {
        private const val TAG = "RetrofitUtils"

        @JvmStatic
        fun ins(): RetrofitUtils {
            return SingletonHolder.instance
        }
    }
}