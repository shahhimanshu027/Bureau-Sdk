package com.bureau.models.callFilter.request

/**
 * Created by Abhin.
 */
data class SmsFilterRequest(var sms_reciever: String? = null, var sms_sender: String? = null, var sms_body: String? = null)
