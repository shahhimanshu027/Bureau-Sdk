package com.bureau.`interface`

/**
 * Created by Abhin.
 */
interface SMSFilterInterface {
    //Triggered on warning
    fun warning(number : String, textBody : String,reason : String)
}