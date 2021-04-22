package com.bureau.network

import android.content.Context
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Created by Abhin.
 * Api Client for retrofit Instance with Base Url
 */
class APIClient(var context : Context) {

    private var mRetrofit: Retrofit? = null

    /**
     * getClient Retorfit instance
     * @param mContext use for store caching
     */
    fun getClient(): RetrofitInterface {
        if (mRetrofit == null) {
            mRetrofit = Retrofit.Builder().baseUrl(BASE_URL).client(getOkHttpClient()).addCallAdapterFactory(RxJava2CallAdapterFactory.create()).addConverterFactory(GsonConverterFactory.create(Gson())).build()
        }
        return mRetrofit?.create(RetrofitInterface::class.java)!!
    }

    /**
     * getOkHttpClient
     */
    private fun getOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor()
        val originInterceptor = OriginHeaderInterceptor()

        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        return OkHttpClient.Builder().connectTimeout(3, TimeUnit.MINUTES).readTimeout(90, TimeUnit.SECONDS).writeTimeout(45, TimeUnit.SECONDS).addInterceptor(originInterceptor).addInterceptor(loggingInterceptor).addNetworkInterceptor(NetworkInterceptor(context)).build()
    }
}
