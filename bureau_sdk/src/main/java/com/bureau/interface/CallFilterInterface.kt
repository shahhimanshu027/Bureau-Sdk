package com.bureau.`interface`

import com.bureau.models.packageDetectorHelper.AllInstalledAppResponse

/**
 * Created by Abhin.
 */
//This interface will throw the events on every response will come from server or from local DB
interface CallFilterInterface{
    //Triggered on detect number
    fun existInContact(number: String? = null)
    //Triggered on spam number detected
    fun spam()
    //Triggered on aggravated number detected
    fun aggravated()
    //Triggered on warning
    fun warning()
    //Triggered on valid number
    fun validNumber(number: String? = null)
}