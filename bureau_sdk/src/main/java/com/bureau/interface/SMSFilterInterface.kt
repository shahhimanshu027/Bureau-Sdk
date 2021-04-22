package com.bureau.`interface`

/**
 * Created by Abhin.
 */
interface SMSFilterInterface {
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