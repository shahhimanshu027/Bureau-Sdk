package com.bureau.`interface`

interface UrlFilterInterface {
    fun urlDetected(url : String)
    fun safeUrl(url : String)
    fun unSafeUrl(url : String)
}