package com.bureau.services

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import com.bureau.*
import com.bureau.`interface`.CallFilterInterface
import com.bureau.models.callFilter.request.CallFilterRequest
import com.bureau.network.APIClient
import com.bureau.utils.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*


/**
 * Created by Abhin.
 * NumberDetectionService is used to check the number is valid or is spam or blocked for incoming    calls
 */

class CallFilteringService : Service() {

    private var number: String? = null

    // start the service if it is not already running.
    companion object {

        var mCallFilterInterface: CallFilterInterface? = null
        private var preferenceManager: PreferenceManager? = null

        //Called from My Activity for save the user number in preference.
        fun initCallFilteringService(
            context: Context,
            userNumber: String,
            email : String,
            callFilterInterface: CallFilterInterface? = null
        ) {
            //initialize the sardine sdk.
//            val uniqueId = UUID.randomUUID().toString()
//            val option: Options = Options.Builder()
//                .setClientID(SARDINE_CLIENT_ID)
//                .setSessionKey(uniqueId)
//                .setUserIDHash(getMd5HashId(email))
//                .setEnvironment(Options.ENV_PRODUCTION)
//                .build()
//            MobileIntelligence.init(context, option)

            this.mCallFilterInterface = callFilterInterface
            preferenceManager =
                PreferenceManager(context.getSharedPreferences(MY_PREFERENCE, MODE_PRIVATE))
            preferenceManager?.setValue(PREF_USER_MOBILE, userNumber)
        }

        fun startCallFilteringService(context: Context, intent: Intent) {
            try {
                //check if service is not running then start the service
                if (!isMyServiceRunning(context, CallFilteringService::class.java)) {
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

        identifyNumber()
        return super.onStartCommand(intent, flags, startId)
    }

    // identify number in contact list
    @SuppressLint("MissingPermission", "HardwareIds")
    private fun identifyNumber() {

        val userNumber = preferenceManager?.getValue(PREF_USER_MOBILE, "12345")
        //Check api call state and perform operation on the basis of it

        if (number != null && contactExists(this, number)) {
            stopService()
        } else {
            apiCallForCallFiltering(userNumber, number)
        }

    }

    //API call for call filtering
    private fun apiCallForCallFiltering(userNumber: String?, receiverNumber: String?) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val apiCall = APIClient(this@CallFilteringService).getClient()
                    .callFilterApi(CallFilterRequest("12345", receiverNumber))
                if (apiCall.isSuccessful) {
                    if (apiCall.body()?.warn != null && apiCall.body()?.warn!!) {
                        Toast.makeText(
                            this@CallFilteringService,
                            "warning [$number]",
                            Toast.LENGTH_LONG
                        )
                            .show()
                        mCallFilterInterface?.warning(number.toString(),apiCall.body()?.reason.toString())
                    }
                } else {
                    Toast.makeText(
                        this@CallFilteringService,
                        "ApI Failure ${apiCall.message()}",
                        Toast.LENGTH_LONG
                    ).show()
                }
                stopService()
            } catch (e: Exception) {
                Toast.makeText(this@CallFilteringService, e.message, Toast.LENGTH_LONG).show()
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