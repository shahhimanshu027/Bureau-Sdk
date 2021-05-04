package com.bureau.helpers

import android.content.Context
import android.widget.Toast
import com.bureau.`interface`.ApplicationFilterInterface
import com.bureau.models.packageDetectorHelper.AppList
import com.bureau.network.APIClient
import com.bureau.utils.getInstalledAppsPackageNames
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AllInstalledAppsHelper {

    fun initAllInstalledApps(context : Context,checkInstalledApps : Boolean,listener : ApplicationFilterInterface) {
        if (checkInstalledApps){
            val allInstalledApps = getInstalledAppsPackageNames(context)
                    apiCallForAllInstalledApps(context,allInstalledApps,listener)
        }
    }

    private fun apiCallForAllInstalledApps(
        context: Context,
        allInstalledApps: ArrayList<AppList>?,
        listener: ApplicationFilterInterface
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val apiCall = APIClient(context).getClient()
                    .allInstalledAppDataApi(allInstalledApps!!)
                if (apiCall.isSuccessful && apiCall.body() != null) {
                    if (!apiCall.body().isNullOrEmpty()) {
                        val maliciousApps = apiCall.body()?.filter { it.warn == true }?.map { it.package_name }?.toList()
                        if (!maliciousApps.isNullOrEmpty()) {
                            for (i in maliciousApps.indices) {
                                listener.maliciousAppWarning(maliciousApps[i].toString(),"MaliciousAppWarning")
                                Toast.makeText(
                                    context,
                                    "App warning --> packageName : [${maliciousApps[i].toString()}] reason : MaliciousAppWarning",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                } else {
                    Toast.makeText(context, "ApI Failure --> ", Toast.LENGTH_LONG)
                        .show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }
}