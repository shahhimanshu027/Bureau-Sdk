package com.bureau.receivers.packageDetector

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import com.bureau.models.packageDetectorHelper.InstalledAppRequest
import com.bureau.utils.ApiCallType
import com.bureau.utils.startNumberDetectionService


/**
 * Created by Abhin.
 * AppInstallReceiver is used to get event when any new app installed in the device
 */

class AppInstallReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        //Checking is there any new app is installed
        if (intent?.action == "android.intent.action.PACKAGE_ADDED" || intent?.action == "android.intent.action.PACKAGE_INSTALL") {
            Toast.makeText(context, "${intent.data?.encodedSchemeSpecificPart}", Toast.LENGTH_SHORT).show()
            val packageName = intent.data?.encodedSchemeSpecificPart
            val packageManager = context?.packageManager!!
            val packageInfo = packageName?.let { packageManager.getPackageInfo(it, 0) }
            val appName = packageInfo?.applicationInfo?.loadLabel(packageManager).toString()
            val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo?.longVersionCode?.toInt()
            } else {
                packageInfo?.versionCode
            }
            val versionName: String? = packageInfo?.versionName
            val lastUpdated = packageInfo?.lastUpdateTime
            val requestBody = InstalledAppRequest(appName,lastUpdated,packageName,versionCode,versionName)
            // Starting the service to get the valid or invalid application
            startNumberDetectionService(context = context, apiCallType = ApiCallType.PACKAGE.name,packageInfo = requestBody)
        }
    }
}
