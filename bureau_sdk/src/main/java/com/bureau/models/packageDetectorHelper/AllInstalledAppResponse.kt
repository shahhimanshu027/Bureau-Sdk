package com.bureau.models.packageDetectorHelper

/**
 * Created by Abhin.
 */
data class AllInstalledAppResponse(
    var package_name: String? = null,
    var reason: String? = null,
    var warn: Boolean? = null
)