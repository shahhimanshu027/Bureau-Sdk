package com.bureau.helpers

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast
import com.bureau.`interface`.ApplicationFilterInterface
import com.bureau.models.packageDetectorHelper.AppList
import com.bureau.network.APIClient
import com.bureau.utils.MY_PREFERENCE
import com.bureau.utils.PREF_SIM_SERIAL_NUM
import com.bureau.utils.PreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SimChangeNotifyHelper {

    private var preferenceManager : PreferenceManager? = null

    @SuppressLint("HardwareIds", "MissingPermission")
    fun initSimChangeNotifyHelper(context: Context){
        preferenceManager = PreferenceManager(context.getSharedPreferences(MY_PREFERENCE, Context.MODE_PRIVATE))
        val telephonyManager: TelephonyManager =
            context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        if (preferenceManager?.getValue(PREF_SIM_SERIAL_NUM, "").isNullOrEmpty()) {
            var simSerialNum = ""
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                val subscriptionManager: SubscriptionManager =
                    context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
                val subsList: List<SubscriptionInfo> =
                    subscriptionManager.activeSubscriptionInfoList
                for (subsInfo in subsList) {
                    simSerialNum = subsInfo.iccId
                }
            } else {
                simSerialNum = telephonyManager.simSerialNumber
            }
            Log.e("TAG", "simSerialNum() --> $simSerialNum ")



            preferenceManager?.setValue(PREF_SIM_SERIAL_NUM, simSerialNum)
        } else {
            Log.e(
                "TAG",
                "simSerialNum() pref --> ${preferenceManager?.getValue(PREF_SIM_SERIAL_NUM, "")} "
            )
        }
    }

//    private fun apiCallForSimChangeEvent(context: Context,) {
//        CoroutineScope(Dispatchers.Main).launch {
//            try {
//                val apiCall = APIClient(context).getClient()
//                    .allInstalledAppDataApi(allInstalledApps!!)
//                if (apiCall.isSuccessful && apiCall.body() != null) {
//                    Toast.makeText(
//                        context,
//                        "Api Success --> ",
//                        Toast.LENGTH_LONG
//                    ).show()
//                    if (!apiCall.body().isNullOrEmpty()) {
//                        val maliciousApps = apiCall.body()?.filter { it.warn == true }?.map { it.package_name } as ArrayList<String>
//                        listener.maliciousApps(maliciousApps)
//                        val commaSeparatedString = maliciousApps.joinToString(separator = ", ")
//                        Toast.makeText(
//                            context,
//                            "Malicious Apps --> $commaSeparatedString ",
//                            Toast.LENGTH_LONG
//                        ).show()
//                    }
//                } else {
//                    Toast.makeText(context, "ApI Failure --> ", Toast.LENGTH_LONG)
//                        .show()
//                }
//            } catch (e: Exception) {
//                Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
//            }
//        }
//    }
}