package com.bureau.services

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import com.bureau.`interface`.ApplicationFilterInterface
import com.bureau.models.packageDetectorHelper.AppList
import com.bureau.network.APIClient
import com.bureau.utils.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


/**
 * Created by Abhin.
 * NumberDetectionService is used to check the number is valid or is spam or blocked
 */

class AppFilteringService : Service() {

    private var installedPackageData: AppList? = null

    // start the service if it is not already running.
    companion object {

        var mApplicationFilterInterface: ApplicationFilterInterface? = null

        private var preferenceManager: PreferenceManager? = null

        //Called from My Activity for save the user number in preference.
        fun initAppFilteringService(
            context: Context,
            userNumber: String,
            applicationFilterInterface: ApplicationFilterInterface
        ) {
            this.mApplicationFilterInterface = applicationFilterInterface
            preferenceManager =
                PreferenceManager(context.getSharedPreferences(MY_PREFERENCE, Context.MODE_PRIVATE))
            preferenceManager?.setValue(PREF_USER_MOBILE, userNumber)
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
        if (preferenceManager == null) {
            preferenceManager = PreferenceManager(
                getSharedPreferences(
                    MY_PREFERENCE,
                    Context.MODE_PRIVATE
                )
            )
        }
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
                        Toast.makeText(
                            this@AppFilteringService,
                            "Api Success --> ${apiCall.body()!![0].reason} ",
                            Toast.LENGTH_LONG
                        ).show()
                        val maliciousApps =
                            apiCall.body()?.filter { it.warn == true } as ArrayList<String>
                        if (!maliciousApps.isNullOrEmpty()) {
                            mApplicationFilterInterface?.maliciousApps(maliciousApps)
                        } else {
                            mApplicationFilterInterface?.safeApp(apiCall.body()!![0].package_name.toString())
                        }
                    }
                } else {
                    Toast.makeText(this@AppFilteringService, "ApI Failure --> ", Toast.LENGTH_LONG)
                        .show()
                }
                stopService()
            } catch (e: Exception) {
                Toast.makeText(this@AppFilteringService, e.message, Toast.LENGTH_LONG).show()
                stopService()
            }
        }
    }


    private fun apiCallForAllInstalledApps(allInstalledApps: ArrayList<AppList>?) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val apiCall = APIClient(this@AppFilteringService).getClient()
                    .allInstalledAppDataApi(allInstalledApps!!)
                if (apiCall.isSuccessful && apiCall.body() != null) {
                    if (!apiCall.body().isNullOrEmpty()) {
                        val maliciousApps = apiCall.body()?.filter { it.warn == true }
                            ?.map { it.package_name } as ArrayList<String>
                        mApplicationFilterInterface?.maliciousApps(maliciousApps)
                        val commaSeparatedString = maliciousApps.joinToString(separator = ", ")
                        Toast.makeText(
                            this@AppFilteringService,
                            "Malicious Apps --> $commaSeparatedString ",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    Toast.makeText(this@AppFilteringService, "ApI Failure --> ", Toast.LENGTH_LONG)
                        .show()
                }
                stopService()
            } catch (e: Exception) {
                Toast.makeText(this@AppFilteringService, e.message, Toast.LENGTH_LONG).show()
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