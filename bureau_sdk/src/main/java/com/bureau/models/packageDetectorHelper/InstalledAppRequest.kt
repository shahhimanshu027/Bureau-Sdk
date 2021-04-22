package com.bureau.models.packageDetectorHelper

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by Abhin.
 */
@Parcelize
data class InstalledAppRequest(var app_name: String? = null, var last_updated: Long? = null, var package_name: String? = null, var version_code: Int? = null, var version_name: String? = null) : Parcelable