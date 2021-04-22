package com.bureau.models.packageDetectorHelper

import android.graphics.drawable.Drawable

/**
 * Created by Abhin.
 */
data class AppList(val name: String?= null, var icon: Drawable?= null, val packages: String?= null,var versionCode : Int?= null, var versionName : String?= null, var lastUpdated : Long?= null)