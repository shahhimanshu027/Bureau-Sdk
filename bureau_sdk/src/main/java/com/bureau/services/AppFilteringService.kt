package com.bureau.services

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import com.bureau.`interface`.ApplicationFilterInterface
import com.bureau.helpers.NotificationHelper
import com.bureau.models.packageDetectorHelper.AppList
import com.bureau.network.APIClient
import com.bureau.utils.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


/**
 * Created by Abhin.
 * AppFilteringService is used to check the packagename for installed app is valid or is spam.
 */

class AppFilteringService : Service() {

    private var installedPackageData: AppList? = null

    // start the service if it is not already running.
    companion object {

        var mApplicationFilterInterface: ApplicationFilterInterface? = null

        //Called from My Activity for save the user number in preference.
        fun initAppFilteringService(
            context: Context,
            applicationFilterInterface: ApplicationFilterInterface
        ) {
            this.mApplicationFilterInterface = applicationFilterInterface
        }

        fun startAppFilteringService(context: Context, intent: Intent) {
            try {
                //check if service is not running then start the service
                if (!isMyServiceRunning(context, AppFilteringService::class.java)) {
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
        //Get installed package data
        installedPackageData = intent?.getParcelableExtra(KEY_PACKAGE_DATA)

        identifyNumber()
        return super.onStartCommand(intent, flags, startId)
    }

    // identify number in contact list
    @SuppressLint("MissingPermission", "HardwareIds")
    private fun identifyNumber() {

        installedPackageData?.let { requestBody ->
            apiCallForNewInstalledPackage(requestBody)
        }
    }

    //API call for new installed application
    private fun apiCallForNewInstalledPackage(requestBody: AppList) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val apiCall = APIClient(this@AppFilteringService).getClient()
                    .allInstalledAppDataApi(arrayListOf(requestBody))
                if (apiCall.isSuccessful && apiCall.body() != null) {
                    if (!apiCall.body().isNullOrEmpty()) {
                        val maliciousApps =
                            apiCall.body()?.filter { it.warn == true }?.toList()
                        if (!maliciousApps.isNullOrEmpty()) {
                            for (i in maliciousApps.indices) {
                                mApplicationFilterInterface?.maliciousAppWarning(maliciousApps[i].toString(),"MaliciousAppWarning")
                                NotificationHelper().showNotification(
                                    this@AppFilteringService,
                                    "App Warning [${maliciousApps[i].package_name}]",
                                    "Reason : ${maliciousApps[i].reason}",
                                    notificationData = null
                                )
                            }
                            stopService()
                        }
                    }
                } else {
                    Toast.makeText(this@AppFilteringService, "ApI Failure --> ", Toast.LENGTH_LONG)
                        .show()
                }
                stopService()
            } catch (e: Exception) {
                e.printStackTrace()
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