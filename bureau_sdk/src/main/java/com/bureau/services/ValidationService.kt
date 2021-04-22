package com.bureau.services

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import com.bureau.`interface`.ApplicationFilterInterface
import com.bureau.`interface`.CallFilterInterface
import com.bureau.`interface`.SIMFilterInterface
import com.bureau.`interface`.SMSFilterInterface
import com.bureau.models.callFilter.request.CallFilterRequest
import com.bureau.models.callFilter.request.SmsFilterRequest
import com.bureau.models.packageDetectorHelper.InstalledAppRequest
import com.bureau.network.APIClient
import com.bureau.utils.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


/**
 * Created by Abhin.
 * NumberDetectionService is used to check the number is valid or is spam or blocked
 */

class ValidationService : Service() {

    private var number: String? = null
    private var smsTextBody: String? = null
    private var apiCallState: String? = null
    private var installedPackageData: InstalledAppRequest? = null

    // start the service if it is not already running.
    companion object {
        var mCallFilterInterface: CallFilterInterface? = null
        var mSMSFilterInterface: SMSFilterInterface? = null
        var mApplicationFilterInterface: ApplicationFilterInterface? = null
        var mSimFilterInterface: SIMFilterInterface? = null

        private var preferenceManager: PreferenceManager? = null

        //Called from My Activity for save the user number in preference.
        fun init(
            context: Context,
            userNumber: String,
            callFilterInterface: CallFilterInterface? = null,
            smsFilterInterface: SMSFilterInterface,
            applicationFilterInterface: ApplicationFilterInterface,
            simFilterInterface: SIMFilterInterface
        ) {
            this.mCallFilterInterface = callFilterInterface
            this.mSMSFilterInterface = smsFilterInterface
            this.mApplicationFilterInterface = applicationFilterInterface
            this.mSimFilterInterface = simFilterInterface
            preferenceManager =
                PreferenceManager(context.getSharedPreferences(MY_PREFERENCE, Context.MODE_PRIVATE))
            preferenceManager?.setValue(PREF_USER_MOBILE, userNumber)
        }

        fun startService(context: Context, intent: Intent) {
            try {
                //check if service is not running then start the service
                if (!isMyServiceRunning(context, ValidationService::class.java)) {
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

    override fun onBind(p0: Intent?): IBinder? {
        return null
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
        //Get API call type
        apiCallState = intent?.getStringExtra(KEY_API_CALL_TYPE)
        //Get installed package data
        installedPackageData = intent?.getParcelableExtra(KEY_PACKAGE_DATA)
        Log.e("TAG", "onStartCommand() apiCallState --> $apiCallState")
        //Get number
        number = intent?.getStringExtra(KEY_NUMBER)
        //Get SMS body
        smsTextBody = intent?.getStringExtra(KEY_SMS_BODY)

        //Call function to identify the number
        identifyNumber(apiCallState)
        return super.onStartCommand(intent, flags, startId)
    }

    // identify number in contact list
    @SuppressLint("MissingPermission", "HardwareIds")
    private fun identifyNumber(apiCallState: String?) {
        val userNumber = preferenceManager?.getValue(PREF_USER_MOBILE, "")
        //Check api call state and perform operation on the basis of it
        when (apiCallState) {

            //SMS filtering
            ApiCallType.SMS.name -> {
                when {
                    //Check if the number is in contact list or not
                    number != null && contactExists(this, number) -> {
                        //Exist in contact list return as valid number
                        Toast.makeText(this, "validNumber [$number]", Toast.LENGTH_SHORT).show()
                        mSMSFilterInterface?.existInContact(number)
                    }

                    //Check if the number is not in contact list and in the black list
                    number != null && !contactExists(this, number) && isInBlackList(number) -> {
                        //Exist in black list return as warning
                        Toast.makeText(this, "warning [$number]", Toast.LENGTH_SHORT).show()
                        mSMSFilterInterface?.warning()
                    }

                    //Check if the number is not in contact list and in the white list
                    number != null && !contactExists(this, number) && isInWhiteList(number) -> {
                        //Exist in white list return as valid number
                        Toast.makeText(this, "validNumber [$number]", Toast.LENGTH_SHORT).show()
                        mSMSFilterInterface?.validNumber(number)
                    }

                    //If not matches with any case, call API for sms filtering
                    else -> {
                        apiCallForSMSFiltering(userNumber, number, smsTextBody)
                    }
                }
            }

            //Call filtering
            ApiCallType.CALL.name -> {
                if (number != null && contactExists(this, number)) {
                    Toast.makeText(this, "VALID number [$number]", Toast.LENGTH_LONG).show()
                    mCallFilterInterface?.existInContact(number)
                } else {
                    apiCallForCallFiltering(userNumber, number)
                }
            }

            //App filtering
            ApiCallType.PACKAGE.name -> {
                installedPackageData?.let { requestBody ->
                    apiCallForNewInstalledPackage(requestBody)
                }
            }
        }
    }

    //Check black list contacts
    private fun isInBlackList(number: String?): Boolean = mBlackList.contains(number.toString())

    //Check white list contact
    private fun isInWhiteList(number: String?): Boolean = mWhiteList.contains(number.toString())

    //API call for new installed application
    private fun apiCallForNewInstalledPackage(requestBody: InstalledAppRequest) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val apiCall = APIClient(this@ValidationService).getClient()
                    .allInstalledAppDataApi(arrayListOf(requestBody))
                if (apiCall.isSuccessful && apiCall.body() != null) {
                    if (!apiCall.body().isNullOrEmpty()) {
                        val maliciousApps = apiCall.body()?.filter { it.warn == true } as ArrayList
                        mApplicationFilterInterface?.maliciousApps(maliciousApps)
                        val commaSeparatedString = maliciousApps.joinToString(separator = ", ")
                        Toast.makeText(
                            this@ValidationService,
                            "Malicious Apps --> $commaSeparatedString ",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    Toast.makeText(this@ValidationService, "ApI Failure --> ", Toast.LENGTH_LONG)
                        .show()
                }
                stopService()
            } catch (e: Exception) {
                Toast.makeText(this@ValidationService, e.message, Toast.LENGTH_LONG).show()
                stopService()
            }
        }
    }

    //API call for SMS filtering
    private fun apiCallForSMSFiltering(userNumber: String?, receiverNumber: String?, smsText: String?) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val apiCall = APIClient(this@ValidationService).getClient()
                    .smsFilterApi(SmsFilterRequest(userNumber, receiverNumber, smsText))
                if (apiCall.isSuccessful) {
                    Toast.makeText(
                        this@ValidationService,
                        "ApI Success --> ${apiCall.body()?.reason}",
                        Toast.LENGTH_LONG
                    ).show()
                    if (apiCall.body()?.warn != null && apiCall.body()?.warn!!) {
                        mSMSFilterInterface?.spam()
                        Toast.makeText(this@ValidationService, "spam [$number]", Toast.LENGTH_LONG)
                            .show()
                    } else {
                        Toast.makeText(
                            this@ValidationService,
                            "validNumber [$number]",
                            Toast.LENGTH_LONG
                        ).show()
                        mSMSFilterInterface?.validNumber(number)
                    }
                } else {
                    Toast.makeText(
                        this@ValidationService,
                        "ApI Failure --> ${apiCall.body()}",
                        Toast.LENGTH_LONG
                    ).show()
                }
                stopService()
            } catch (e: Exception) {
                Toast.makeText(this@ValidationService, e.message, Toast.LENGTH_LONG).show()
                stopService()
            }
        }
    }

