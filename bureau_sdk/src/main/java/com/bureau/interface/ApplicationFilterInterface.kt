package com.bureau.`interface`

/**
 * Created by Abhin.
 */
interface ApplicationFilterInterface {
    //Triggered on malicious app detected
    fun maliciousAppWarning(packageName : String, reason : String)
}