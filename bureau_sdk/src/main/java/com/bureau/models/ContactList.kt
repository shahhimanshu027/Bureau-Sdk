package com.bureau.models

/**
 * Created by Abhin.
 */
open class ContactList(var id: Long = 0, var name: String = "", var number: String = "", var hasPhone: Boolean = false, var path: String = "", var imagePath: String = "", var isInvite: Boolean = false)

class PhoneList(var phoneId:String="",var phoneNumber:String="") : ContactList()