    //API call for call filtering
    private fun apiCallForCallFiltering(userNumber: String?, receiverNumber: String?) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val apiCall = APIClient(this@ValidationService).getClient()
                    .callFilterApi(CallFilterRequest(userNumber, receiverNumber))
                if (apiCall.isSuccessful) {
                    Toast.makeText(
                        this@ValidationService,
                        "ApI Success --> ${apiCall.body()?.reason} ",
                        Toast.LENGTH_LONG
                    ).show()
                    if (apiCall.body()?.warn != null && apiCall.body()?.warn!!) {
                        Toast.makeText(this@ValidationService, "spam [$number]", Toast.LENGTH_LONG)
                            .show()
                        mCallFilterInterface?.spam()
                    } else {
                        Toast.makeText(
                            this@ValidationService,
                            "validNumber [$number]",
                            Toast.LENGTH_LONG
                        ).show()
                        mCallFilterInterface?.validNumber(number)
                    }
                } else {
                    Toast.makeText(
                        this@ValidationService,
                        "ApI Failure --> ${apiCall.body()}",
                        Toast.LENGTH_LONG
                    ).show()
                }
                stopService()
            } catch (e: Exception) {
                Toast.makeText(this@ValidationService, e.message, Toast.LENGTH_LONG).show()
                stopService()
            }
        }
    }

    //stop the service.
    private fun stopService() {
        stopForeground(true)
        stopSelf()
    }

}