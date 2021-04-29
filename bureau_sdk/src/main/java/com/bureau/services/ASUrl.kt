package com.bureau.services

import android.accessibilityservice.AccessibilityService
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.bureau.`interface`.UrlFilterInterface
import com.bureau.models.callFilter.request.CallFilterRequest
import com.bureau.network.APIClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

/**
 * Created by Abhin.
 */
class ASUrl : AccessibilityService() {

    private var isApiCall = false

    companion object {
        private var isOneTimeApiCall = false
        var mUrlFilterInterface: UrlFilterInterface? = null

        fun initCallbacks(listener: UrlFilterInterface) {
            mUrlFilterInterface = listener
        }

        /** @return a list of supported browser configs
         * This list could be instead obtained from remote server to support future browser updates without updating an app
         */
        private val supportedBrowsers: List<SupportedBrowserConfig>
            get() {
                val browsers: MutableList<SupportedBrowserConfig> = ArrayList()
                browsers.add(
                    SupportedBrowserConfig("com.android.chrome", "com.android.chrome:id/url_bar")
                )
                browsers.add(
                    SupportedBrowserConfig(
                        "org.mozilla.firefox",
                        "org.mozilla.firefox:id/url_bar_title"
                    )
                )
                return browsers
            }
    }

    override fun onServiceConnected() {
        Log.e("TAG", "OnServiceConnected")
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val source = event.source ?: return
        val packageName = source.packageName.toString()
        var browserConfig: SupportedBrowserConfig? = null
        for (supportedConfig in supportedBrowsers) {
            if (supportedConfig.packageName == packageName) {
                browserConfig = supportedConfig
            }
        }
        //this is not supported browser, so exit
        if (browserConfig == null) {
            return
        }
        val browserList = Arrays.asList(*browserConfig.packageName.split(",\\s*").toTypedArray())
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if (!browserList.contains(packageName)) {
                return
            }
        }
        if (browserList.contains(packageName)) {
            try {
                // App opened is a browser.
                // Parse urls in browser.
                if (AccessibilityEvent.eventTypeToString(event.eventType).contains("WINDOW")) {
                    val nodeInfo = event.source
                    getUrlsFromViews(nodeInfo, browserConfig)
                }
            } catch (ex: StackOverflowError) {
                ex.printStackTrace()
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    /**
     * Method to loop through all the views and try to find a URL.
     *
     * @param info
     * @param browserConfig
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    fun getUrlsFromViews(info: AccessibilityNodeInfo, browserConfig: SupportedBrowserConfig) {
        val nodes = info.findAccessibilityNodeInfosByViewId(browserConfig.addressBarId)
        try {
            if (nodes == null) return
            if (nodes.isNotEmpty()) {
                val addressBarNodeInfo = nodes[0]
                var url: String? = null
                if (addressBarNodeInfo.text != null) {
                    url = addressBarNodeInfo.text.toString()
                }
                addressBarNodeInfo.recycle()
                Log.e("TAG", url!!)
                if (!isApiCall) {
                    CoroutineScope(Dispatchers.Main).launch {
                        apiCallForCallFiltering("1234567890", "123456789")
                    }
                    isApiCall = true
                }
            }
        } catch (ex: StackOverflowError) {
            ex.printStackTrace()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    override fun onInterrupt() {}
    class SupportedBrowserConfig(var packageName: String, var addressBarId: String)

    private fun apiCallForCallFiltering(userNumber: String?, receiverNumber: String?) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val apiCall = APIClient(this@ASUrl).getClient()
                    .callFilterApi(CallFilterRequest(userNumber, receiverNumber))
                if (apiCall.isSuccessful) {
                    if (apiCall.body()?.warn != null && apiCall.body()?.warn!!) {
                        Toast.makeText(this@ASUrl, "unSafeUrl", Toast.LENGTH_LONG)
                            .show()
                        mUrlFilterInterface?.unSafeUrl("url")
                    } else {
                        Toast.makeText(
                            this@ASUrl,
                            "safeUrl",
                            Toast.LENGTH_LONG
                        ).show()
                        mUrlFilterInterface?.safeUrl("url")
                    }
                } else {
                    Toast.makeText(
                        this@ASUrl,
                        "ApI Failure --> ${apiCall.body()}",
                        Toast.LENGTH_LONG
                    ).show()
                }
                isApiCall = false
            } catch (e: Exception) {
                isApiCall = false
                Toast.makeText(this@ASUrl, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }
}