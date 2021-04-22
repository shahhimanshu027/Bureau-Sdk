package com.bureau.utils

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bureau.R
import java.util.*

/**
 * Created by Abhin.
 */
@Suppress("UNCHECKED_CAST")
class MarshMellowHelper {

    private var requestCode: Int? = null
    private var activity: Activity? = null
    private var fragment: Fragment? = null
    private var permissions: Array<String>? = null
    private var mPermissionCallback: PermissionCallback? = null
    private var showRational: Boolean = false
    private var mContext: Context? = null

    constructor(activity: Activity, permissions: Array<String>, requestCode: Int) {
        this.activity = activity
        this.permissions = permissions
        this.requestCode = requestCode
        mContext = getContext()
    }

    constructor(fragment: Fragment, permissions: Array<String>, requestCode: Int) {
        this.fragment = fragment
        this.permissions = permissions
        this.requestCode = requestCode
        mContext = getContext()
    }

    fun request(permissionCallback: PermissionCallback) {
        this.mPermissionCallback = permissionCallback
        if (!checkSelfPermission(permissions!!)) {
            showRational = shouldShowRational(permissions!!)
            if (activity != null) requestCode?.let {
                ActivityCompat.requestPermissions(
                    activity!!,
                    filterNotGrantedPermission(permissions!!),
                    it
                )
            }
            else requestCode?.let {
                fragment!!.requestPermissions(
                    filterNotGrantedPermission(permissions!!),
                    it
                )
            }
        } else {
            if (mPermissionCallback != null) mPermissionCallback!!.onPermissionGranted()
        }
    }

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == this.requestCode) {
            var denied = false
            for (grantResult in grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    denied = true
                    break
                }
            }
            if (denied) {
                val currentShowRational = shouldShowRational(permissions)
                if (!showRational && !currentShowRational) {
                    if (mPermissionCallback != null) mPermissionCallback!!.onPermissionDeniedBySystem(
                        mContext!!.getString(
                            R.string.permission_denied_by_system
                        )
                    )
                } else {
                    if (mPermissionCallback != null) mPermissionCallback!!.onPermissionDenied(
                        mContext!!.getString(
                            R.string.permission_denied
                        )
                    )
                }
            } else {
                if (mPermissionCallback != null) mPermissionCallback!!.onPermissionGranted()
            }
        }
    }

    private fun <T : Context> getContext(): T {
        return if (activity != null) activity as T else fragment!!.context as T
    }

    /**
     * Return list that is not granted and we need to ask for permission
     *
     * @param permissions
     * @return
     */
    private fun filterNotGrantedPermission(permissions: Array<String>): Array<String> {
        val notGrantedPermission = ArrayList<String>()
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(getContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                notGrantedPermission.add(permission)
            }
        }
        return notGrantedPermission.toTypedArray()
    }

    /**
     * Check permission is there or not for fit_watch of permissions
     *
     * @param permissions
     * @return
     */
    private fun checkSelfPermission(permissions: Array<String>): Boolean {
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(getContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    /**
     * Checking if there is need to show rational for fit_watch of permissions
     *
     * @param permissions
     * @return
     */
    private fun shouldShowRational(permissions: Array<String>): Boolean {
        var currentShowRational = false
        for (permission in permissions) {

            if (activity != null) {
                if (shouldShowRequestPermissionRationale(activity!!, permission)) {
                    currentShowRational = true
                    break
                }
            } else {
                if (fragment!!.shouldShowRequestPermissionRationale(permission)) {
                    currentShowRational = true
                    break
                }
            }
        }
        return currentShowRational
    }

    interface PermissionCallback {
        fun onPermissionGranted()

        fun onPermissionDenied(permissionDeniedError: String)

        fun onPermissionDeniedBySystem(permissionDeniedBySystem: String)
    }
}
