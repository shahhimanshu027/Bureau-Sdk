package com.bureau.utils

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract.PhoneLookup
import androidx.core.app.ActivityCompat
import com.bureau.models.packageDetectorHelper.AllInstalledAppResponse
import com.bureau.models.packageDetectorHelper.AppList
import com.bureau.models.packageDetectorHelper.InstalledAppRequest
import com.bureau.services.ValidationService


/**
 * Created by Abhin.
 */



// check the service are running or not
fun isMyServiceRunning(
    context: Context?,
    serviceClass: Class<*>
): Boolean {
    val manager = context!!.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
    for (service in manager!!.getRunningServices(Int.MAX_VALUE)) {
        if (serviceClass.name == service.service.className) {
            return true
        }
    }
    return false
}

val phoneCallPermission = arrayOf(Manifest.permission.READ_CONTACTS, Manifest.permission.READ_PHONE_STATE, Manifest.permission.CALL_PHONE, Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_BOOT_COMPLETED, Manifest.permission.PROCESS_OUTGOING_CALLS, Manifest.permission.RECEIVE_SMS)

fun hasPermissions(
    context: Context?,
    permissions: Array<String>?
): Boolean {
    if (context != null && !permissions.isNullOrEmpty()) {
        for (permission in permissions) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
    }
    return true
}

fun contactExists(context: Context, number: String?): Boolean {
    /// number is the phone number
    val lookupUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number))
    val mPhoneNumberProjection = arrayOf(PhoneLookup._ID, PhoneLookup.NUMBER, PhoneLookup.DISPLAY_NAME)
    val cur = context.contentResolver.query(lookupUri, mPhoneNumberProjection, null, null, null)
    cur.use { cursor ->
        if (cursor!!.moveToFirst()) {
            cursor.close()
            return true
        }
    }
    return false
}

fun startNumberDetectionService(context: Context, number: String? = null, apiCallType: String = ApiCallType.CALL.name, message: String? = null, packageInfo: InstalledAppRequest? = null) {
    if (!isMyServiceRunning(context, ValidationService::class.java)) {
        ValidationService.startService(context, Intent(context, ValidationService::class.java).apply {
            putExtras(Bundle().apply {
                putString(KEY_NUMBER, number)
                putString(KEY_API_CALL_TYPE, apiCallType)
                putString(KEY_SMS_BODY, message)
                putParcelable(KEY_PACKAGE_DATA, packageInfo)
            })
        })
    }
}

fun getInstalledAppsPackageNames(context: Context): List<AppList>? {
    val flags = PackageManager.GET_META_DATA or PackageManager.GET_SHARED_LIBRARY_FILES
    val packageManager = context.packageManager
    val apps = ArrayList<AppList>()
    val packs: List<ApplicationInfo> = packageManager.getInstalledApplications(flags)
    for (i in packs.indices) {
        val p = packs[i]
        // for filter the system apps : put below code in if (!isSystemPackage(p)) { }
        val appName = p.loadLabel(packageManager).toString()
        val icon = p.loadIcon(packageManager)
        val pInfo = packageManager.getPackageInfo(p.packageName, 0)
        val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            pInfo.longVersionCode.toInt()
        } else {
            pInfo.versionCode
        }
        val versionName: String? = pInfo.versionName
        val lastUpdated = pInfo.lastUpdateTime
        val packages = p.packageName
        apps.add(AppList(appName, icon, packages, versionCode, versionName, lastUpdated))
    }
    return apps
}

private fun isSystemPackage(appInfo: ApplicationInfo): Boolean {
    return appInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0
}