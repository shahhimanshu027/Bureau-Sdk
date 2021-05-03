package com.bureau.network

import com.bureau.models.callFilter.request.CallFilterRequest
import com.bureau.models.callFilter.request.SmsFilterRequest
import com.bureau.models.callFilter.request.UrlFilterRequest
import com.bureau.models.callFilter.response.CommonFilterResponse
import com.bureau.models.packageDetectorHelper.AllInstalledAppResponse
import com.bureau.models.packageDetectorHelper.AppList
import com.bureau.models.packageDetectorHelper.InstalledAppRequest
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST


/**
 * Created by Abhin.
 */
interface RetrofitInterface {

    @POST("androidapi/callfilter")
    suspend fun callFilterApi(@Body requestBody: CallFilterRequest): Response<CommonFilterResponse>

    @POST("androidapi/smsfilter")
    suspend fun smsFilterApi(@Body requestBody: SmsFilterRequest): Response<CommonFilterResponse>

    @POST("androidapi/appfilter")
    suspend fun allInstalledAppDataApi(@Body requestBody: ArrayList<AppList>): Response<ArrayList<AllInstalledAppResponse>>

    @POST("")
    suspend fun simCardNumNotifyApi(): Response<ResponseBody>

    @POST("androidapi/urlfilter")
    suspend fun urlFilterApi(@Body requestBody: UrlFilterRequest): Response<CommonFilterResponse>

}

