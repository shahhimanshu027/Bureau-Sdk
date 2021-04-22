package com.bureau.`interface`

/**
 * Created by Abhin.
 */
interface ApplicationFilterInterface {
    //Triggered on malicious app detected
    fun maliciousApps(list: ArrayList<String>)
    fun safeApp(appName : String)
}