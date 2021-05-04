package com.bureau.`interface`

import com.bureau.models.packageDetectorHelper.AllInstalledAppResponse

/**
 * Created by Abhin.
 */
//This interface will throw the events on every response will come from server or from local DB
interface CallFilterInterface{
    //Triggered on warning
    fun warning(number : String, reason : String)
}