package com.bureau.models.packageDetectorHelper

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by Abhin.
 */
@Parcelize
data class AppList(val app_name: String?= null, val package_name: String?= null, var version_code : String?= null, var version_name : String?= null, var last_updated : Long?= null):
    Parcelable