package com.bureau.`interface`

import com.bureau.models.packageDetectorHelper.AllInstalledAppResponse

/**
 * Created by Abhin.
 */
interface ApplicationFilterInterface {
    //Triggered on malicious app detected
    fun maliciousApps(list : ArrayList<AllInstalledAppResponse>)
}