package com.bureau.network

import android.content.Context
import com.bureau.utils.isInternetAvailable
import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.util.concurrent.TimeUnit


/**
 * Created by Abhin
 */
open class NetworkInterceptor(context: Context) : Interceptor {
    private val appContext = context.applicationContext
    /**
     * Overridden method for intercept
     */
    override fun intercept(chain: Interceptor.Chain): Response = chain.run {
        val request: Request
        val cc: CacheControl = if (!isInternetAvailable(appContext)) {
            CacheControl.Builder()
                .onlyIfCached()
                .maxStale(30, TimeUnit.DAYS)
                .build()
        } else {
            CacheControl.Builder()
                .maxAge(0, TimeUnit.SECONDS)
                .build()
        }
        request = request().newBuilder()
            .cacheControl(cc)
            .build()
        proceed(request)
    }
}

