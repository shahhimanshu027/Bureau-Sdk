package com.bureau.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import com.bureau.`interface`.SMSFilterInterface
import com.bureau.models.callFilter.request.SmsFilterRequest
import com.bureau.network.APIClient
import com.bureau.utils.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Created by Abhin.
 * SmsFilteringService is used to check the number is valid or is spam or blocked for sms.
 */

class SmsFilteringService : Service() {

    private var number: String? = null
    private var smsTextBody: String? = null

    companion object {

        private var preferenceManager: PreferenceManager? = null
        var mSMSFilterInterface: SMSFilterInterface? = null

        //Called from My Activity for save the user number in preference.
        fun initSmsFilterService(
            context: Context,
            userNumber: String,
            smsFilterInterface: SMSFilterInterface
        ) {
            this.mSMSFilterInterface = smsFilterInterface
            preferenceManager =
                PreferenceManager(context.getSharedPreferences(MY_PREFERENCE, Context.MODE_PRIVATE))
            preferenceManager?.setValue(PREF_USER_MOBILE, userNumber)
        }

        // start the service if it is not already running.
        fun startSmsFilteringService(context: Context, intent: Intent) {
            try {
                //check if service is not running then start the service
                if (!isMyServiceRunning(context, SmsFilteringService::class.java)) {
                    context.startService(intent)
                }
            } catch (e: IllegalStateException) {
                //if exception will come check the OS version and if it's 8 or above, start foreground service
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    throw IllegalStateException()
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (preferenceManager == null) {
            preferenceManager = PreferenceManager(
                getSharedPreferences(
                    MY_PREFERENCE,
                    Context.MODE_PRIVATE
                )
            )
        }
        //Get number
        number = intent?.getStringExtra(KEY_NUMBER)
        //Get SMS body
        smsTextBody = intent?.getStringExtra(KEY_SMS_BODY)
        identifyNumber()
        return super.onStartCommand(intent, flags, startId)
    }

    private fun identifyNumber() {
        val userNumber = preferenceManager?.getValue(PREF_USER_MOBILE, "12345")

        when {
            //Check if the number is in contact list or not
            number != null && contactExists(this, number) -> {
                //Exist in contact list return as valid number
                stopService()
            }

            //Check if the number is not in contact list and in the black list
            number != null && !contactExists(this, number) && isInBlackList(number) -> {
                //Exist in black list return as warning
                Toast.makeText(this, "warning [$number] reason : Blacklisted", Toast.LENGTH_SHORT)
                    .show()
                mSMSFilterInterface?.warning(
                    number.toString(),
                    smsTextBody.toString(),
                    "Blacklisted"
                )
                stopService()
            }

            //Check if the number is not in contact list and in the white list
            number != null && !contactExists(this, number) && isInWhiteList(number) -> {
                //Exist in white list return as valid number
                stopService()
            }

            //If not matches with any case, call API for sms filtering
            else -> {
                apiCallForSMSFiltering(userNumber, number, smsTextBody)
            }
        }
    }

    //Check black list contacts
    private fun isInBlackList(number: String?): Boolean = mBlackList.contains(number.toString())

    //Check white list contact
    private fun isInWhiteList(number: String?): Boolean = mWhiteList.contains(number.toString())

    //API call for SMS filtering
    private fun apiCallForSMSFiltering(
        userNumber: String?,
        receiverNumber: String?,
        smsText: String?
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val apiCall = APIClient(this@SmsFilteringService).getClient()
                    .smsFilterApi(SmsFilterRequest(userNumber, receiverNumber, smsText))
                if (apiCall.isSuccessful) {
                    if (apiCall.body()?.warn != null && apiCall.body()?.warn!!) {
                        mSMSFilterInterface?.warning(
                            number.toString(),
                            smsTextBody.toString(),
                            "Blacklisted"
                        )
                        Toast.makeText(
                            this@SmsFilteringService,
                            "warning [$number] reason : ${apiCall.body()?.reason}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@SmsFilteringService,
                        "ApI Failure --> ${apiCall.body()}",
                        Toast.LENGTH_LONG
                    ).show()
                }
                stopService()
            } catch (e: Exception) {
                Toast.makeText(this@SmsFilteringService, e.message, Toast.LENGTH_LONG).show()
                stopService()
            }
        }
    }


    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    //stop the service.
    private fun stopService() {
        stopForeground(true)
        stopSelf()
    }
}