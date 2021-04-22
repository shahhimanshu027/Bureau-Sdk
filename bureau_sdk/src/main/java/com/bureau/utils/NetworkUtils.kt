package com.bureau.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.os.Build

/**File containing all Network related utility functions**/


fun isInternetAvailable(appContext: Context): Boolean {
    var result = false
    val connectivityManager = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        connectivityManager?.let {
            it.getNetworkCapabilities(connectivityManager.activeNetwork)?.apply {
                result = when {
                    hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                    hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                    else -> false
                }
            }
        }
    } else {
        val activeNetwork: NetworkInfo? = connectivityManager?.activeNetworkInfo
        result = activeNetwork?.isConnectedOrConnecting == true
    }
    return result
}
