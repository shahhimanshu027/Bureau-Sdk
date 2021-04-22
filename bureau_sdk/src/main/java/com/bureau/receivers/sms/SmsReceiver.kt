package com.bureau.receivers.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsMessage
import com.bureau.utils.ApiCallType
import com.bureau.utils.MY_PREFERENCE
import com.bureau.utils.PreferenceManager
import com.bureau.utils.startNumberDetectionService


/**
 * Created by Abhin.
 * SmsReceiver is used to get event on any SMS is coming
 */

class SmsReceiver : BroadcastReceiver() {

    private var preferenceManager: PreferenceManager? = null

    override fun onReceive(context: Context?, intent: Intent?) {
        preferenceManager = PreferenceManager(context!!.getSharedPreferences(MY_PREFERENCE, Context.MODE_PRIVATE))
        val bundle = intent?.extras
        try {
            if (bundle != null) {
                val pdusObj: Array<Any>? = bundle["pdus"] as Array<Any>?
                for (i in pdusObj!!.indices) {
                    //Get current sms
                    val currentMessage: SmsMessage = SmsMessage.createFromPdu(pdusObj[i] as ByteArray)
                    //Get current sms number
                    val phoneNumber: String = currentMessage.displayOriginatingAddress
                    //Get sms body
                    val message: String = currentMessage.displayMessageBody
                    // Starting the service to get the valid or invalid number
                    startNumberDetectionService(context = context, number = phoneNumber, apiCallType = ApiCallType.SMS.name, message = message)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
